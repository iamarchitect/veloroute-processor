package app.velor.utils;

public class VertexReducer {

	/**
	 * The O(n) Reumann-Witkam routine uses a point-to-line (perpendicular)
	 * weight tolerance. It defines a line through the route_id two vertices of
	 * the original polyline. For each successive vertex vi, its perpendicular
	 * weight to this line is calculated. A new key is found at vi-1, when this
	 * weight exceeds the specified tolerance. The vertices vi and vi+1 are then
	 * used to define a new line, and the process repeats itself.
	 * 
	 * @return A zoom level array where each element i indicates at witch zoom
	 *         level the vertex i is starting to be shown (when vertexZoom <=
	 *         actualZoom, the vertex is visible on the map).
	 * @param polyline
	 * @see <a
	 *      href="http://www.codeproject.com/Articles/114797/Polyline-Simplification#headingRW">Reumann-Witkam</a>
	 */
	public static int[] reduce(double[][] polyline, double tolerance, int zoom,
			int[] indices) {
		int n = polyline.length;

		// start with the route_id coord as key1
		double[] a = polyline[0];

		// key2 is determined as the next coord respecting tolerance
		double[] b = null;

		int i;
		for (i = 0; i < n; i++) {
			b = polyline[i];
			if (distance(a, b) > tolerance) {
				indices[i] = zoom;
				break;
			}
		}

		// now run the Reumann-Witkam algorithm with the remaining vertices
		for (int j = i + 1; j < n; j++) {
			double[] p = polyline[j];
			if (distance(p, a, b) > tolerance) {
				indices[j] = zoom;
				a = polyline[i];
				b = polyline[j];
				i = j;
			}
		}

		return indices;
	}

	/**
	 * Distance between two coords.
	 * 
	 * @param p
	 * @param a
	 * @param b
	 * @return
	 */
	public static double distance(double[] a, double[] b) {
		double dx = b[0] - a[0];
		double dy = b[1] - a[1];
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Distance between a coord and a infinite line from two coords.
	 * 
	 * @param point
	 * @param seg1
	 * @param seg2
	 * @return
	 */
	public static double distance(double[] p, double[] a, double[] b) {
		double[] v = vect(b[0], b[1], a[0], a[1]);
		double[] r = vect(p[0], p[1], a[0], a[1]);
		return Math.abs(dot(normalize(v), r));
	}

	/**
	 * Dot product of two vectors
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double dot(double[] a, double[] b) {
		return a[0] * b[0] + a[1] * b[1];
	}

	/**
	 * Non normalized normal of vector.
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @return
	 */
	public static double[] normal(double[] vec) {
		double tmp = vec[0];
		vec[0] = vec[1];
		vec[1] = -tmp;
		return vec;
	}

	/**
	 * Norm of vector.
	 * 
	 * @param vec
	 * @return
	 */
	public static double[] normalize(double[] vec) {
		double l = Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1]);
		vec[0] /= l;
		vec[1] /= l;
		return vec;
	}

	/**
	 * Vector from two points
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @return
	 */
	public static double[] vect(double ax, double ay, double bx, double by) {
		double[] result = new double[2];
		result[0] = bx - ax;
		result[1] = by - ay;
		return result;
	}

	/**
	 * Projection of vector CA into vector CB given start-point "c" for the two
	 * vectors, end-point "a" of vector CA and end-point "b" of vector CB.
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @param cx
	 * @param cy
	 * @return
	 */
	public static double[] projection(double ax, double ay, double bx,
			double by, double cx, double cy) {

		double k;

		double[] ca = vect(cx, cy, ax, ay);
		double[] cb = vect(cx, cy, bx, by);

		k = dot(ca, cb) / (cb[0] * cb[0] + cb[1] * cb[1]);

		double[] result = new double[3];
		result[0] = cb[0];
		result[1] = cb[1];

		result[0] *= k;
		result[1] *= k;

		result[0] += cx;
		result[1] += cy;

		result[2] = k;
		return result;
	}

}
