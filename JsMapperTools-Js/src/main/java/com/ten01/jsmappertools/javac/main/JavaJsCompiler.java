/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.ten01.jsmappertools.javac.main;

import static com.sun.tools.javac.main.Option.ENCODING;
import static com.sun.tools.javac.main.Option.G_CUSTOM;
import static com.sun.tools.javac.main.Option.PRINTSOURCE;
import static com.sun.tools.javac.main.Option.VERBOSE;
import static com.sun.tools.javac.main.Option.WERROR;
import static com.sun.tools.javac.main.Option.XJCOV;
import static com.sun.tools.javac.main.Option.XLINT_CUSTOM;
import static com.ten01.jsmappertools.commons.FileUtils.toJsFile;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.code.Lint.LintCategory;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.comp.CompileStates;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Flow;
import com.sun.tools.javac.comp.Lower;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.comp.TransTypes;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.file.RelativePath.RelativeFile;
import com.sun.tools.javac.js.JsUtils;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.jvm.Gen;
import com.sun.tools.javac.jvm.JNIWriter;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Abort;
import com.sun.tools.javac.util.BaseFileManager;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.RichDiagnosticFormatter;
import com.ten01.jsmappertools.javac.jvm.JsWriter;


/** This class could be the main entry point for GJC when GJC is used as a
 *  component in a larger software system. It provides operations to
 *  construct a new compiler, and to run a new compiler on a set of source
 *  files.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class JavaJsCompiler extends JavaCompiler {

	/** Get the JavaCompiler instance for this context. */
    public static JavaJsCompiler instance(Context context) {
        JavaCompiler instance = context.get(compilerKey);
        if (instance == null)
            instance = new JavaJsCompiler(context);
        return (JavaJsCompiler)instance;
    }   		

    /** Construct a new compiler using a shared context.
     */
    public JavaJsCompiler(Context context) {
        this.context = context;
        context.put(compilerKey, this);

        // if fileManager not already set, register the JavacFileManager to be used
        if (context.get(JavaFileManager.class) == null)
            JavacFileManager.preRegister(context);

        names = Names.instance(context);
        log = Log.instance(context);
        diagFactory = JCDiagnostic.Factory.instance(context);
        reader = ClassReader.instance(context);
        make = TreeMaker.instance(context);
        writer = JsWriter.instance(context);
        jniWriter = JNIWriter.instance(context);
        enter = Enter.instance(context);
        todo = Todo.instance(context);

        fileManager = context.get(JavaFileManager.class);
        parserFactory = ParserFactory.instance(context);
        compileStates = CompileStates.instance(context);

        try {
            // catch completion problems with predefineds
            syms = Symtab.instance(context);
        } catch (CompletionFailure ex) {
            // inlined Check.completionError as it is not initialized yet
            log.error("cant.access", ex.sym, ex.getDetailValue());
            if (ex instanceof ClassReader.BadClassFile)
                throw new Abort();
        }
        source = Source.instance(context);
        Target target = Target.instance(context);
        attr = Attr.instance(context);
        chk = Check.instance(context);
        gen = Gen.instance(context);
        //ten01
        //gen = GenJs.instance(context);
        flow = Flow.instance(context);
        transTypes = TransTypes.instance(context);
        lower = Lower.instance(context);
        annotate = Annotate.instance(context);
        types = Types.instance(context);
        taskListener = MultiTaskListener.instance(context);

        reader.sourceCompleter = thisCompleter;

        options = Options.instance(context);

        verbose       = options.isSet(VERBOSE);
        sourceOutput  = options.isSet(PRINTSOURCE); // used to be -s
        stubOutput    = options.isSet("-stubs");
        relax         = options.isSet("-relax");
        printFlat     = options.isSet("-printflat");
        attrParseOnly = options.isSet("-attrparseonly");
        encoding      = options.get(ENCODING);
        lineDebugInfo = options.isUnset(G_CUSTOM) ||
                        options.isSet(G_CUSTOM, "lines");
        genEndPos     = options.isSet(XJCOV) ||
                        context.get(DiagnosticListener.class) != null;
        devVerbose    = options.isSet("dev");
        processPcks   = options.isSet("process.packages");
        werror        = options.isSet(WERROR);

        if (source.compareTo(Source.DEFAULT) < 0) {
            if (options.isUnset(XLINT_CUSTOM, "-" + LintCategory.OPTIONS.option)) {
                if (fileManager instanceof BaseFileManager) {
                    if (((BaseFileManager) fileManager).isDefaultBootClassPath())
                        log.warning(LintCategory.OPTIONS, "source.no.bootclasspath", source.name);
                }
            }
        }

        checkForObsoleteOptions(target);

        verboseCompilePolicy = options.isSet("verboseCompilePolicy");

        if (attrParseOnly)
            compilePolicy = CompilePolicy.ATTR_ONLY;
        else
            compilePolicy = CompilePolicy.decode(options.get("compilePolicy"));

        implicitSourcePolicy = ImplicitSourcePolicy.decode(options.get("-implicit"));

        completionFailureName =
            options.isSet("failcomplete")
            ? names.fromString(options.get("failcomplete"))
            : null;

        shouldStopPolicyIfError =
            options.isSet("shouldStopPolicy") // backwards compatible
            ? CompileState.valueOf(options.get("shouldStopPolicy"))
            : options.isSet("shouldStopPolicyIfError")
            ? CompileState.valueOf(options.get("shouldStopPolicyIfError"))
            : CompileState.INIT;
        shouldStopPolicyIfNoError =
            options.isSet("shouldStopPolicyIfNoError")
            ? CompileState.valueOf(options.get("shouldStopPolicyIfNoError"))
            : CompileState.GENERATE;

        if (options.isUnset("oldDiags"))
            log.setDiagnosticFormatter(RichDiagnosticFormatter.instance(context));
    }


	public void translate(List<JavaFileObject> sourceFileObjects,
            List<String> classnames,
            Iterable<? extends Processor> processors) {
		if (processors != null && processors.iterator().hasNext())
            explicitAnnotationProcessingRequested = true;
        // as a JavaCompiler can only be used once, throw an exception if
        // it has been used before.
        if (hasBeenUsed)
            throw new AssertionError("attempt to reuse JavaCompiler");
        hasBeenUsed = true;

        // forcibly set the equivalent of -Xlint:-options, so that no further
        // warnings about command line options are generated from this point on
        options.put(XLINT_CUSTOM.text + "-" + LintCategory.OPTIONS.option, "true");
        options.remove(XLINT_CUSTOM.text + LintCategory.OPTIONS.option);

        start_msec = now();

        try {
            initProcessAnnotations(processors);
            translate(stopIfError(CompileState.PARSE, parseFiles(sourceFileObjects)));
            close();
        } catch (Abort ex) {
            if (devVerbose)
                ex.printStackTrace(System.err);
        } finally {
            if (procEnvImpl != null)
                procEnvImpl.close();
        }
		
	}

	private void translate(List<JCCompilationUnit> jTrees) {
		boolean usePrintSource = (stubOutput || sourceOutput || printFlat);
		
		for(JCCompilationUnit tree : jTrees){
			
			if (verboseCompilePolicy) {
                printNote("[generate "
                               + (usePrintSource ? " source" : "code")
                               + " " + tree.sourcefile + "]");
            }
			
			if (!taskListener.isEmpty()) {
                /*TaskEvent e = new TaskEvent(TaskEvent.Kind.GENERATE, env.toplevel, cdef.sym);
                taskListener.started(e);*/
            }

            JavaFileObject prev = log.useSource(tree.sourcefile);
            try {
            	if(errorCount() == 0)
            		translate(tree);
                /*JavaFileObject file;
                if (usePrintSource)
                    file = printSource(tree);
                else {
                    if (fileManager.hasLocation(StandardLocation.NATIVE_HEADER_OUTPUT)
                            && jniWriter.needsHeader(cdef.sym)) {
                        jniWriter.write(cdef.sym);
                    }
                    file = genCode(env, cdef);
                }
                if (results != null && file != null)
                    results.add(file);*/
            } catch (IOException ex) {
                log.error("class.cant.write",
                		tree.sourcefile, ex.getMessage());
                return;
            } finally {
                log.useSource(prev);
            }

            if (!taskListener.isEmpty()) {
                /*TaskEvent e = new TaskEvent(TaskEvent.Kind.GENERATE, env.toplevel, cdef.sym);
                taskListener.finished(e);*/
            }
		}
	}
	
	protected JavaFileObject translate(JCCompilationUnit jTree) throws IOException {
		JavaFileObject outFile = ((JavacFileManager)fileManager).getFileForOutput(CLASS_OUTPUT,
	        		new RelativeFile(toJsFile(jTree)),
	        		jTree.getSourceFile());
        if (inputFiles.contains(outFile)) {
            log.error("source.cant.overwrite.input.file", outFile);
            return null;
        } else {
            BufferedWriter out = new BufferedWriter(outFile.openWriter());
            try {
                //new Pretty(out, true).printUnit(env.toplevel, cdef);
            	JsUtils.getPretty(this.getClass().getClassLoader(),
            			out, true).printUnit(jTree, null);
                if (verbose)
                    log.printVerbose("wrote.file", outFile);
            } finally {
                out.close();
            }
            return outFile;
        }
    }
}
