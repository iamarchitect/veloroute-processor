package com.velor;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.velor.json.JsonDownloader;

public class VelorProcessor {
	private JsonDownloader downloader;
	private Json2DatabaseProcessor jsonProcessor;
	private DatabaseManager databaseManager;
	private RoutePreprocessor routePreprocessor;
	private RouteRenderer routeRenderer;
	private String databaseName;

	private SqlDumperPropertyPlaceholderHelper sqlDumper;

	public void setRoutePreprocessor(RoutePreprocessor routePreprocessor) {
		this.routePreprocessor = routePreprocessor;
	}

	public void setDownloader(JsonDownloader downloader) {
		this.downloader = downloader;
	}

	public void setJsonProcessor(Json2DatabaseProcessor jsonProcessor) {
		this.jsonProcessor = jsonProcessor;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setSqlDumper(SqlDumperPropertyPlaceholderHelper sqlDumper) {
		this.sqlDumper = sqlDumper;
	}

	public void setRouteRenderer(RouteRenderer routeRenderer) {
		this.routeRenderer = routeRenderer;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	protected Options buildOptions() {
		//@formatter:off
		Options options = new Options();
		options.addOption("nod", false, "bypass the download process");
		options.addOption("noi", false, "bypass the import process");
		options.addOption("nor", false, "bypass the route process");
		options.addOption("dmpsql", true,"dump all sql statements into the specified folder");
		options.addOption("help", false, "display this help");
		//@formatter:on
		return options;
	}

	protected void backup(String databaseName) {
		File database = new File(databaseName);
		if (database.exists()) {
			try {
				String suff = DateFormat
						.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
						.format(new Date()).replace(" ", "-").replace(":", "-");
				String name = databaseName + "." + suff + ".bak";

				File f = new File(name);

				int i = 0;
				while (f.exists()) {
					f = new File(name + i++);
				}

				FileUtils.copyFile(database, f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void run(String[] args) {
		Date d = new Date();
		long start = new Date().getTime();

		// automatically generate the help statement

		CommandLineParser parser = new BasicParser();
		CommandLine line;
		Options options;
		try {
			options = buildOptions();
			line = parser.parse(options, args);
		} catch (ParseException e1) {
			// TODO
			System.out.println("Error");

			throw new RuntimeException(e1);
		}

		if (line.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("velor", options);
			return;
		}

		System.out.println("Processing started at "
				+ DateFormat.getInstance().format(d));

		databaseManager.open(databaseName);
		databaseManager.beginTransaction();

		try {
			// FIXME use an option handler (map) with the option name as key and
			// a Preprocessor (the interface) as value. Or pass in the options
			// to a Preprocessor chain ?
			if (!line.hasOption("nod")) {
				downloader.preprocess();
			}

			if (!line.hasOption("noi")) {
				databaseManager.dropTables();
				databaseManager.createTables();
				jsonProcessor.preprocess();
			}
			if (!line.hasOption("nor")) {
				routePreprocessor.preprocess();
			}
			if (!line.hasOption("not")) {
				routeRenderer.preprocess();
			}

			if (line.hasOption("dmpsql")) {
				sqlDumper.preprocess();
			}
			databaseManager.endTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			databaseManager.rollback();
		} finally {
			databaseManager.close();

			System.out.println("Processing finished in "
					+ (new Date().getTime() - start) + " milliseconds");
		}
	}

}
