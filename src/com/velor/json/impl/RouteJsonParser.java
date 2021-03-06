package com.velor.json.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.velor.DatabaseManager;
import com.velor.algorithms.geodata.GeoUtils;
import com.velor.json.DefaultJsonParser;

public class RouteJsonParser extends DefaultJsonParser {

	private String routeDataTable;
	private String routesTable;

	public RouteJsonParser(DatabaseManager databaseManager) {
		super(databaseManager, null, null);
	}

	public void setRouteDataTable(String routeDataTable) {
		this.routeDataTable = routeDataTable;
	}

	public void setRoutesTable(String routesTable) {
		this.routesTable = routesTable;
	}

	public long createRoute(long typeId) {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("type_id", typeId);
		long[] result = databaseManager.create(routesTable, row);
		return result[0];
	}

	public int updateRouteLength(long id, double length) {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("route_length", length);
		int result = databaseManager.update(routesTable, row, "id=?",
				new String[] { Long.toString(id) });
		return result;
	}

	@Override
	protected void handleNextValue(Map<String, Object> map) {

		long typeId = -1;
		Double value = (Double) map.get("route_section");
		try {
			typeId = value != null ? value.longValue() : -1;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		List<List<List<Double>>> wowList = (List<List<List<Double>>>) map
				.get("lines");
		int m = wowList.size();
		Map<String, Object> values = new HashMap<String, Object>();

		// temporary variables for route length calculation
		double prevlng = wowList.get(0).get(0).get(0);
		double prevlat = wowList.get(0).get(0).get(1);

		for (int i = 0; i < m; i++) {

			long routeId = createRoute(typeId);

			int n = wowList.get(i).size();
			double length = 0;

			values.clear();
			for (int j = 0; j < n; j++) {
				double lng = wowList.get(i).get(j).get(0);
				double lat = wowList.get(i).get(j).get(1);
				length += GeoUtils.distVincenty(prevlat, prevlng, lat, lng);
				prevlng = lng;
				prevlat = lat;

				values.put("longitude", lng);
				values.put("latitude", lat);
				values.put("route_id", routeId);
				values.put("ordinality", j);

				databaseManager.create(routeDataTable, values);
			}

			// update the route with its pre-calculated length
			updateRouteLength(routeId, length);

		}
	}
}
