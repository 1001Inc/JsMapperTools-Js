package com.ten01.jsmappertools.javac.main;

import static com.sun.tools.javac.main.Option.PROC;
import static com.sun.tools.javac.main.Option.PROCESSOR;
import static com.sun.tools.javac.main.Option.PROCESSORPATH;
import static com.sun.tools.javac.main.Option.XPRINT;

import java.util.Queue;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Pair;
import com.ten01.jsmappertools.javac.jvm.GenJs;

/**
 * @author chidveer chinthauntla
 */
public class JavaJsCompilerOld  {


	
	/* Generating JavaScript
	 * (non-Javadoc)
	 * @see com.sun.tools.javac.main.JavaCompiler#generate(java.util.Queue, java.util.Queue)
	 */
	/*@Override
	public void generate(Queue<Pair<Env<AttrContext>, JCClassDecl>> queue, Queue<JavaFileObject> results) {
		// TODO Do any changes to instance var, if needed -- as genCode is not overridable -- JavaFileObject genCode(Env<AttrContext> env, JCClassDecl cdef) throws IOException {
		
		if (shouldStop(CompileState.GENERATE))
            return;

        boolean usePrintSource = (stubOutput || sourceOutput || printFlat);
        
	}*/
	
	/*@Override
	protected void desugar(final Env<AttrContext> env, Queue<Pair<Env<AttrContext>, JCClassDecl>> results) {
		
	}*/
	
		

}
