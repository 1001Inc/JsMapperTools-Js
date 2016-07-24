/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package com.ten01.jsmappertools.javac.processing;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.file.FSInfo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Assert;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JavacMessages;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Log.DeferredDiagnosticHandler;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;
import com.ten01.jsmappertools.javac.main.JavaJsCompiler;

/**
 * @author chidveer chinthauntla
 * Objects of this class hold and manage the state needed to support
 * annotation processing.
 *
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
public class JavacJsProcessingEnvironment extends JavacProcessingEnvironment {
    
	protected JavacJsProcessingEnvironment(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    /** Get the JavacProcessingEnvironment instance for this context. */
    public static JavacProcessingEnvironment instance(Context context) {
        JavacProcessingEnvironment instance = context.get(JavacProcessingEnvironment.class);
        if (instance == null)
            instance = new JavacJsProcessingEnvironment(context);
        return instance;
    }
    
    /**
     * Helper object for a single round of annotation processing.
     */
    class RoundJs extends JavacProcessingEnvironment.Round {

        public RoundJs(Context context, List<JCCompilationUnit> roots, List<ClassSymbol> classSymbols,
				DeferredDiagnosticHandler deferredDiagnosticHandler) {
			super(context, roots, classSymbols, deferredDiagnosticHandler);
		}

		/** Create the compiler to be used for the final compilation. */
        public JavaCompiler finalCompiler() {
            try {
                Context nextCtx = nextContext();
                JavacJsProcessingEnvironment.this.context = nextCtx;
                JavaCompiler c = JavaJsCompiler.instance(nextCtx);
                c.log.initRound(compiler.log);
                return c;
            } finally {
                compiler.close(false);
            }
        }
        
        protected Context nextContext() {
            Context next = new Context(context);

            Options options = Options.instance(context);
            Assert.checkNonNull(options);
            next.put(Options.optionsKey, options);

            Locale locale = context.get(Locale.class);
            if (locale != null)
                next.put(Locale.class, locale);

            Assert.checkNonNull(messages);
            next.put(JavacMessages.messagesKey, messages);

            final boolean shareNames = true;
            if (shareNames) {
                Names names = Names.instance(context);
                Assert.checkNonNull(names);
                next.put(Names.namesKey, names);
            }

            DiagnosticListener<?> dl = context.get(DiagnosticListener.class);
            if (dl != null)
                next.put(DiagnosticListener.class, dl);

            MultiTaskListener mtl = context.get(MultiTaskListener.taskListenerKey);
            if (mtl != null)
                next.put(MultiTaskListener.taskListenerKey, mtl);

            FSInfo fsInfo = context.get(FSInfo.class);
            if (fsInfo != null)
                next.put(FSInfo.class, fsInfo);

            JavaFileManager jfm = context.get(JavaFileManager.class);
            Assert.checkNonNull(jfm);
            next.put(JavaFileManager.class, jfm);
            if (jfm instanceof JavacFileManager) {
                ((JavacFileManager)jfm).setContext(next);
            }

            Names names = Names.instance(context);
            Assert.checkNonNull(names);
            next.put(Names.namesKey, names);

            Tokens tokens = Tokens.instance(context);
            Assert.checkNonNull(tokens);
            next.put(Tokens.tokensKey, tokens);

            Log nextLog = Log.instance(next);
            nextLog.initRound(log);

            JavaCompiler oldCompiler = JavaJsCompiler.instance(context);
            JavaCompiler nextCompiler = JavaJsCompiler.instance(next);
            nextCompiler.initRound(oldCompiler);

            filer.newRound(next);
            messager.newRound(next);
            elementUtils.setContext(next);
            typeUtils.setContext(next);

            JavacTask task = context.get(JavacTask.class);
            if (task != null) {
                next.put(JavacTask.class, task);
                if (task instanceof BasicJavacTask)
                    ((BasicJavacTask) task).updateContext(next);
            }

            JavacTrees trees = context.get(JavacTrees.class);
            if (trees != null) {
                next.put(JavacTrees.class, trees);
                trees.updateContext(next);
            }

            context.clear();
            return next;
        }
    }
    
    
    // TODO: internal catch clauses?; catch and rethrow an annotation
    // processing error
    public JavaCompiler doProcessing(Context context,
                                     List<JCCompilationUnit> roots,
                                     List<ClassSymbol> classSymbols,
                                     Iterable<? extends PackageSymbol> pckSymbols,
                                     Log.DeferredDiagnosticHandler deferredDiagnosticHandler) {
        log = Log.instance(context);

        Set<PackageSymbol> specifiedPackages = new LinkedHashSet<PackageSymbol>();
        for (PackageSymbol psym : pckSymbols)
            specifiedPackages.add(psym);
        this.specifiedPackages = Collections.unmodifiableSet(specifiedPackages);

        Round round = new RoundJs(context, roots, classSymbols, deferredDiagnosticHandler);

        boolean errorStatus;
        boolean moreToDo;
        do {
            // Run processors for round n
            round.run(false, false);

            // Processors for round n have run to completion.
            // Check for errors and whether there is more work to do.
            errorStatus = round.unrecoverableError();
            moreToDo = moreToDo();

            round.showDiagnostics(errorStatus || showResolveErrors);

            // Set up next round.
            // Copy mutable collections returned from filer.
            round = round.next(
                    new LinkedHashSet<JavaFileObject>(filer.getGeneratedSourceFileObjects()),
                    new LinkedHashMap<String,JavaFileObject>(filer.getGeneratedClasses()));

             // Check for errors during setup.
            if (round.unrecoverableError())
                errorStatus = true;

        } while (moreToDo && !errorStatus);

        // run last round
        round.run(true, errorStatus);
        round.showDiagnostics(true);

        filer.warnIfUnclosedFiles();
        warnIfUnmatchedOptions();

        /*
         * If an annotation processor raises an error in a round,
         * that round runs to completion and one last round occurs.
         * The last round may also occur because no more source or
         * class files have been generated.  Therefore, if an error
         * was raised on either of the last *two* rounds, the compile
         * should exit with a nonzero exit code.  The current value of
         * errorStatus holds whether or not an error was raised on the
         * second to last round; errorRaised() gives the error status
         * of the last round.
         */
        if (messager.errorRaised()
                || werror && round.warningCount() > 0 && round.errorCount() > 0)
            errorStatus = true;

        Set<JavaFileObject> newSourceFiles =
                new LinkedHashSet<JavaFileObject>(filer.getGeneratedSourceFileObjects());
        roots = cleanTrees(round.roots);

        JavaCompiler compiler = round.finalCompiler();

        if (newSourceFiles.size() > 0)
            roots = roots.appendList(compiler.parseFiles(newSourceFiles));

        errorStatus = errorStatus || (compiler.errorCount() > 0);

        // Free resources
        this.close();

        if (!taskListener.isEmpty())
            taskListener.finished(new TaskEvent(TaskEvent.Kind.ANNOTATION_PROCESSING));

        if (errorStatus) {
            if (compiler.errorCount() == 0)
                compiler.log.nerrors++;
            return compiler;
        }

        compiler.enterTreesIfNeeded(roots);

        return compiler;
    }

 
}
