package com.velor;

import java.util.List;
import java.util.Map;

public class PreprocessorHandler implements Preprocessor {

	List<Preprocessor> chain;
	Map<Preprocessor, String> required;
	Map<Preprocessor, String> masks;

	public void preprocess() {
	}

}
