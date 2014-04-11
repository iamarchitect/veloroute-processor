package app.velor.utils;

import android.database.Cursor;

public final class CursorUtils {

	private CursorUtils() {
	}

	public static int[] getInt(Cursor c, String column) {
		int[] result = new int[c.getCount()];

		int col = c.getColumnIndex(column);
		int i = 0;
		for (c.moveToFirst(); c.isAfterLast(); c.moveToNext(), i++) {
			result[i] = c.getInt(col);
		}

		return result;
	}

	public static long[] getLong(Cursor c, String column) {
		long[] result = new long[c.getCount()];

		int col = c.getColumnIndex(column);
		int i = 0;
		for (c.moveToFirst(); c.isAfterLast(); c.moveToNext(), i++) {
			result[i] = c.getLong(col);
		}

		return result;
	}

	public static double[] getDouble(Cursor c, String column) {
		double[] result = new double[c.getCount()];

		int col = c.getColumnIndex(column);
		int i = 0;
		for (c.moveToFirst(); c.isAfterLast(); c.moveToNext(), i++) {
			result[i] = c.getDouble(col);
		}

		return result;
	}

	public static float[] getFloat(Cursor c, String column) {
		float[] result = new float[c.getCount()];

		int col = c.getColumnIndex(column);
		int i = 0;
		for (c.moveToFirst(); c.isAfterLast(); c.moveToNext(), i++) {
			result[i] = c.getFloat(col);
		}

		return result;
	}

	public static String[] getString(Cursor c, String column) {
		String[] result = new String[c.getCount()];

		int col = c.getColumnIndex(column);
		int i = 0;
		for (c.moveToFirst(); c.isAfterLast(); c.moveToNext(), i++) {
			result[i] = c.getString(col);
		}

		return result;
	}

	public static byte[][] getBlob(Cursor c, String column) {
		byte[][] result = new byte[c.getCount()][];

		int col = c.getColumnIndex(column);
		int i = 0;
		for (c.moveToFirst(); c.isAfterLast(); c.moveToNext(), i++) {
			result[i] = c.getBlob(col);
		}

		return result;
	}
}
