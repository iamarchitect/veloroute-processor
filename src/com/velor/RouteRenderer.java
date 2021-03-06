package com.velor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.velor.algorithms.geodata.LatLng;
import com.velor.algorithms.geodata.Projection;
import com.velor.algorithms.spatial.Point;
import com.velor.map.provider.route.RouteProvider;
import com.velor.map.provider.tile.TileProvider;
import com.velor.map.storage.tile.TileStorage;
import com.velor.map.vo.Route;
import com.velor.map.vo.Tile;
import com.velor.map.vo.TileImpl;

public class RouteRenderer extends AbstractPreprocessor {

	private TileProvider tileProvider;
	private RouteProvider routeProvider;
	private TileStorage tileStorate;
	private Projection projection;
	private float routeAlpha;
	private float routeWidth;
	private int tilePixels;
	private int minzoom = 9;
	private int maxzoom = 10;
	private String destination;
	private ExecutorService pool;
	private boolean onlyDownloadTiles;

	public void setOnlyDownloadTiles(boolean onlyDownloadTiles) {
		this.onlyDownloadTiles = onlyDownloadTiles;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void setMinzoom(int minzoom) {
		this.minzoom = minzoom;
	}

	public void setMaxzoom(int maxzoom) {
		this.maxzoom = maxzoom;
	}

	public void setTilePixels(int tilePixels) {
		this.tilePixels = tilePixels;
	}

	public void setTileProvider(TileProvider tileProvider) {
		this.tileProvider = tileProvider;
	}

	public void setRouteProvider(RouteProvider routeProvider) {
		this.routeProvider = routeProvider;
	}

	public void setTileStorate(TileStorage tileStorate) {
		this.tileStorate = tileStorate;
	}

	public void setProjection(Projection projection) {
		this.projection = projection;
	}

	public void setRouteAlpha(float routeAlpha) {
		this.routeAlpha = routeAlpha;
	}

	public void setRouteWidth(float routeWidth) {
		this.routeWidth = routeWidth;
	}

	@Override
	public void preprocess() {

		tileStorate.setTileDirectory(destination);
		System.out.println("Rendering routes on tiles " + minzoom + "-"
				+ maxzoom);

		File f = new File(destination);
		if (f.exists()) {
			try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		List<Route> list = routeProvider.getRoutes();
		ProgressReport pr = new ProgressReport();

		final Map<String, Object> locks = new HashMap<String, Object>();
		pool = Executors.newFixedThreadPool(10);

		for (int zoom = minzoom; zoom <= maxzoom; zoom++) {

			for (Route route : list) {
				if (route.getType().getId() >= -1) {
					pr.total += render(route, zoom, pr, locks);
				}
			}
		}

		try {
			pool.shutdown();
			pool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public class CompositeStroke implements Stroke {
		private Stroke stroke1, stroke2;

		public CompositeStroke(Stroke stroke1, Stroke stroke2) {
			this.stroke1 = stroke1;
			this.stroke2 = stroke2;
		}

		public Shape createStrokedShape(Shape shape) {
			return stroke2
					.createStrokedShape(stroke1.createStrokedShape(shape));
		}
	}

	private class Renderer {
		public Tile render(Tile tile, Route route, List<Point> xy)
				throws IOException {
			int n = xy.size();
			final float frouteAlpha = routeAlpha;
			final float frouteWidth = routeWidth;

			// hack to smoothly blend out the route when zooming out.
			// if (zoom == minzoom) {
			// frouteAlpha /= 8;
			// routeWidth /= 4;
			// } else if (zoom == minzoom + 1) {
			// routeAlpha /= 4;
			// routeWidth /= 4;
			// } else if (zoom == minzoom + 2) {
			// routeAlpha /= 2;
			// routeWidth /= 2;
			// }

			if (n == 1 || routeAlpha == 0) {
				return null;
			}

			int w = tilePixels;
			int h = tilePixels;

			int x = tile.getX();
			int y = tile.getY();

			int[] xPoints = new int[n];
			int[] yPoints = new int[n];
			// normalize the points at the tile coordinates
			for (int i = 0; i < n; i++) {
				xPoints[i] = (int) ((xy.get(i).x - x) * w);
				yPoints[i] = (int) ((xy.get(i).y - y) * h);
			}

			int y0 = yPoints[0];
			boolean allsamey = true;
			for (int i : yPoints) {
				allsamey &= i == y0;
			}

			boolean allsamex = true;
			int c0 = xPoints[0];
			for (int i : xPoints) {
				allsamex &= i == c0;
			}

			if (allsamex && allsamey) {
				return null;
			}

			BufferedImage img = ImageIO.read(new ByteArrayInputStream(tile
					.getData()));
			Graphics2D g = (Graphics2D) img.getGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					frouteAlpha));

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			
		
			
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			Color c = new Color(route.getType().getColor());
			g.setPaint(c);

			// g.setStroke(new CompositeStroke(new BasicStroke(frouteWidth,
			// BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND),
			// new BasicStroke(0.5f)));

			g.setStroke(new BasicStroke(frouteWidth + 2, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL));

			g.drawPolyline(xPoints, yPoints, n);

			g.setPaint(Color.WHITE);

			g.setStroke(new BasicStroke(frouteWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_BEVEL));
			g.drawPolyline(xPoints, yPoints, n);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", bos);
			bos.close();

			tile = new TileImpl(bos.toByteArray(), w, h);

			return tile;
		}
	}

	protected int render(final Route route, final int zoom,
			final ProgressReport pr, final Map<String, Object> locks) {
		int n = route.size();
		final List<Point> xy = new ArrayList<>();

		// get all tiles to be rendered for each points of the route
		Set<Point> tiles = new HashSet<>();

		// add the first point
		Point p = projection.toTile(new LatLng(route.head()), zoom);
		xy.add(p);
		// add the tile to be rendered
		tiles.add(new Point((int) p.x, (int) p.y));

		for (int i = 1, j = 1; i < n; i++) {
			if (zoom >= route.minZoom[i] && zoom <= route.maxZoom[i]) {
				p = projection.toTile(new LatLng(route.data[i]), zoom);
				xy.add(p);

				// add all the tiles intersecting the segment from on point to
				// another
				for (int x = (int) xy.get(j - 1).x; x <= (int) p.x; x++) {
					for (int y = (int) xy.get(j - 1).y; y <= (int) p.y; y++) {
						tiles.add(new Point(x, y));
					}
				}
				j++;
			}
		}

		// render the route on all tiles intersecting the route
		// FIXME clip the route for the tile to be rendered
//		final Semaphore sem = new Semaphore(20);
		for (final Point tileCoord : tiles) {
//
//			try {
//				sem.acquire();
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
			pool.execute(new Runnable() {

				@Override
				public synchronized void run() {
					Tile tile = null;
					int x = -1;
					int y = -1;

					x = (int) tileCoord.x;
					y = (int) tileCoord.y;

					try {
						File file = tileStorate.getStorageInfo().getTileFile(x,
								y, zoom);
						Object lock = null;
						synchronized (locks) {
							lock = locks.get(file.getAbsolutePath());

							if (lock == null) {
								lock = new Object();
								locks.put(file.getAbsolutePath(), lock);
							}
						}
						synchronized (lock) {
							tile = tileProvider.getTile(x, y, zoom);
							if (!onlyDownloadTiles) {
								tile = new Renderer().render(tile, route, xy);
								if (tile != null) {
									tileStorate.create(tile, zoom, x, y);
								}
							}
							pr.update();
						}
//						sem.release();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
			
		}

		return tiles.size();

	}
}
