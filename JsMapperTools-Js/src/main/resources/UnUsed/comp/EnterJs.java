package com.ten01.jsmappertools.javac.comp;

import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

public class EnterJs extends Enter{

	protected EnterJs(Context context) {
		super(context);
	}
	
    public static EnterJs instance(Context context) {
        Enter instance = context.get(enterKey);
        if (instance == null)
            instance = new EnterJs(context);
        return (EnterJs) instance;
    }
    
    @Override
    public void main(List<JCCompilationUnit> trees) {
        complete(trees, null);
    }

}
