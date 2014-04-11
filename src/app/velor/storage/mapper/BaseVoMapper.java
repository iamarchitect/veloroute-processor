package app.velor.storage.mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.database.Cursor;

public abstract class BaseVoMapper<T> implements VOMapper<T> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see app.velor.storage.mapper.VOMapper#toList(android.database.Cursor)
	 */
	@Override
	public List<T> toList(Cursor c) {
		List<T> result = new ArrayList<T>();
		while (c.moveToNext()) {
			T row = map(c);
			result.add(row);
		}
		return result;
	}

	/**
	 * Actually do the mappings for getObject(Cursor c1, int i, String field).
	 * Hook method for subclasses to change how the mapping is made (like
	 * caching).
	 * 
	 * @param c1
	 * @param i
	 * @param mapper
	 * @return
	 */
	protected Object getObject(Cursor c, int i, VOMapper<?> mapper) {
		Object result = null;
		result = mapper.map(c);
		return result;
	}

	@Override
	public Iterator<T> toIterator(Cursor c) {
		return new CursorIterator<T>(c, this);
	}

	protected abstract T createObject();

	@Override
	public T map(Cursor c) {
		// instantiate a new object
		return map(c, createObject());
	}

}
