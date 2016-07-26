/*
 * Copyright (c) 1999, 2015, Oracle and/or its affiliates. All rights reserved.
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

package com.ten01.jsmappertools.javac.jvm;

import static com.ten01.jsmappertools.commons.FileUtils.toJsFile;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

import java.io.IOException;
import java.io.OutputStream;

import javax.tools.JavaFileObject;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.file.RelativePath.RelativeFile;
import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

/**
 * @author chidveer chinthauntla
 *  This class provides operations to map an internal symbol table graph
 *  rooted in a ClassSymbol into a classfile.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class JsWriter extends ClassWriter {

	protected JsWriter(Context context) {
		super(context);
	}
   
	/** Get the ClassWriter instance for this context. */
    public static JsWriter instance(Context context) {
        ClassWriter instance = context.get(classWriterKey);
        if (instance == null)
            instance = new JsWriter(context);
        return (JsWriter)instance;
    }
    
    public JavaFileObject writeJs(JCCompilationUnit jTree)
            throws IOException, PoolOverflow, StringOverflow{
            JavaFileObject outFile
                = ((JavacFileManager)fileManager).getFileForOutput(CLASS_OUTPUT,
                		new RelativeFile(toJsFile(jTree)),
                		jTree.getSourceFile());
            OutputStream out = outFile.openOutputStream();
            try {
            	out.write(jTree.toString().getBytes());
                if (verbose)
                    log.printVerbose("wrote.file", outFile);
                out.close();
                out = null;
            } finally {
                if (out != null) {
                    // if we are propagating an exception, delete the file
                    out.close();
                    outFile.delete();
                    outFile = null;
                }
            }
            return outFile; // may be null if write failed
        }

}
