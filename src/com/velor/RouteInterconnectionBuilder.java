package com.velor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;

import com.velor.algorithms.geodata.GeoUtils;
import com.velor.storage.database.Cursor;
import com.velor.storage.mapper.VOMapper;
import com.velor.storage.mapper.VOMapperFactory;
import com.velor.storage.vo.RouteConnection;
import com.velor.storage.vo.RouteConnectionPoint;

import android.content.ContentValues;

/**
 * Create a grph of routes represented by RouteConnection between tow
 * RouteConnectionPoint.
 * 
 * @author glenn-eric
 * 
 */
public class RouteInterconnectionBuilder implements Preprocessor {
	private DatabaseManager databaseManager;

	// insert statements
	private String[] edgeCreationSqls;

	// the table containing route geo data
	private String routedataTable;
	// the table containting the interconntions ie route connection points

	private String edgesTable;

	// the statement for selecting route connection points
	private String selectEdgesSql;

	// a mapper from cursor to route connection points
	private VOMapper<RouteConnectionPoint> connMapper;

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setEdgeCreationSqls(String[] edgeCreationSqls) {
		this.edgeCreationSqls = edgeCreationSqls;
	}

	public void setRoutedataTable(String routedataTable) {
		this.routedataTable = routedataTable;
	}

	public void setEdgesTable(String edgesTable) {
		this.edgesTable = edgesTable;
	}

	public void setSelectEdgesSql(String selectEdgesSql) {
		this.selectEdgesSql = selectEdgesSql;
	}

	public RouteInterconnectionBuilder() {
		super();
		VOMapperFactory<RouteConnectionPoint> fconn = VOMapperFactory
				.getInstance();
		connMapper = fconn.getMapper(RouteConnectionPoint.class);
	}

	protected Cursor queryRoute(long id) {
		return databaseManager.query(routedataTable, null, "route_id=?",
				new String[] { Long.toString(id) }, null, null, "ordinality");
	}

	protected double distance(long srcid, int srcord, long dstid, int dstord) {
		double distance = 0;
		if (srcid == dstid) {
			Cursor route = queryRoute(srcid);

			List<double[]> data = new ArrayList<double[]>();

			int latC = route.getColumnIndex("latitude");
			int lonC = route.getColumnIndex("longitude");

			while (route.moveToNext()) {
				data.add(new double[] { route.getDouble(latC),
						route.getDouble(lonC) });
			}
			int a = Math.min(srcord, dstord);
			int b = Math.max(srcord, dstord);

			for (int i = a; i < b; i++) {
				distance += GeoUtils.distVincenty(data.get(i)[0],
						data.get(i)[1], data.get(i + 1)[0], data.get(i + 1)[1]);
			}
		}
		return distance;
	}

	protected Cursor queryForNodes() {
		return databaseManager.query(true, edgesTable, new String[] {
				"first_id", "first_ord" }, null, null, null, null, null, null);
	}

	protected Cursor queryForEdges(long id, int ord) {
		String[] args = { Long.toString(id), Integer.toString(ord) };
		return databaseManager.rawQuery(selectEdgesSql, args);
	}

	protected List<RouteConnectionPoint> getConnectionPoints() {
		Cursor c = queryForNodes();
		List<RouteConnectionPoint> result = connMapper.toList(c);
		c.close();
		return result;
	}

	protected List<RouteConnectionPoint> getConnectionPoints(
			RouteConnectionPoint node) {
		Cursor c = queryForEdges(node.getRouteId(), node.getOrdinality());
		List<RouteConnectionPoint> result = connMapper.toList(c);
		c.close();
		return result;
	}

	protected void updateEdgeWeight(long idA, int ordA, long idB, int ordB,
			double weight) {

		String[] args = new String[4];
		args[0] = Long.toString(idA);
		args[1] = Integer.toString(ordA);
		args[2] = Long.toString(idB);
		args[3] = Integer.toString(ordB);

		ContentValues values = new ContentValues();
		values.put("distance", weight);

		databaseManager.update(edgesTable, values,
				"first_id=? AND first_ord=? AND second_id=? AND second_ord=?",
				args);
	}

	/**
	 * Create the edges
	 */
	public void createEdges() {
		// insert the edges into the database
		for (String sql : edgeCreationSqls) {
			databaseManager.execSQL(sql);
		}

		// calculate the distance between each nodes
		Set<RouteConnection> visited = new HashSet<RouteConnection>();

		for (RouteConnectionPoint src : getConnectionPoints()) {
			for (RouteConnectionPoint dst : getConnectionPoints(src)) {
				RouteConnection conn = new RouteConnection(src, dst);

				if (visited.add(conn)) {
					conn.distance = distance(src.getRouteId(),
							src.getOrdinality(), dst.getRouteId(),
							dst.getOrdinality());
					visited.add(conn.flip());
				}
			}

		}

		for (RouteConnection conn : visited) {
			updateEdgeWeight(conn.src.getRouteId(), conn.src.getOrdinality(),
					conn.dst.getRouteId(), conn.dst.getOrdinality(),
					conn.distance);
		}
	}

	@Override
	public void preprocess(CommandLine cmd) {
		createEdges();
	}
}
