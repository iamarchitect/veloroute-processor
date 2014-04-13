package com.velor.storage.mapper;

import java.util.Iterator;

import com.velor.storage.database.Cursor;

public class CursorIterator<T> implements Iterator<T> {

	private Cursor cursor;

	private Mapper<T, Cursor> mapper;

	protected CursorIterator(Cursor cursor, Mapper<T, Cursor> mapper) {
		super();
		this.cursor = cursor;
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		// return cursor.getPosition() < cursor.getCount();
		return !cursor.isLast() && !cursor.isAfterLast();
	}

	@Override
	public T next() {
		cursor.moveToNext();
		return mapper.map(cursor);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
