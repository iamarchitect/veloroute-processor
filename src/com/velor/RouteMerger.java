package com.velor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;

import com.velor.storage.database.Cursor;

/**
 * Some routes having same name that should be connected are disconnected.
 * Reconnect them in the database.
 */
public class RouteMerger implements Preprocessor {
	private DatabaseManager databaseManager;

	// select the routes and the geo points from route data where the geo points
	// are connected ie very close together and their type id is the same.
	private String selectIntersectionsSql;

	// the table containing route geo data
	private String routedataTable;

	// update the route data, setting two connected route with same type ids
	// with the same route id.
	private String updateMergedSql;

	// recalculate the length of each merged route.
	private String updateMergedLengthSql;

	// delete the route definition that have been merged from the routes table
	private String deleteMergedSql;

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setSelectIntersectionsSql(String selectIntersectionsSql) {
		this.selectIntersectionsSql = selectIntersectionsSql;
	}

	public void setRoutedataTable(String routedataTable) {
		this.routedataTable = routedataTable;
	}

	public void setUpdateMergedSql(String updateMergedSql) {
		this.updateMergedSql = updateMergedSql;
	}

	public void setUpdateMergedLengthSql(String updateMergedLengthSql) {
		this.updateMergedLengthSql = updateMergedLengthSql;
	}

	public void setDeleteMergedSql(String deleteMergedSql) {
		this.deleteMergedSql = deleteMergedSql;
	}

	/**
	 * select the routes and the geo points from route data where the geo points
	 * are connected ie very close together and their type id is the same.
	 * 
	 * @return
	 */
	private Cursor queryForExploded() {
		return databaseManager
				.rawQuery(selectIntersectionsSql, new String[] {});
	}

	/**
	 * update the route data, setting two connected route with same type ids
	 * with the same route id.
	 * 
	 * @param fromId
	 * @param toId
	 */
	private void merge(long fromId, long toId) {

		// remove the route_id vertex as it is already defined in the previous
		// route.
		databaseManager.delete(routedataTable, "route_id=? AND ordinality=0",
				new String[] { Long.toString(fromId) });

		// change route_id of routedata
		String[] args = { Long.toString(toId), Long.toString(toId),
				Long.toString(fromId) };
		databaseManager.execSQL(updateMergedSql, args);

		// increment length of route
		String[] args2 = { Long.toString(fromId), Long.toString(toId) };
		databaseManager.execSQL(updateMergedLengthSql, args2);
	}

	/**
	 * delete the route definition that have been merged from the routes table
	 */
	private void finishMerge() {
		// remove routes not in routedata
		String[] args3 = {};
		databaseManager.execSQL(deleteMergedSql, args3);
	}

	/**
	 * Some routes having same name that should be connected are disconnected.
	 * Reconnect them in the database.
	 */
	protected void mergeRoutes() {
		Cursor c = queryForExploded();
		int firstCol = c.getColumnIndex("first_id");
		int secondCol = c.getColumnIndex("second_id");

		// index : source=key
		Map<Long, Long> idxa = new TreeMap<Long, Long>();

		// index : destination=key
		Map<Long, Long> idxb = new TreeMap<Long, Long>();

		// list of destinations
		List<Long> seconds = new ArrayList<Long>();

		// list of found leafs
		Set<Long> leafs = new TreeSet<Long>();

		// already known nodes not leafs
		Set<Long> visited = new TreeSet<Long>();

		// populate the indexes
		while (c.moveToNext()) {
			long firstId = c.getLong(firstCol);
			long secondId = c.getLong(secondCol);

			idxa.put(firstId, secondId);
			idxb.put(secondId, firstId);
			seconds.add(secondId);
		}

		c.close();

		// find leafs of graph.
		for (Long key : seconds) {
			if (visited.contains(key)) {
				continue;
			}

			// remove all non leafs
			Long src = idxb.get(key);
			while (src != null) {
				visited.add(src);
				src = idxb.get(src);
			}

			// recurse until leaf is found
			Long dst = idxa.get(key);
			while (dst != null) {
				key = dst;
				dst = idxa.get(key);
			}
			leafs.add(key);
		}

		// actually update the database, merging connected routes
		for (Long key : leafs) {

			Long src = idxb.get(key);
			while (src != null) {
				merge(key, src);
				key = src;
				src = idxb.get(key);
			}
		}
		finishMerge();
	}

	@Override
	public void preprocess(CommandLine cmd) {
		mergeRoutes();
	}

}
