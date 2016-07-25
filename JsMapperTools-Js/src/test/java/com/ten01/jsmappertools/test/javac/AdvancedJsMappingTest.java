package com.ten01.jsmappertools.test.javac;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.ten01.jsmappertools.commons.FileUtils;
import com.ten01.jsmappertools.javac.Main;
import com.ten01.jsmappertools.test.AbstractTest;

/**
 * @author chidveer chinthauntla
 */
public class AdvancedJsMappingTest extends AbstractTest {

	String[] OPTIONS_AND_FILES =  ArrayUtils.addAll(OPTIONS, FileUtils.getFilePath("src/test/java/com/ten01/jsmappertools/test/javac/AdvancedClass.java"));
	
	@Test
	public void mapAdavacedJsClass(){
		Main.compileToJs(OPTIONS_AND_FILES);
	}
	
	//@Test
	public void doRegularCompilation(){
		doRegularCompilation(OPTIONS_AND_FILES);
	}


}
