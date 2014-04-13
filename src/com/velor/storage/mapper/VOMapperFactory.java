package com.velor.storage.mapper;

import com.velor.storage.database.Cursor;
import com.velor.storage.mapper.VOMapperFactory.ObjectBuilder;

/**
 * Mapper factory to create mappers from sqlite database Curosor to variable
 * objects (VO) commonly named Plain Old Java Objects (POJO).
 * 
 * @author Glenn Hall
 * 
 * 
 */
public final class VOMapperFactory<DestType> extends
		MapperFactory<DestType, Cursor, Class<DestType>, ObjectBuilder> {

	private static VOMapperFactory<?> factory;

	/**
	 * Returns the unique instance of the factory.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <DestType> VOMapperFactory<DestType> getInstance() {
		if (factory == null) {
			factory = new VOMapperFactory<DestType>();
		}

		return (VOMapperFactory<DestType>) factory;
	}

	/**
	 * Helper class to map Cursor to pojo
	 * 
	 * @author Glenn Hall
	 * 
	 * 
	 */
	public static interface ObjectBuilder {
		Object createObject(Cursor c, int i, VOMapper<?> mapper);

		Object createMappedObject();
	}

	/**
	 * 
	 * @author Glenn Hall
	 * 
	 * 
	 * @param <T>
	 */
	private static class MapperEx<DestType> extends VOMapperImpl<DestType> {
		private ObjectBuilder builder;

		protected MapperEx(String idColumnName, Class<DestType> clas,
				ObjectBuilder builder) {
			super(idColumnName, clas);
			this.builder = builder;
		}

		@Override
		protected Object getObject(Cursor c, int i, VOMapper<?> mapper) {
			Object result = builder.createObject(c, i, mapper);
			if (result == null) {
				result = super.getObject(c, i, mapper);
			}
			return result;
		}

		@Override
		protected DestType createObject() {
			DestType result = (DestType) builder.createMappedObject();
			return result != null ? result : super.createObject();
		}

	}

	private static final String DEFAULT_ID_COLUMN = "id";

	@Override
	protected Mapper<DestType, Cursor> createMapper(Class<DestType> mappings) {
		VOMapper<DestType> result = new VOMapperImpl<DestType>(
				DEFAULT_ID_COLUMN, mappings);
		return result;
	}

	@Override
	protected Mapper<DestType, Cursor> createMapper(Class<DestType> mappings,
			ObjectBuilder helper) {
		return new VOMapperFactory.MapperEx<DestType>(DEFAULT_ID_COLUMN,
				mappings, helper);
	}

}
