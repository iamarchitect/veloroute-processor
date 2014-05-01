package com.velor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;

import com.velor.map.storage.tile.TileKeyGenerator;
import com.velor.storage.database.Cursor;

public class TileListPreprocessor implements Preprocessor {

	private String fileListUrl;
	private String saveDir;
	private DatabaseManager databaseManager;
	private String tileTable;
	private TileKeyGenerator tileKeyGenerator;
	private String selectSameTilesSql;
	private String updateSameTilesSql;

	public void setSelectSameTilesSql(String selectSameTilesSql) {
		this.selectSameTilesSql = selectSameTilesSql;
	}

	public void setUpdateSameTilesSql(String updateSameTilesSql) {
		this.updateSameTilesSql = updateSameTilesSql;
	}

	public void setFileListUrl(String fileListUrl) {
		this.fileListUrl = fileListUrl;
	}

	public void setTileKeyGenerator(TileKeyGenerator tileKeyGenerator) {
		this.tileKeyGenerator = tileKeyGenerator;
	}

	public void setSaveDir(String saveDir) {
		this.saveDir = saveDir;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setTileTable(String tileTable) {
		this.tileTable = tileTable;
	}

	@Override
	public void preprocess() {
		System.out.println("Downloading tile list file");
		// downloadFile();

		System.out.println("parsing tile list");
		parseFile();

		System.out.println("Optimizing tiles");
		updateTiles();
	}

	protected void updateTiles() {
		Cursor c = databaseManager
				.rawQuery(selectSameTilesSql, new String[] {});

		int hashCol = c.getColumnIndex("hash");
		List<String> sameHash = new ArrayList<String>();
		while (c.moveToNext()) {
			String hash = c.getString(hashCol);
			sameHash.add(hash);
		}

		for (String hash : sameHash) {
			databaseManager.execSQL(updateSameTilesSql, new String[] { hash });
		}
	}

	protected void parseFile() {
		String fileName = saveDir + File.separator
				+ fileListUrl.substring(fileListUrl.lastIndexOf('/'));

		Path path = Paths.get(fileName);
		try (BufferedReader reader = Files.newBufferedReader(path,
				StandardCharsets.UTF_8)) {
			String line = null;
			ProgressReport pr = new ProgressReport();
			pr.total = new File(fileName).length();
			while ((line = reader.readLine()) != null) {
				pr.update(line.length());

				// Parameters are separated by spaces
				// file connections name, file size, file hash
				String[] tokens = line.split(" ");

				int x = Integer.parseInt(tokens[0]);
				int y = Integer.parseInt(tokens[1]);
				int zoom = Integer.parseInt(tokens[2]);
				String url = tokens[3];
				int size = Integer.parseInt(tokens[4]);
				String hash = tokens[5];

				ContentValues row = new ContentValues();
				row.put("id", tileKeyGenerator.tileKey(zoom, x, y));
				row.put("url", url);
				row.put("hash", hash);

				databaseManager.createOrReplace(tileTable, row);

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println();
	}

	protected void downloadFile() {
		int length = 0;

		URL url;
		ProgressReport pr = new ProgressReport();

		try {
			url = new URL(fileListUrl);
			HttpURLConnection connexion = (HttpURLConnection) url
					.openConnection();
			connexion.setRequestMethod("HEAD");
			length = connexion.getContentLength();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("total length of files is " + length + " bytes");
		pr.total = length;

		InputStream is = null;
		FileOutputStream fos = null;
		HttpURLConnection connexion = null;

		long debut = new Date().getTime();

		try {
			System.out.println("downloading " + fileListUrl);
			url = new URL(fileListUrl);

			File result = null;
			String fileName = url.getFile().substring(
					url.getFile().lastIndexOf('/'));

			result = new File(saveDir + fileName);

			connexion = (HttpURLConnection) url.openConnection();
			connexion.setRequestMethod("GET");
			connexion.connect();

			// create the file in the save directory
			is = connexion.getInputStream();
			fos = new FileOutputStream(result);

			byte data[] = new byte[1024];

			int count = 0;

			// download the data
			while ((count = is.read(data)) != -1) {

				fos.write(data, 0, count);
				pr.update(count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			if (connexion != null) {
				connexion.disconnect();
			}
		}
		System.out.println();
		System.out.println("downloading finished in "
				+ (new Date().getTime() - debut) + " milliseconds");
	}

}
