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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.velor.algorithms.geodata.LatLng;
import com.velor.algorithms.geodata.Projection;
import com.velor.algorithms.spatial.Point;
import com.velor.map.provider.route.RouteProvider;
import com.velor.map.provider.tile.Tile;
import com.velor.map.provider.tile.TileImpl;
import com.velor.map.provider.tile.TileProvider;
import com.velor.map.storage.tile.TileStorage;
import com.velor.map.vo.Route;

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

	private class PR {
		long start = 0;
		double total = 0;
		int pcent = 0;
		int current = 0;
		int pcent_ = 0;

		public PR() {
			super();
			start = new Date().getTime();
		}

		void update() {
			pcent = (int) (current / total * 100);

			if (pcent != pcent_) {
				// long time = new Date().getTime();
				// if (time - start > 2000) {
				if (pcent % 10 == 0) {
					System.out.print(pcent + "%");
					// start = time;
				} else {
					System.out.print(".");
				}
			}
			pcent_ = pcent;
			current++;
		}

		void update(int n) {
			current += n;
			pcent = (int) (current / total * 100);
			long time = new Date().getTime();

			if (pcent != pcent_ || time - start > 1000) {

				System.out.println(pcent + "% done");
				start = time;

			} else {
				System.out.print(".");
			}
			pcent_ = pcent;

		}
	}

	@Override
	public void preprocess() {

		tileStorate.setTileDirectory(destination);
		System.out.println("Rendering routes on tiles " + minzoom + "-" + maxzoom);

		File f = new File(destination);
		if (f.exists()) {
			try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		List<Route> list = routeProvider.getRoutes();
		PR pr = new PR();

		// for (int i = minzoom; i <= maxzoom; i++) {
		// pr.total += Math.pow(2, i) * list.size();
		// }

		final Map<String, Object> locks = new HashMap<String, Object>();
		pool = Executors.newFixedThreadPool(10);

		for (int zoom = minzoom; zoom <= maxzoom; zoom++) {
			
			// int ntiles = (int) Math.pow(2, zoom);
			for (Route route : list) {
				if (route.getType().getId() >= -1) {
					pr.total += render(route, zoom, pr, locks);
				}
			}
		}

		try {
			pool.shutdown();
			pool.awaitTermination(1, TimeUnit.DAYS);
			System.out.println("100%");
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

	protected int render(final Route route, final int zoom, final PR pr,
			final Map<String, Object> locks) {
		int n = route.size();
		final List<Point> xy = new ArrayList<>();

		float routeAlpha = this.routeAlpha;
		float routeWidth = this.routeWidth;

		if (zoom == minzoom) {
			routeAlpha /= 8;
			routeWidth /= 4;
		} else if (zoom == minzoom + 1) {
			routeAlpha /= 4;
			routeWidth /= 4;
		} else if (zoom == minzoom + 2) {
			routeAlpha /= 2;
			routeWidth /= 2;
		}

		Set<Point> tiles = new HashSet<>();
		for (int i = 0, j = 0; i < n; i++) {

			if (zoom >= route.minZoom[i] && zoom <= route.maxZoom[i]) {
				Point p = projection.toTile(new LatLng(route.data[i]), zoom);
				xy.add(p);
				tiles.add(new Point((int) xy.get(j).x, (int) xy.get(j).y));
				j++;
			}
		}

		// int ntiles = (int) Math.pow(2, zoom);
		// pr.update(ntiles-tiles.size());
		for (final Point tileCoord : tiles) {
			final float frouteAlpha = routeAlpha;
			final float frouteWidth = routeWidth;

			pool.execute(new Runnable() {

				@Override
				public synchronized void run() {

					int w = tilePixels;
					int h = tilePixels;
					Tile tile = null;
					BufferedImage img = null;
					// int prevX = -1, prevY = -1;
					int x = -1;
					int y = -1;
					Graphics2D g = null;

					int n = xy.size();
					int[] xPoints = new int[n];
					int[] yPoints = new int[n];

					x = (int) tileCoord.x;
					y = (int) tileCoord.y;
					for (int i = 0; i < n; i++) {
						xPoints[i] = (int) ((xy.get(i).x - x) * w);
						yPoints[i] = (int) ((xy.get(i).y - y) * h);
					}

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

							img = ImageIO.read(new ByteArrayInputStream(tile
									.getData()));
							g = (Graphics2D) img.getGraphics();
							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_ATOP, frouteAlpha));

							g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
									RenderingHints.VALUE_ANTIALIAS_ON);
							g.setRenderingHint(RenderingHints.KEY_RENDERING,
									RenderingHints.VALUE_RENDER_QUALITY);
							g.setRenderingHint(
									RenderingHints.KEY_ALPHA_INTERPOLATION,
									RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
							g.setRenderingHint(
									RenderingHints.KEY_COLOR_RENDERING,
									RenderingHints.VALUE_COLOR_RENDER_QUALITY);
							g.setRenderingHint(
									RenderingHints.KEY_FRACTIONALMETRICS,
									RenderingHints.VALUE_FRACTIONALMETRICS_ON);

							Color c = new Color(route.getType().getColor());
							g.setPaint(c);
							g.setStroke(new BasicStroke(frouteWidth,
									BasicStroke.CAP_ROUND,
									BasicStroke.JOIN_BEVEL));
							g.drawPolyline(xPoints, yPoints, n);

							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							ImageIO.write(img, "jpg", bos);
							tile = new TileImpl(bos.toByteArray(), w, h);
							tileStorate.create(tile, zoom, x, y);
							bos.close();
							pr.update();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});

			// ntiles--;
		}

		return tiles.size();

	}
}
