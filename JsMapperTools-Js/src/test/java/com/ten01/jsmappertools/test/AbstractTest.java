package com.ten01.jsmappertools.test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

//@RunWith(BlockJUnit4ClassRunner.class)
public class AbstractTest {
	
	protected static final String OUT_PUT_DIR = new File("unittests/").getPath();
	
	protected static final String[] OPTIONS = {
			"-d",
			OUT_PUT_DIR,
	};
	
	
	//@Test
	public void doRegularCompilation(String[] optionsWithFiles){
		try(StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw)){
		    int rc = com.sun.tools.javac.Main.compile(optionsWithFiles, pw);
		    pw.flush();
		    if (sw.getBuffer().length() > 0)
		        System.err.println(sw.toString());
		    if (rc != 0)
		        throw new RuntimeException("compilation failed: rc=" + rc);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

}
