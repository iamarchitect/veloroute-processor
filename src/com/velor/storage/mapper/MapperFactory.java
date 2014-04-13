package com.velor.storage.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper factory creating mappers and caching them for later use.
 * 
 * @author Glenn Hall
 * 
 * 
 * @param <DestType>
 * @param <SrcType>
 * @param <MappingType>
 * @param <HelperType>
 */
public abstract class MapperFactory<DestType, SrcType, MappingType, HelperType> {

	/*
	 * Mapper cache.
	 */
	private Map<MappingType, Mapper<DestType, SrcType>> mapperCache = new HashMap<MappingType, Mapper<DestType, SrcType>>();

	/**
	 * Creates a mapper if not in cache and puts it in cache. Returns the mapper
	 * from cache if it exists.
	 * 
	 * @param mappings
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Mapper<DestType, SrcType>> T getMapper(
			MappingType mappings) {
		Mapper<DestType, SrcType> result = null;
		if (mappings != null && !mapperCache.containsKey(mappings)) {
			result = createMapper(mappings);
			mapperCache.put(mappings, result);
		}
		result = mapperCache.get(mappings);
		return (T) result;
	}

	/**
	 * Gets a mapper backed by a mapper helper for resolving complex mappings.
	 * Caches mappers only on mapper name, so using a different helper from a
	 * previousBounds call with same mappings will return previousBounds mapper with
	 * previousBounds helper.
	 * 
	 * @param mappings
	 * @param helper
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Mapper<DestType, SrcType>> T getMapper(
			MappingType mappings, HelperType helper) {
		Mapper<DestType, SrcType> result = null;
		if (!mapperCache.containsKey(mappings)) {
			result = createMapper(mappings, helper);
			mapperCache.put(mappings, result);
		}
		result = (Mapper<DestType, SrcType>) mapperCache.get(mappings);
		return (T) result;
	}

	/**
	 * Clears the cache.
	 */
	public void flushCache() {
		mapperCache.clear();
	}

	protected abstract Mapper<DestType, SrcType> createMapper(
			MappingType mappings);

	protected abstract Mapper<DestType, SrcType> createMapper(
			MappingType mappings, HelperType helper);

}
