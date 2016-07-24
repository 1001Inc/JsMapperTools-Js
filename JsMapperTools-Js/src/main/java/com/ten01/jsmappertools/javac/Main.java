package com.ten01.jsmappertools.javac;

import java.io.PrintWriter;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.List;
import com.ten01.jsmappertools.js.JsGenerator;

/**
 * @author chidveer chinthauntla
 */
public class Main{// extends com.sun.tools.javac.Main{

	
    public static void compileToJs(String[] args) {
    	com.ten01.jsmappertools.javac.main.Main compiler =
            new com.ten01.jsmappertools.javac.main.Main("javac", new PrintWriter(System.out, true));
    	JsGenerator.generate(compiler.parseTree(args));
    	
    }



    

}
