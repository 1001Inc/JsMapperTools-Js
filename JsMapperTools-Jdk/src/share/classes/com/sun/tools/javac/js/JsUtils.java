package com.sun.tools.javac.js;

import static com.sun.tools.javac.js.Constants.PRETTY_CLASS;

import java.io.Writer;

import com.sun.tools.javac.tree.Pretty;

/**
 * @author chidveer chinthauntla
 */
public abstract class JsUtils {
	
	public static Pretty getPretty(ClassLoader classLoader, Writer out, boolean sourceOutput){
		try {
			return (Pretty)classLoader
					.loadClass(PRETTY_CLASS)
					.getConstructor(Writer.class, Boolean.class)
					.newInstance(out, sourceOutput);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
	}

}
