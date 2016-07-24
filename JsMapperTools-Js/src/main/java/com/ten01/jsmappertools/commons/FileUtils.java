package com.ten01.jsmappertools.commons;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author chidveer chinthauntla
 */
public class FileUtils {
	
	public static String getFilePath(String file){
		File f = new File(file);
		if(!f.exists())
			throw new RuntimeException(new FileNotFoundException(file));
		return f.getPath();
	}
}
