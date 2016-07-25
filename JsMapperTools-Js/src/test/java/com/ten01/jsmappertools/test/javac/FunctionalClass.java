package com.ten01.jsmappertools.test.javac;

import com.ten01.jsmapper.js.BaseJs;
import com.ten01.jsmapper.js.annotations.FunctionalStyle;

/**
 * @author chidveer chinthauntla
 */
@FunctionalStyle
public class FunctionalClass implements BaseJs{
	
	static{
		SimpleClass sc = new SimpleClass();
		sc.sayHello();
	}
	
	void imAFunction(){
		console.log("I'm a function");
	}
	
	

}
