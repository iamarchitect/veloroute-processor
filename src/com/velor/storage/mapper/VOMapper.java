package com.velor.storage.mapper;

import java.util.Iterator;
import java.util.List;

import com.velor.storage.database.Cursor;

public interface VOMapper<T> extends Mapper<T, Cursor> {

	void setId(T row, long id);

	List<T> toList(Cursor c);

	Iterator<T> toIterator(Cursor c);

	Class<T> getVoClass();
}
