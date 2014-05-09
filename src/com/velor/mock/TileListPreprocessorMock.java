package com.velor.mock;

import java.io.File;

import com.velor.TileListPreprocessor;

public class TileListPreprocessorMock extends TileListPreprocessor {

	@Override
	protected void downloadFile() {
		String fileName = saveDir + File.separator
				+ fileListUrl.substring(fileListUrl.lastIndexOf('/'));

		File result = new File(fileName);

		if (!result.exists()) {
			super.downloadFile();
		}
	}

}
