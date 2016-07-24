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

package com.ten01.jsmappertools.javac.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import com.sun.source.tree.*;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Scope.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.ten01.jsmappertools.js.PrettyJs;

import static com.sun.tools.javac.tree.JCTree.Tag.*;

/**
 * Root class for abstract syntax tree nodes. It provides definitions
 * for specific tree nodes as subclasses nested inside.
 *
 * <p>Each subclass is highly standardized.  It generally contains
 * only tree fields for the syntactic subcomponents of the node.  Some
 * classes that represent identifier uses or definitions also define a
 * Symbol field that denotes the represented identifier.  Classes for
 * non-local jumps also carry the jump target as a field.  The root
 * class Tree itself defines fields for the tree's type and position.
 * No other fields are kept in a tree node; instead parameters are
 * passed to methods accessing the node.
 *
 * <p>Except for the methods defined by com.sun.source, the only
 * method defined in subclasses is `visit' which applies a given
 * visitor to the tree. The actual tree processing is done by visitor
 * classes in other packages. The abstract class Visitor, as well as
 * an Factory interface for trees, are defined as inner classes in
 * Tree.
 *
 * <p>To avoid ambiguities with the Tree API in com.sun.source all sub
 * classes should, by convention, start with JC (javac).
 *
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 *
 * @see TreeMaker
 * @see TreeInfo
 * @see TreeTranslator
 * @see Pretty
 */
public abstract class JsJCTree extends JCTree implements Cloneable{

    /** Convert a tree to a pretty-printed string. */
    @Override
    public String toString() {
        StringWriter s = new StringWriter();
        try {
            new PrettyJs(s, false).printExpr(this);
        }
        catch (IOException e) {
            // should never happen, because StringWriter is defined
            // never to throw any IOExceptions
            throw new AssertionError(e);
        }
        return s.toString();
    }
    
    public static class JsJCExpressionStatement extends JCExpressionStatement{

        protected JsJCExpressionStatement(JCExpression expr) {
			super(expr);
		}

		/** Convert a expression-statement tree to a pretty-printed string. */
        @Override
        public String toString() {
            StringWriter s = new StringWriter();
            try {
                new PrettyJs(s, false).printStat(this);
            }
            catch (IOException e) {
                // should never happen, because StringWriter is defined
                // never to throw any IOExceptions
                throw new AssertionError(e);
            }
            return s.toString();
        }
    }

   

}
