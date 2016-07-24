package com.ten01.jsmappertools.js;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.List;

/**
 * @author chidveer chinthauntla
 */
public class JsGenerator {

	public static void generate(List<JCCompilationUnit> parseTrees) {
		System.out.println(parseTrees.toString());
		for(JCCompilationUnit parseTree : parseTrees){
			
			
		}
	}
	
	 

}
