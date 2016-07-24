package com.ten01.jsmappertools.javac.jvm;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.jvm.Gen;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

/**
 * @author chidveer chinthauntla
 */
public class GenJs extends Gen{

	public GenJs(Context context) {
		super(context);
	}
	
	public static GenJs instance(Context context) {
		Gen instance = context.get(genKey);
        if (instance == null)
            instance = new GenJs(context);
        return (GenJs)instance;
    }
	

}
