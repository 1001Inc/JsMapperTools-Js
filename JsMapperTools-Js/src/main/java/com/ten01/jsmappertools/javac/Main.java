package com.ten01.jsmappertools.javac;

import java.io.PrintWriter;

/**
 * @author chidveer chinthauntla
 */
public class Main{// extends com.sun.tools.javac.Main{
	
    public static int translateToJs(String[] args) {
    	com.ten01.jsmappertools.javac.main.Main compiler =
            new com.ten01.jsmappertools.javac.main.Main("javac", new PrintWriter(System.out, true));
    	return compiler.translate(args).exitCode;
    }

    public static void main(String[] args) throws Exception {
        System.exit(translateToJs(args));
    }

    public static int translateToJs(String[] args, PrintWriter out) {
    	com.ten01.jsmappertools.javac.main.Main compiler =
                new com.ten01.jsmappertools.javac.main.Main("javac", out);
        return compiler.translate(args).exitCode;
    }  
}
