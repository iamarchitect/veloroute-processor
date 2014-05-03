package com.velor;


// FIXME build a processor chain instead of protected methods
public class RoutePreprocessor {
	// private static final double halfpi = Math.PI / 2.0;
	// private static final int minLevelJoinHack = 11;
	// private static final double[] dist4Zoom = { 8e-4, // 11
	// 5.5e-4, // 12
	// 3e-4, // 13
	// 2e-4, // 14
	// 8e-5, // 15
	// 7e-5, // 16
	// 3e-5, // 17
	// 10e-6, // 18
	// 10e-6, // 19
	// 10e-6 // 20
	// };
	private RouteMerger routeMerger;
	private RouteReducer routeReducer;
	private RouteInterconnectionBuilder routeInterconnectionBuilder;

	public void setRouteMerger(RouteMerger routeMerger) {
		this.routeMerger = routeMerger;
	}

	public void setRouteReducer(RouteReducer routeReducer) {
		this.routeReducer = routeReducer;
	}

	public void setRouteInterconnectionBuilder(
			RouteInterconnectionBuilder routeInterconnectionBuilder) {
		this.routeInterconnectionBuilder = routeInterconnectionBuilder;
	}

	public void preprocess() {

		// FIXME not working properly, some unconnected routes still stays
		// reversed.
		// reorderVertices();
		// System.out.println("Merging exploded routes");
		// routeMerger.mergeRoutes();
		System.out.println("Reducing vertex");
		routeReducer.reduceVertices();
		System.out.println("Creating eges");
		routeInterconnectionBuilder.createEdges();
		System.out.println("Preparing system tables");
	}

	/**
	 * Reorder the vertices of the routes so that a connected route ends on the
	 * first vertex of the next route.
	 */
	protected void reorderVertices() {

		// String[] sqls = ApplicationManager.getInstance().getResources()
		// .getStringArray(R.array.reorder_ordinality_sql);
		//
		// String[] prepSqls = ApplicationManager.getInstance().getResources()
		// .getStringArray(R.array.create_temporary_routedata);
		//
		// for (String sql : prepSqls) {
		// routeDao.execSQL(sql);
		// }
		//
		// boolean finished = false;
		// int count = 0;
		// while (!finished) {
		// routeDao.beginTransaction();
		//
		// for (String sql : sqls) {
		// routeDao.execSQL(sql);
		// }
		// // FIXME hardcode
		// Cursor c = routeDao.rawQuery("SELECT COUNT(*) FROM visited;",
		// new String[] {});
		//
		// if (c.moveToFirst()) {
		// int n = c.getInt(0);
		// finished = n == count;
		// count = n;
		// }
		// c.close();
		// routeDao.endTransactionSuccessful();
		// }

	}
	// protected void jointHack(Route r, int zoom) {
	//
	// if (r.data == null || r.data.length == 1) {
	// return;
	// }
	//
	// double[] b = getCoordHackEnd(r, zoom);
	// double[] a = getCoordHackStart(r, zoom);
	//
	// List<double[]> list = new ArrayList<double[]>();
	// if (a != null) {
	// // put the new coord after the route_id coord
	// list.add(r.data[0]);
	// list.addAll(Arrays.asList(r.data));
	// list.set(2, a);
	// } else {
	// list.addAll(Arrays.asList(r.data));
	// }
	//
	// if (b != null) {
	// // put the new coord before the last coord
	// list.add(list.get(list.size() - 1));
	// list.set(list.size() - 2, b);
	// }
	// r.data = list.toArray(new double[list.size()][]);
	// }

	// protected double[] getCoordHackStart(Route r, int zoom) {
	// Long previous = routeDao.queryForPrevious(r.id);
	// if (previous == null) {
	// return null;
	// }
	//
	// double a[] = routeDao.queryForCoords(previous, -2, zoom);
	// if (a == null) {
	// return null;
	// }
	// double center[] = r.data[0];
	// double b[] = r.data[1];
	//
	// return getCoordHack(a, b, center, zoom, halfpi);
	// }

	// protected double[] getCoordHackEnd(Route r, int zoom) {
	// Long next = routeDao.queryForNext(r.id);
	// if (next == null) {
	// return null;
	// }
	//
	// double b[] = routeDao.queryForCoords(next, 1, zoom);
	// if (b == null) {
	// return null;
	// }
	// double center[] = r.data[r.data.length - 1];
	// double a[] = r.data[r.data.length - 2];
	//
	// return getCoordHack(a, b, center, zoom, -halfpi);
	// }

	// protected double[] getCoordHack(double[] a, double[] b, double[] center,
	// int zoom, double rot) {
	// b = b.clone();
	// a = a.clone();
	//
	// subtract(a, center);
	// subtract(b, center);
	//
	// VertexReducer.normalize(a);
	// VertexReducer.normalize(b);
	//
	// double rotation = Math.acos(VertexReducer.dot(a, b)) / 2;
	// if (Double.isNaN(rotation) || halfpi - Math.abs(rotation) < 0.2) {
	// return null;
	// }
	//
	// rotate(rotation + rot, a);
	//
	// scale(dist4Zoom[zoom - minLevelJoinHack], a);
	// translate(a, center);
	//
	// return a;
	// }

	// protected double[] scale(double scale, double[] a) {
	// a[0] *= scale;
	// a[1] *= scale;
	// return a;
	// }

	// protected double[] rotate(double theta, double[] a) {
	// double cos = Math.cos(theta);
	// double sin = Math.sin(theta);
	//
	// double y = a[0];
	// double x = a[1];
	// a[1] = cos * x - sin * y;
	// a[0] = sin * x + cos * y;
	//
	// return a;
	// }

	// protected double[] subtract(double[] a, double[] b) {
	// a[0] -= b[0];
	// a[1] -= b[1];
	// return a;
	// }

	// protected double[] translate(double[] a, double[] b) {
	// a[0] += b[0];
	// a[1] += b[1];
	// return a;
	// }
}
