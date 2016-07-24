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

package com.ten01.jsmappertools.javac.comp;

import static com.sun.tools.javac.code.Flags.EXISTS;
import static com.sun.tools.javac.code.Flags.INTERFACE;
import static com.sun.tools.javac.code.Flags.PUBLIC;
import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.code.Kinds.MTH;
import static com.sun.tools.javac.code.Kinds.PCK;
import static com.sun.tools.javac.code.Kinds.TYP;
import static com.sun.tools.javac.code.Kinds.VAR;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Scope.ImportScope;
import com.sun.tools.javac.code.Scope.StarImportScope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.main.Option.PkgInfo;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;

/** @author chidveer chinthauntla 
 * This class enters symbols for all encountered definitions into
 *  the symbol table. The pass consists of two phases, organized as
 *  follows:
 *
 *  <p>In the first phase, all class symbols are entered into their
 *  enclosing scope, descending recursively down the tree for classes
 *  which are members of other classes. The class symbols are given a
 *  MemberEnter object as completer.
 *
 *  <p>In the second phase classes are completed using
 *  MemberEnter.complete().  Completion might occur on demand, but
 *  any classes that are not completed that way will be eventually
 *  completed by processing the `uncompleted' queue.  Completion
 *  entails (1) determination of a class's parameters, supertype and
 *  interfaces, as well as (2) entering all symbols defined in the
 *  class into its scope, with the exception of class symbols which
 *  have been entered in phase 1.  (2) depends on (1) having been
 *  completed for a class and all its superclasses and enclosing
 *  classes. That's why, after doing (1), we put classes in a
 *  `halfcompleted' queue. Only when we have performed (1) for a class
 *  and all it's superclasses and enclosing classes, we proceed to
 *  (2).
 *
 *  <p>Whereas the first phase is organized as a sweep through all
 *  compiled syntax trees, the second phase is demand. Members of a
 *  class are entered when the contents of a class are first
 *  accessed. This is accomplished by installing completer objects in
 *  class symbols for compiled classes which invoke the member-enter
 *  phase for the corresponding class tree.
 *
 *  <p>Classes migrate from one phase to the next via queues:
 *
 *  <pre>{@literal
 *  class enter -> (Enter.uncompleted)         --> member enter (1)
 *              -> (MemberEnter.halfcompleted) --> member enter (2)
 *              -> (Todo)                      --> attribute
 *                                              (only for toplevel classes)
 *  }</pre>
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class EnterJs extends Enter {

    public static Enter instance(Context context) {
        Enter instance = context.get(enterKey);
        if (instance == null)
            instance = new EnterJs(context);
        return instance;
    }

    protected EnterJs(Context context) {
        context.put(enterKey, this);

        log = Log.instance(context);
        reader = ClassReader.instance(context);
        make = TreeMaker.instance(context);
        syms = Symtab.instance(context);
        chk = Check.instance(context);
        memberEnter = MemberEnter.instance(context);
        
        types = Types.instance(context);
        annotate = Annotate.instance(context);
        lint = Lint.instance(context);
        names = Names.instance(context);

        predefClassDef = make.ClassDef(
            make.Modifiers(PUBLIC),
            syms.predefClass.name,
            List.<JCTypeParameter>nil(),
            null,
            List.<JCExpression>nil(),
            List.<JCTree>nil());
        predefClassDef.sym = syms.predefClass;
        todo = Todo.instance(context);
        fileManager = context.get(JavaFileManager.class);

        Options options = Options.instance(context);
        pkginfoOpt = PkgInfo.get(options);
        typeEnvs = TypeEnvsJs.instance(context);
    }

    
}
