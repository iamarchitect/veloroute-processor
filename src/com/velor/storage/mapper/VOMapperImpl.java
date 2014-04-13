package com.velor.storage.mapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.velor.storage.database.Cursor;

// Why not use an ORM like ORMLite or use annotations
// http://stackoverflow.com/questions/7417426/why-are-annotations-under-android-such-a-performance-issue-slow
// also, for each row, every foreign key is duplicated even if it refers to the same object
class VOMapperImpl<T> extends BaseVoMapper<T> {

	/**
	 * Used to represent the types used in the fields of the POJO and the
	 * columns of the database.
	 * 
	 */
	private enum TYPES {
		BLOB, FLOAT, DOUBLE, INTEGER, LONG, STRING, OBJECT
	};

	private String idColumnName;

	/*
	 * Mappings to map from database column names to pojo's field names
	 */
	private Map<String, String> mappingsFromDB = new HashMap<String, String>();

	/*
	 * Mappings to map from pojo's field names to database column names
	 */
	private Map<String, String> mappingsFromObject = new HashMap<String, String>();

	/*
	 * The name of each columns (fields)
	 */
	private Map<String, TYPES> typeMappings = new HashMap<String, TYPES>();

	/*
	 * Cache the Fields objects so we don't look for them every time we need to
	 * access them.
	 */
	private Map<String, Field> fieldMappings = new HashMap<String, Field>();

	/*
	 * The class name of the parameter name.
	 */
	private Class<T> clas = null;

	protected VOMapperImpl(String idColumnName, Class<T> clas) {
		super();

		this.clas = clas;
		List<String> columns = new ArrayList<String>();
		List<String> fields = new ArrayList<String>();

		// fetch the column names and their name declarations
		for (Field field : getParameterType().getDeclaredFields()) {
			DatabaseMapping column = field.getAnnotation(DatabaseMapping.class);
			if (column != null) {
				String cname = column.name();
				columns.add(cname.equals("") ? null : cname);
				fields.add(field.getName());

				Class<?> type = field.getType();
				if (type.equals(String.class)) {
					typeMappings.put(field.getName(), TYPES.STRING);
				}

				else if (type.equals(Integer.class) || type.equals(int.class)) {
					typeMappings.put(field.getName(), TYPES.INTEGER);
				}

				else if (type.equals(Double.class) || type.equals(double.class)) {
					typeMappings.put(field.getName(), TYPES.DOUBLE);
				}

				else if (type.equals(Long.class) || type.equals(long.class)) {
					typeMappings.put(field.getName(), TYPES.LONG);
				}

				else if (type.equals(Float.class) || type.equals(float.class)) {
					typeMappings.put(field.getName(), TYPES.FLOAT);
				}

				else if (type.equals(byte[].class)) {
					typeMappings.put(field.getName(), TYPES.BLOB);
				}

				else {
					typeMappings.put(field.getName(), TYPES.OBJECT);
				}

				fieldMappings.put(field.getName(), field);
			}
		}

		String[] columnNames = columns.toArray(new String[] {});
		String[] fieldNames = fields.toArray(new String[] {});

		initialize(idColumnName, fieldNames, columnNames);
	}

	private Class<T> getParameterType() {
		return clas;

	}

	/**
	 * Instantiate a new object from the name parameter
	 * 
	 * @return
	 */
	protected T createObject() {
		T result = null;
		Class<T> clas = getParameterType();
		try {
			result = clas.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Initialize the internal mappings.
	 * 
	 * @param idColumnName
	 * @param fields
	 * @param columns
	 */
	private void initialize(String idColumnName, String[] fields,
			String[] columns) {
		this.idColumnName = idColumnName;

		for (int i = 0; i < fields.length; i++) {
			mappingsFromDB.put(columns[i], fields[i]);
			mappingsFromObject.put(fields[i], columns[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see app.velor.storage.mapper.VOMapper#setId(java.lang.Object, long)
	 */
	@Override
	public void setId(T row, long id) {
		try {
			Field f = row.getClass().getDeclaredField(idColumnName);
			boolean b = f.isAccessible();
			f.setAccessible(true);
			f.set(row, id);
			f.setAccessible(b);

		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException ignored) {
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Maps a database cursor to a plain old java object (POJO) of the name
	 * specified in constructor.
	 */
	@Override
	public T map(Cursor c, T result) {
		if (!c.hasCurrent()) {
			return null;
		}

		Field f;
		String column;
		Object value;
		TYPES type;

		// check for every fields if an mapping exist in the cursor.
		for (String field : mappingsFromObject.keySet()) {
			column = mappingsFromObject.get(field);
			int i = column != null ? c.getColumnIndex(column) : -1;
			value = null;

			if (i < 0 && column != null) {
				continue;
			}

			type = typeMappings.get(field);

			switch (type) {
			case BLOB:
				value = c.getBlob(i);
				break;
			case DOUBLE:
				value = c.getDouble(i);
				break;
			case FLOAT:
				value = c.getFloat(i);
				break;
			case INTEGER:
				value = c.getInt(i);
				break;
			case LONG:
				value = c.getLong(i);
				break;
			case STRING:
				value = c.getString(i);
				break;
			case OBJECT:
				value = getObject(c, i, field);
				break;
			default:
				break;
			}

			try {
				f = fieldMappings.get(field);
				boolean b = f.isAccessible();
				f.setAccessible(true);
				f.set(result, value);
				f.setAccessible(b);
			} catch (IllegalAccessException ignored) {
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

		}

		return result;
	}

	/**
	 * Create an object to be injected into a field. This uses another mapper to
	 * map the values in the cursor into the fields of another POJO.
	 * 
	 * @param c1
	 * @param i
	 * @param field
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Object getObject(Cursor c, int i, String field) {
		Object result = null;

		VOMapper<?> mapper = (VOMapper<?>) VOMapperFactory.getInstance()
				.getMapper((Class<Object>) fieldMappings.get(field).getType());
		result = getObject(c, i, mapper);
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
	public Class<T> getVoClass() {
		return getParameterType();
	}
}
