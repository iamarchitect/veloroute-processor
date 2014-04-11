package app.velor;

// FIXME build a processor chain instead of protected methods
public class RoutePreprocessor {

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
		System.out.println("Merging exploded routes");
		routeMerger.mergeRoutes();
		System.out.println("Reducing vertex");
		routeReducer.reduceVertices();
		System.out.println("Creating eges");
		routeInterconnectionBuilder.createEdges();
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

}
