package com.ten01.jsmappertools.test.javac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ten01.jsmapper.js.BaseJs;

/**
 * @author chidveer chinthauntla
 */
public class AdvancedClass<K,V> extends SimpleClass implements BaseJs, Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	private List<String> l ;
	K key;
	V value;

	AdvancedClass(K key, V value){
		this.key = key;
		this.value = value;
	}
	
	public static void main(String[] args) {
		document.getElementById("id").innerHTML = "Hello 123";
		AdvancedClass<String, Long> adv = new AdvancedClass<>("dfgd",34L);
		adv.reset();
		adv.myMethodWithVarArg("2123","asfas");
		adv.doCasts();
		adv.printAll();
	}
	
	private void myMethodWithVarArg(String... str) {
		l.add(str[0]);
	}
	
	@SuppressWarnings("unchecked")
	public void doCasts(){
		Object str = new String("some object");
		Object tempObj = new AdvancedClass<String, Long>("dfgd",34L);
		l.add((String)str);
		l.add(((AdvancedClass<String, Long>)tempObj).toString());
	}
	
	void printAll(){
		for(String s : l){
			console.log(s);
		}
	}

	List<String> reset(){
		l = new ArrayList<String>();
		l.add("hello");
		return l;
	}
	
	public enum HELLO{
		HELLO1, HELLO2;
	}
	
}
