package com.ten01.jsmappertools.test.javac;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.ten01.jsmappertools.commons.FileUtils;
import com.ten01.jsmappertools.javac.Main;
import com.ten01.jsmappertools.test.AbstractTest;

public class FunctionalJsMappingTest extends AbstractTest {
	
	String[] OPTIONS_AND_FILES =  ArrayUtils.addAll(OPTIONS, FileUtils.getFilePath("src/test/java/com/ten01/jsmappertools/test/javac/FunctionalClass.java"));

	@Test
	public void mapNewFeatureTest(){
		Main.compileToJs(OPTIONS_AND_FILES);
	}
	

}
