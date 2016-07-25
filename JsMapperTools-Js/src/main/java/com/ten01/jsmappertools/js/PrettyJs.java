package com.ten01.jsmappertools.js;

import static com.sun.tools.javac.code.Flags.ENUM;
import static com.sun.tools.javac.code.Flags.INTERFACE;
import static com.sun.tools.javac.tree.JCTree.Tag.IMPORT;
import static com.sun.tools.javac.tree.JCTree.Tag.NEWCLASS;
import static com.sun.tools.javac.tree.JCTree.Tag.SELECT;
import static com.ten01.jsmapper.js.common.JSKeyWords.CONSTRUCTOR;
import static com.ten01.jsmapper.js.common.JSKeyWords.LET;
import static com.ten01.jsmapper.js.common.JavaKeyWords.STATIC;

import java.io.IOException;
import java.io.Writer;

import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.ten01.jsmapper.js.common.JSKeyWords;
import com.ten01.jsmapper.js.common.JavaKeyWords;
import com.ten01.jsmappertools.mappers.AnnotationMapper;
import com.ten01.jsmappertools.mappers.ClassAnnotationVisitor;

/**
 * @author chidveer chinthauntla
 */
public class PrettyJs extends Pretty {
	
	private boolean methodArg = false;
	private ClassAnnotationVisitor classAnnotationVisitor;

	public PrettyJs(Writer out, Boolean sourceOutput) {
		super(out, sourceOutput);
	}
	
	public void printUnit(JCCompilationUnit tree, JCClassDecl cdef) throws IOException {
        docComments = tree.docComments;
        printDocComment(tree);
        if (tree.pid != null) { //FIXME: if pid is null it will not add the file name
            print("// "+toJsFormat(tree.pid != null? tree.pid.toString():""+"."+tree.getSourceFile().getName()));
            println();
        }
        boolean firstImport = true;
        for (List<JCTree> l = tree.defs;
        l.nonEmpty() && (cdef == null || l.head.hasTag(IMPORT));
        l = l.tail) {
            if (l.head.hasTag(IMPORT)) {
                JCImport imp = (JCImport)l.head;
                Name name = TreeInfo.name(imp.qualid);
                if (name == name.table.names.asterisk ||
                        cdef == null ||
                        isUsed(TreeInfo.symbol(imp.qualid), cdef)) {
                    if (firstImport) {
                        firstImport = false;
                        println();
                    }
                    printStat(imp);
                }
            } else {
                printStat(l.head);
            }
        }
        if (cdef != null) {
            printStat(cdef);
            println();
        }
    }
	
	public void visitClassDef(JCClassDecl tree) {
        try {
            println(); align();
            printDocComment(tree);
            //printAnnotations(tree.mods.annotations);
            processClassAnnotations(tree.mods.annotations);
            //printFlags(tree.mods.flags & ~INTERFACE);
            Name enclClassNamePrev = enclClassName;
            enclClassName = tree.name;
            if(!isFunctionalClass()){
	            if ((tree.mods.flags & INTERFACE) != 0) {
	                print("interfaces not supported: " + tree.name);
	                return;
	                /*printTypeParameters(tree.typarams);
	                if (tree.implementing.nonEmpty()) {
	                    print(" extends ");
	                    printExprs(tree.implementing);
	                }*/
	            } else {
	                if ((tree.mods.flags & ENUM) != 0){
	                	print("enum " + tree.name);
	                }
	                else
	                    print("class " + tree.name);
	                //printTypeParameters(tree.typarams);
	                if (tree.extending != null) {
	                    print(" extends ");
	                    printExpr(tree.extending);
	                }
	                /*if (tree.implementing.nonEmpty()) {
	                    print(" implements ");
	                    printExprs(tree.implementing);
	                }*/
	            }
	            print(" ");
	            if ((tree.mods.flags & ENUM) != 0) {
	                printEnumBody(tree.defs);
	            } else {
	                printBlock(tree.defs);
	            }
            }else {
            	printNoParamBlock(tree.defs);
            }
            enclClassName = enclClassNamePrev;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	
	private void processClassAnnotations(List<JCAnnotation> annotations) throws IOException{
		classAnnotationVisitor = new ClassAnnotationVisitor();
		for (List<JCAnnotation> l = annotations; l.nonEmpty(); l = l.tail) {
			if (l.head != null) {
				l.head.accept(classAnnotationVisitor);
            }
            //print(" ");
            //println();
            //align();
        }
	}
	
	public void printEnumBody(List<JCTree> stats) throws IOException {
        print("{");
        println();
        indent();
        boolean first = true;
        //FIXME: 
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (isEnumerator(l.head)) {
                if (!first) {
                    print(",");
                    println();
                }
                align();
                printStat(l.head);
                first = false;
            }
        }
        print(";");
        println();
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (!isEnumerator(l.head)) {
                align();
                printStat(l.head);
                println();
            }
        }
        undent();
        align();
        print("}");
    }
	
	//TODO: add  methos for ema6
	public void visitMethodDef(JCMethodDecl tree) {
        try {
            // when producing source output, omit anonymous constructors
            if (tree.name == tree.name.table.names.init &&
                    enclClassName == null &&
                    sourceOutput) return;
            println(); align();
            printDocComment(tree);
            printExpr(tree.mods);
            //printTypeParameters(tree.typarams);
            if(isFunctionalClass()){
            	print(JSKeyWords.FUNCTION+" "+tree.name);
            }else{
	            if(tree.mods != null && TreeInfo.flagNames(tree.mods.flags).contains(JavaKeyWords.STATIC)){
	            	print(JSKeyWords.STATIC+" ");
	            }
	            if (tree.name == tree.name.table.names.init) {
	                //print(enclClassName != null ? enclClassName : tree.name);
	            	print(CONSTRUCTOR);
	            } else {
	                //printExpr(tree.restype);
	                print(tree.name);
	            	//print("constructor");
	            }
            }
            print("(");
            //TODO: chek whats this
            if (tree.recvparam!=null) {
                printExpr(tree.recvparam);
                if (tree.params.size() > 0) {
                    print(", ");
                }
            }
            setMethodArg(true);
            printExprs(tree.params);
            setMethodArg(false);
            print(")");
            /*if (tree.thrown.nonEmpty()) {
                print(" throws ");
                printExprs(tree.thrown);
            }*/
            //TODO: fix how
            if (tree.defaultValue != null) {
                print(" default ");
                printExpr(tree.defaultValue);
            }
            if (tree.body != null) {
                print(" ");
                printStat(tree.body);
            } else {
                print(";");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	
	private boolean isFunctionalClass() {
		return classAnnotationVisitor.isFunctionalClass();
	}

	public void visitBlock(JCBlock tree) {
        try {
        	//###############
        	//TODO: check if static block, remove the staic and the brackets.
            //printFlags(tree.flags); 
        	//FIXME: chk id any flags avil for static blocks
        	if(TreeInfo.flagNames(tree.flags).trim().equals(STATIC)){
        		printNoParamBlock(tree.stats);
        	}
        	else{
        		printBlock(tree.stats);
        	}
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	
	private void printNoParamBlock(List<? extends JCTree> stats) throws IOException {
       //  print("{");
        println();
       // indent();
        printStats(stats);
       // undent();
       // align();
       //  print("}");
    }
	
	public void visitExec(JCExpressionStatement tree) {
        try {
            printExpr(tree.expr);
            if (prec == TreeInfo.notExpression) print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	
	 public void visitApply(JCMethodInvocation tree) {
        try {
            if (!tree.typeargs.isEmpty()) {
                if (tree.meth.hasTag(SELECT)) {
                    JCFieldAccess left = (JCFieldAccess)tree.meth;
                    printExpr(left.selected);
                    /*print(".<");
                    printExprs(tree.typeargs);
                    print(">" + left.name);*/
                } else {
                    /*print("<");
                    printExprs(tree.typeargs);
                    print(">");*/
                    printExpr(tree.meth);
                }
            } else {
                printExpr(tree.meth);
            }
            print("(");
            printExprs(tree.args);
            print(")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	 //FIXME: add the const for final variables
	public void visitVarDef(JCVariableDecl tree) {
        try {
            if (docComments != null && docComments.hasComment(tree)) {
                println(); align();
            }
            printDocComment(tree);
            //TODO: need to check, how this enum comes in var decl
            if ((tree.mods.flags & ENUM) != 0) {
                //print("/*public static final*/ ");
            	//print(tree.vartype +" ");
                print(tree.name);
                if (tree.init != null) {
                    if (sourceOutput && tree.init.hasTag(NEWCLASS)) {
                        print(" /*enum*/ ");
                        JCNewClass init = (JCNewClass) tree.init;
                        if (init.args != null && init.args.nonEmpty()) {
                            print("(");
                            print(init.args);
                            print(")");
                        }
                        if (init.def != null && init.def.defs != null) {
                            print(" ");
                            printBlock(init.def.defs);
                        }
                        return;
                    }
                    /*print(" /* = ");
                    print(" = ");
                    printExpr(tree.init);
                    print(" * /");*/
                }
            } else {
                //printExpr(tree.mods);
            	if(tree.mods != null && TreeInfo.flagNames(tree.mods.flags).contains(JavaKeyWords.FINAL)){
                	print(JSKeyWords.CONST+" ");
                }
                /*if ((tree.mods.flags & VARARGS) != 0) {
                    JCTree vartype = tree.vartype;
                    List<JCAnnotation> tas = null;
                    if (vartype instanceof JCAnnotatedType) {
                        tas = ((JCAnnotatedType)vartype).annotations;
                        vartype = ((JCAnnotatedType)vartype).underlyingType;
                    }
                    printExpr(((JCArrayTypeTree) vartype).elemtype);
                    if (tas != null) {
                        print(' ');
                        printTypeAnnotations(tas);
                    }
                    print("... " + tree.name);
                } else {
                    printExpr(tree.vartype);*/
            		if(!isMethodArg())
            			print(LET+" "); 
                    print(tree.name);
                //}
                if (tree.init != null) {
                    print(" = ");
                    printExpr(tree.init);
                }
                if (prec == TreeInfo.notExpression) print(";");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	
	public void visitImport(JCImport tree) {
        try {
        	String file = tree.qualid.toString(); 
            if (tree.staticImport){
            	//TODO: check if selected can do this
            	file = file.substring(0, file.lastIndexOf("\\.")); 
            }
            file = toJsFormat(file);
            print(getScriptTag(file));
            println();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

	private String getScriptTag(String file) {
		return "<script src=\""+file+"\"/> ";
	}

	private String toJsFormat(String file) {
		if(file.endsWith(".java"))
			file = file.replace("\\.java", "");
		return replacePathSerperators(file)+".js";
	}

	private String replacePathSerperators(String file) {
		return file.replaceAll("\\.", "/");
	}
	
	public void visitLiteral(JCLiteral tree) {
        try {
            switch (tree.typetag) {
                case INT:
                    print(tree.value.toString());
                    break;
                case LONG:
                    print(tree.value);
                    break;
                case FLOAT:
                    print(tree.value);
                    break;
                case DOUBLE:
                    print(tree.value.toString());
                    break;
                case CHAR:
                    print("\'" +
                            Convert.quote(
                            String.valueOf((char)((Number)tree.value).intValue())) +
                            "\'");
                    break;
                case BOOLEAN:
                    print(((Number)tree.value).intValue() == 1 ? "true" : "false");
                    break;
                case BOT:
                    print("null");
                    break;
                default:
                    print("\"" + Convert.quote(tree.value.toString()) + "\"");
                    break;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

	public boolean isMethodArg() {
		return methodArg;
	}

	public void setMethodArg(boolean methodArg) {
		this.methodArg = methodArg;
	}
	
	public void visitNewClass(JCNewClass tree) {
        try {
            if (tree.encl != null) {
                printExpr(tree.encl);
                print(".");
            }
            print("new ");
            /*if (!tree.typeargs.isEmpty()) {
                print("<");
                printExprs(tree.typeargs);
                print(">");
            }
            if (tree.def != null && tree.def.mods.annotations.nonEmpty()) {
                printTypeAnnotations(tree.def.mods.annotations);
            }*/
            printExpr(tree.clazz);
            print("(");
            printExprs(tree.args);
            print(")");
            if (tree.def != null) {
                Name enclClassNamePrev = enclClassName;
                enclClassName =
                        tree.def.name != null ? tree.def.name :
                            tree.type != null && tree.type.tsym.name != tree.type.tsym.name.table.names.empty
                                ? tree.type.tsym.name : null;
                if ((tree.def.mods.flags & Flags.ENUM) != 0) print("/*enum*/");
                printBlock(tree.def.defs);
                enclClassName = enclClassNamePrev;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	
	 public void visitTypeApply(JCTypeApply tree) {
        try {
            printExpr(tree.clazz);
            //print("<");
            //printExprs(tree.arguments);
            //print(">");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	 
	 public void visitTypeCast(JCTypeCast tree) {
        try {
            /*open(prec, TreeInfo.prefixPrec);
            print("(");
            printExpr(tree.clazz);
            print(")");*/
            printExpr(tree.expr, TreeInfo.prefixPrec);
            //close(prec, TreeInfo.prefixPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	 
	 public void visitLambda(JCLambda tree) {
        try {
            print("(");
            if (tree.paramKind == JCLambda.ParameterKind.EXPLICIT) {
            	setMethodArg(true);
            	printExprs(tree.params);
                setMethodArg(false);
            } else {
                String sep = "";
                for (JCVariableDecl param : tree.params) {
                    print(sep);
                    print(param.name);
                    sep = ",";
                }
            }
            print(")=>");
            printExpr(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	 
	public void visitModifiers(JCModifiers mods) {
        try {
            printAnnotations(mods.annotations);
            //printFlags(mods.flags);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
	
	public void visitReference(JCMemberReference tree) {
        try {
        	print(JSKeyWords.PARAM_VAR+" => ");
            printExpr(tree.expr);
            /*print("::");
            if (tree.typeargs != null) {
                print("<");
                printExprs(tree.typeargs);
                print(">");
            }*/
            print(tree.getMode() == ReferenceMode.INVOKE ? prepareReferenceParam(tree.name.toString()) : "new");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

	private String prepareReferenceParam(String methodName) {
		return "."+methodName+"("+JSKeyWords.PARAM_VAR+") ";
	}
	
	public void printAnnotations(List<JCAnnotation> trees) throws IOException {
        for (List<JCAnnotation> l = trees; l.nonEmpty(); l = l.tail) {
            printStat(l.head);
            print(" ");
            //println();
            //align();
        }
    }
	
	public void visitAnnotation(JCAnnotation tree) {
        try {
            /*print("@");
            printExpr(tree.annotationType);
            print("(");
            printExprs(tree.args);
            print(")");*/
        	print(AnnotationMapper.mapAnnotation((JCIdent)tree.annotationType));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
