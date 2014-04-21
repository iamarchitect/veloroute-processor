package com.velor.json;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import com.velor.Preprocessor;

public class JsonDownloader implements Preprocessor {

	private String jsonurls;
	private String mediaUrl;
	private String[] jsons;
	private String destinationFolder;

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public String[] getJsons() {
		return jsons;
	}

	public void setJsons(String[] jsons) {
		this.jsons = jsons;
	}

	public String getJsonurls() {
		return jsonurls;
	}

	public void setJsonurls(String jsonurls) {
		this.jsonurls = jsonurls;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public void downloadJSons(String saveDir) {

		int length = 0;
		for (String json : jsons) {
			URL url;
			try {
				url = new URL(jsonurls.replace("?{table}", json));
				HttpURLConnection connexion = (HttpURLConnection) url
						.openConnection();
				connexion.setRequestMethod("HEAD");
				length += connexion.getContentLength();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("total length of files is " + length + " bytes");
		InputStream is = null;
		FileOutputStream fos = null;
		HttpURLConnection connexion = null;

		long start = new Date().getTime();
		long debut = new Date().getTime();
		int progress = 0;
		for (String json : jsons) {
			URL url;
			try {
				System.out.println("downloading " + json);
				url = new URL(jsonurls.replace("?{table}", json));

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
					progress += count;
					long delta = new Date().getTime() - start;
					fos.write(data, 0, count);

					if (delta >= 500) {
						int pourcent = (int) ((progress / (double) length) * 100);
						System.out.println(pourcent + "% completed");
						start = new Date().getTime();
					}
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
		}
		System.out.println("downloading finished in "
				+ (new Date().getTime() - debut) + " milliseconds");

	}

	@Override
	public void preprocess() {
		downloadJSons(destinationFolder);
	}
}
