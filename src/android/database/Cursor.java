package android.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Cursor {

	private ResultSet resultSet;
	private boolean success = false;

	public Cursor(ResultSet resultSet) {
		super();
		this.resultSet = resultSet;
	}

	public boolean hasCurrent() {
		return success;
	}

	public boolean isAfterLast() {
		boolean result = false;
		try {
			result = resultSet.isAfterLast();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean isLast() {
		boolean result = false;
		try {
			result = resultSet.isLast();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean moveToNext() {
		try {
			return success = resultSet.next();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean moveToFirst() {
		boolean result = false;
		try {
			result = resultSet.first();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public double getDouble(int i) {
		double result = 0.0;
		try {
			result = resultSet.getDouble(i);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public void close() {
		try {
			resultSet.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int getColumnIndex(String string) {
		int result = -1;
		try {
			result = resultSet.findColumn(string);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public byte[] getBlob(int data) {
		byte[] result = null;
		try {
			result = resultSet.getBlob(data).getBytes(1, Integer.MAX_VALUE);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public int getCount() {
		int result = -1;
		try {
			result = resultSet.getFetchSize();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public String getString(int data) {
		String result = null;
		try {
			result = resultSet.getString(data);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public long getLong(int i) {
		long result = -1;
		try {
			result = resultSet.getLong(i);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public float getFloat(int i) {
		float result = -1;
		try {
			result = resultSet.getFloat(i);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public int getInt(int i) {
		int result = -1;
		try {
			result = resultSet.getInt(i);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

}
