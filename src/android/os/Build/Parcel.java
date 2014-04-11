package android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class Parcel {

	HashMap<String, Object> internal;

	public static Parcel obtain() {
		return new Parcel();
	}

	public void writeMap(Map<String, Object> map) {
		internal = new HashMap<String, Object>(map);
	}

	public void setDataPosition(int i) {
	}

	public void recycle() {
		internal = null;
	}

	public HashMap<String, Object> readHashMap(Object object) {
		return internal;
	}

}
