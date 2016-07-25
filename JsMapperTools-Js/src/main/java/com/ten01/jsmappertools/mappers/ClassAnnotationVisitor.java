package com.ten01.jsmappertools.mappers;

import static com.ten01.jsmapper.js.annotations.AnnotationMethodUtils.isAnnotatedWith;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.ten01.jsmapper.js.annotations.FunctionalStyle;

public class ClassAnnotationVisitor extends JCTree.Visitor{
	
	boolean functionalClass;
	
	@Override
	public void visitAnnotation(JCAnnotation annotationTree) {
		String annotationName = annotationTree.annotationType == null || ((JCIdent)annotationTree.annotationType).name == null ? null : ((JCIdent)annotationTree.annotationType).name.toString();    
		if(annotationName == null)
			return;
		if(isAnnotatedWith(annotationName, FunctionalStyle.class)){
			setFunctionalClass(true);
		}
	}

	public boolean isFunctionalClass() {
		return functionalClass;
	}

	public void setFunctionalClass(boolean functionalClass) {
		this.functionalClass = functionalClass;
	}
	
	

}
