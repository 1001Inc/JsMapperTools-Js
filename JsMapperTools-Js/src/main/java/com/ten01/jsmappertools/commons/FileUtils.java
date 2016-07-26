package com.ten01.jsmappertools.commons;

import static com.ten01.jsmapper.js.common.JSKeyWords.FILE_EXTENSION;
import static com.ten01.jsmapper.js.common.JavaKeyWords.PACKAGE_SEPERATOR;
import static com.ten01.jsmapper.js.common.JavaKeyWords.SRC_EXTENSION;

import java.io.File;
import java.io.FileNotFoundException;

import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;

import com.sun.tools.javac.file.RegularFileObject;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

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
	public static String getScriptTag(String file) {
		return "<script src=\""+file+"\"/> ";
	}
	
	public static String toJsFile(JCCompilationUnit tree) {
		if(tree == null)
			return "";
		String file = tree.pid != null? tree.pid.toString():"";
		return toJsFile(file+"."+sourceFile(tree.getSourceFile()));
	}

	private static String sourceFile(JavaFileObject sourceFile) {
		if(sourceFile instanceof RegularFileObject){
			return ((RegularFileObject)sourceFile).getShortName();
		}
		return sourceFile.getName();
	}
	
	public static String toJsFile(String file) {
		if(StringUtils.isBlank(file))
			return "";
		file = file.replaceFirst("\\"+SRC_EXTENSION+"$", "");
		return replacePathSerperators(file)+FILE_EXTENSION;
	}

	public static String replacePathSerperators(String file) {
		return file.replaceAll("\\"+PACKAGE_SEPERATOR, "/");
	}
	
	public static void main(String[] args) {
		System.out.println(toJsFile("com.ten01.jsmappertools.test.javac.AdvancedClass.java"));
	}
	
}
