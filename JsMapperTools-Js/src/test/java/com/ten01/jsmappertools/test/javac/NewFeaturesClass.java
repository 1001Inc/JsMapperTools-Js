package com.ten01.jsmappertools.test.javac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ten01.jsmapper.js.annotations.AsyncFunction;


public class NewFeaturesClass {
	
	//TODO: mapping array classes
	List<? extends String> list = new ArrayList<>(Arrays.asList(new String[]{"Hello1", "Hello2", "Hello3", "a", "B"}));
	
	public void doLamdas(){
		list.sort((a,b) -> {
			return a.compareTo(b);
		});
		list.sort((String a, String b) -> {
			return a.compareTo(b);
		});
		list.sort((String a, String b) -> a.compareTo(b));
	}
	
	public void doLamdasWithContext(){
		final int abc = 123;
		list.sort((a,b) -> {
			System.out.println(abc);
			return a.compareTo(b);
		});		
	}
	
	public void passParams(){
		list.forEach( System.out::println);
		
	}
	
	@AsyncFunction
	public void callAsyn(){
		System.out.println("my call is a synch");
	}

}
