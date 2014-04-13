package com.velor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.velor.json.AbstractJsonParser;
import com.velor.json.JsonParser;

public class GeoJson2VelorJson extends AbstractJsonParser {

	private File input;
	private File output;
	private Gson outgson;
	private List<Street> streets;

	public static class VelorPath {
		public Street[] data;
		public Schema schema;
	}

	public static class Schema {
		public String city = "INTEGER";
		public String id = "INTGER";
		public String kml_layer = "TEXT";
		public String lines = "MULTILINE";
		public String reoute_section = "INTEGER";
	}

	public static class Street {
		static long ids = 0;
		public long id = ids++;
		public long city = -1;
		public long route_section = -2;
		public String kml_layer = "highway";
		public String name;
		public double[][][] lines;
	}

	public static void main(String[] args) throws Exception {

		JsonParser parser = new GeoJson2VelorJson();
		parser.parseJSON(new File(args[0]));
	}

	@Override
	public boolean parseJSON(File f) throws Exception {
		return super.parseJSON(this.input = f);
	}

	@Override
	protected void closeReader(JsonReader reader) throws IOException {
		reader.endArray();
		reader.endObject();
		reader.close();
	}

	@Override
	protected void prepareReader(JsonReader reader) throws IOException {
		reader.beginObject();

		while (reader.hasNext()) {
			String name = reader.nextName();

			if ("features".equals(name)) {
				reader.beginArray();
				break;
			} else {
				String value = reader.nextString();
				System.out.println("skipping " + name + " : " + value);
			}
		}

	}

	@Override
	protected void prepareParsing() {
		streets = new ArrayList<>();
		output = new File(input.getPath().replace(input.getName(),
				input.getName() + ".velor"));

		outgson = new Gson();

	}

	@Override
	protected void markAsSuccess() {
		VelorPath outvo = new VelorPath();
		outvo.data = streets.toArray(new Street[] {});
		outvo.schema = new Schema();
		String s = gson.toJson(outvo);

		try {
			FileOutputStream outputStream = new FileOutputStream(output);
			outputStream.write(s.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void markAsFailure() {
		// try {
		// fos.close();
		// output.delete();
		// } catch (IOException e) {
		// throw new RuntimeException(e);
		// }
	}

	@Override
	protected void handleNextValue(JsonReader reader) {
		Street s = new Street();
		try {
			reader.beginObject();
			String name = "";
			String value = "";

			while (reader.hasNext()) {
				name = reader.nextName();

				if ("geometry".equals(name)) {
					s.lines = new double[][][] { processCoordinates(reader) };
				} else if ("properties".equals(name)) {
					s.name = processPorperties(reader);
				} else {
					value = reader.nextString();
					System.out.println("skipping " + name + " : " + value);
				}
			}

			reader.endObject();
			streets.add(s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	double[][] processCoordinates(JsonReader reader) throws Exception {
		reader.beginObject();

		List<double[]> coordinates = new ArrayList<>();
		while (reader.hasNext()) {
			String name = reader.nextName();

			if ("coordinates".equals(name)) {
				reader.beginArray();// coordinates

				while (reader.hasNext()) {
					reader.beginArray();
					double lng = reader.nextDouble();
					double lat = reader.nextDouble();
					coordinates.add(new double[] { lng, lat });
					reader.endArray();

					System.out.println("lat:" + lat + " lng:" + lng);
				}
				reader.endArray(); // end array
			} else {
				String value = reader.nextString();
				System.out.println("skipping " + name + " : " + value);
			}
		}

		reader.endObject();

		return coordinates.toArray(new double[][] {});
	}

	String processPorperties(JsonReader reader) throws Exception {
		reader.beginObject();
		String name = "";
		String value = "";
		while (reader.hasNext()) {
			name = reader.nextName();
			if ("name".equals(name)) {
				value = reader.nextString();
				System.out.println("reading " + name + " : " + value);
			} else {
				String v = reader.nextString();
				System.out.println("skipping " + name + " : " + v);
			}
		}
		reader.endObject();
		return value;
	}
}
