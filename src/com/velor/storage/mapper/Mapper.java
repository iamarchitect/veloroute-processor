package com.velor.storage.mapper;

public interface Mapper<T, K> {

	/**
	 * Map an object from a name to another name.
	 * 
	 * @param c1
	 * @return
	 */
	T map(K c);

	/**
	 * Map an object to another existing prototype.
	 * 
	 * @param c1
	 * @param dest
	 */
	T map(K c, T dest);
}
