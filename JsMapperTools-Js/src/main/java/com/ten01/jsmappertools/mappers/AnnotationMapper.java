package com.ten01.jsmappertools.mappers;

import static com.ten01.jsmapper.js.annotations.AnnotationMethodUtils.getAnnotationDefaultValue;
import static com.ten01.jsmapper.js.annotations.AnnotationMethodUtils.isAnnotatedWith;

import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.ten01.jsmapper.js.annotations.AsyncFunction;

/**
 * @author chidveer chinthauntla
 */
public class AnnotationMapper {
	
	public static String mapAnnotation(JCIdent annotation){
		try {
			String annotationName = annotation == null || annotation.name == null ? null : annotation.name.toString();    
			if(annotationName == null)
				return "";
			if(isAnnotatedWith(annotationName, AsyncFunction.class)){
				return getAnnotationDefaultValue(AsyncFunction.class);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		throw new RuntimeException("Not a valid Annotation");
	}

}
