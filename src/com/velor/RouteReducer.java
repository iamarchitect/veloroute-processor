package com.velor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;

import com.velor.algorithms.geodata.LatLng;
import com.velor.algorithms.geodata.Projection;
import com.velor.algorithms.polyline.VertexReducerUtils;
import com.velor.algorithms.spatial.Point;
import com.velor.storage.database.Cursor;

/**
 * Reduces the vertex of route polylines for each zoom levels from level 5 to
 * level 17.
 * 
 * @author glenn-eric
 * 
 */
public class RouteReducer extends AbstractPreprocessor {
	private static final int MIN_ZOOM = 5;
	public static final String DB_MIN_ZOOM = "min_zoom";
	public static final String DB_MAX_ZOOM = "max_zoom";

	private DatabaseManager databaseManager;
	private String routesTable;
	private String routedataTable;
	private Projection projection;
	private int tolerance;

	public void setRouteWidth(int routeWidth) {
		this.tolerance = routeWidth;
	}

	public void setProjection(Projection projection) {
		this.projection = projection;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setRoutesTable(String routesTable) {
		this.routesTable = routesTable;
	}

	public void setRoutedataTable(String routedataTable) {
		this.routedataTable = routedataTable;
	}

	public Cursor queryRouteIds() {
		return databaseManager.query(routesTable, new String[] { "id" }, null,
				null, null, null, null);
	}

	public Cursor queryRoute(long id) {
		return databaseManager.query(routedataTable, null, "route_id=?",
				new String[] { Long.toString(id) }, null, null, "ordinality");
	}

	/**
	 * Magic method to determine the tolerance of the algorithm for a given zoom
	 * level.
	 * 
	 * @param zoom
	 * @return
	 */
	protected double toleranceForZoom(int zoom) {

		double sqZ = zoom * zoom;
		double tolerance = 360 / (256 * sqZ * sqZ);

		if (zoom <= 8) {
			tolerance *= 200;
		} else if (zoom <= 9) {
			tolerance *= 20;
		} else if (zoom <= 10) {
			tolerance *= 15;
		} else {
			tolerance *= 10;
		}

		return tolerance;
	}

	public void updateRouteZoom(long id, int[] minZooms, int[] maxZooms) {
		ContentValues row = new ContentValues();
		int n = minZooms.length;
		for (int i = 0; i < n; i++) {
			String[] args = { Long.toString(id), Integer.toString(i) };
			row.put(DB_MIN_ZOOM, minZooms[i]);
			row.put(DB_MAX_ZOOM, maxZooms[i]);
			databaseManager.update(routedataTable, row,
					"route_id=? AND ordinality=?", args);
		}
	}

	/**
	 * Apply vertex reduction to routes
	 */
	protected void reduceVertices() {
		Cursor c = queryRouteIds();

		while (c.moveToNext()) {

			long id = c.getLong(1);
			Cursor route = queryRoute(id);

			int latC = route.getColumnIndex("latitude");
			int lonC = route.getColumnIndex("longitude");
			List<double[]> data = new ArrayList<double[]>();

			while (route.moveToNext()) {
				data.add(new double[] { route.getDouble(latC),
						route.getDouble(lonC) });
			}

			int m = data.size();

			// pre-calculate the zoom levels for which every vertex is shown
			int[] minZooms = new int[m];
			int[] maxZooms = new int[m];

			// vertices are shown if : minZoom < zoom < maxZoom
			// far constraint (when zooming out) : can only show from beyond 17
			Arrays.fill(minZooms, 17);

			// near constraints (when zooming in) : no constraints for points
			Arrays.fill(maxZooms, 20);

			// seek for minimum zooms
			for (int zoom = 16; zoom >= MIN_ZOOM; zoom--) {
				List<double[]> points = new ArrayList<double[]>();
				for (double[] datum : data) {
					Point p = projection.toPoint(
							new LatLng(datum[0], datum[1]), zoom);
					points.add(new double[] { p.x, p.y });
				}

				VertexReducerUtils.reumannWitkam(
						points.toArray(new double[][] {}), tolerance, zoom,
						minZooms);
			}

			minZooms[0] = MIN_ZOOM;
			minZooms[minZooms.length - 1] = MIN_ZOOM;

			// update the zooms
			updateRouteZoom(id, minZooms, maxZooms);

		}

		c.close();
	}

	@Override
	public void preprocess() {
		reduceVertices();
	}
}
