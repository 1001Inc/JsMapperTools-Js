# JsMapperTools-Js
(Java to Script Mapper Tools for JavaScript)Translator for converting Java code to JavaScript.

## Motivation

Writing, testing and maintaining plain old JavaScript is tedious. The intention of scripting languages like JavaScript was to simplify development while writing "some pieces of code" but, most applications demand huge amounts of javascript. so, we simply need a new approach to the javascript, without replacing it. 
JsMapper will just do the same by borrowing the robust features of Java compiler, to help us better write, test and maintain JavaScript. Its clean and simple API will let developers write robust code in less time and manage with huge scripts effectively.

## Code Example

The following AdvancedClass is written using the API provided by the JsMapper-Js.
```java
public class AdvancedClass<K,V> extends SimpleClass implements BaseJs, Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	private List<String> list ;
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
		list.add(str[0]);
	}	
	
	void printAll(){
		for(String s : list){
			console.log(s);
		}
	}
	
	public void doLamdasWithContext(){
		final int abc = 123;
		list.sort((a,b) -> {
			console.log(abc);
			return a.compareTo(b);
		});		
		list.forEach( System.out::println);
	}
	
	@AsyncFunction
	public void callAsyn(){
		Object yield_val = yield("234");
		console.log(yield_val);
	}
	
	@GeneratorFunction
	public void generator(){
		console.log("I'm a generator");
	}
	
}
```

A class like this, will be effectively translated to JavaScript as below using JsMapperTools-Js.
```javascript
// com/ten01/jsmappertools/test/javac/AdvancedClass.js

class AdvancedClass extends SimpleClass {
    static const serialVersionUID = 1;
    list;
    key;
    value;
    
    constructor(key, value) {
        this.key = key;
        this.value = value;
    }
    
    static main(args) {
        document.getElementById("id").innerHTML = "Hello 123";
        let adv = new AdvancedClass("dfgd", 34);
        adv.reset();
        adv.myMethodWithVarArg("2123", "asfas");
        adv.doCasts();
        adv.printAll();
    }
    
    myMethodWithVarArg( ...str) {
        list.add(str[0]);
    }
    
    printAll() {
        for (let s of list) {
            console.log(s);
        }
    }
	
	doLamdasWithContext() {
        const let abc = 123;
        list.sort((a,b)=>{
            console.log(abc);
            return a.compareTo(b);
        });
		list.forEach(param => System.out.println(param) );
    }
  
    async callAsyn() {
        console.log("my call is a synch");
        let yield_val =  yield "234" ;
        console.log(yield_val);
    }
    
    function* generator() {
        console.log("I\'m a generator");
    }    
    
}
```

It also lets you use the compile time features like -D for output directory and so on.

## Installation
```sh
cd <dir>/JsMapper-Js/JsMapper-Js
mvn clean install  
```

## Tests
```sh
cd <dri>/JsMapperTools-Js/JsMapperTools-Js 
mvn clean test -pl :JsMapperTools-Js -Dtest=AdvancedJsMappingTest
```

## Contributors

  - `Twitter`: [@Chidveer](https://twitter.com/chidveer)
