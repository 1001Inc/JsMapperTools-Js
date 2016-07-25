package com.ten01.jsmappertools.test.javac;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import com.ten01.jsmappertools.commons.FileUtils;
import com.ten01.jsmappertools.javac.Main;
import com.ten01.jsmappertools.test.AbstractTest;

/**
 * @author chidveer chinthauntla
 */
public class BasicJsMappingTest extends AbstractTest {

	String[] OPTIONS_AND_FILES =  ArrayUtils.addAll(OPTIONS, FileUtils.getFilePath("src/test/java/com/ten01/jsmappertools/test/javac/SimpleClass.java"));
	
	/*
	 * use the Main class from JdkTools to convert 
	 * the SimpleClass to javaByte code to, use for debug.
	 */
	//@Test
	public void doRegularCompilation(){
		doRegularCompilation(OPTIONS_AND_FILES);
	}
	
	
	/*
	 * Use the JsMapper implemnetaion for Main class.
	 */
	@Test
	public void mapSimpleClass(){
		Main.compileToJs(OPTIONS_AND_FILES);
	}
	
	
	//Play around methods --- not needed
	
	public   void getJavaSourceAsTree() throws Exception {
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
		com.sun.tools.javac.main.Main main = new com.sun.tools.javac.main.Main("javac");
		Collection<File> files;
		JavaCompiler comp = null;
		List<JavaFileObject> fileObjects = List.<JavaFileObject>nil(); 
		JavaFileManager fileManager;
		Context context = new Context();
		Set<File> filenames = new LinkedHashSet<File>();
		ListBuffer<String> classnames = new ListBuffer<String>();
        
        JavacFileManager.preRegister(context);
        main.setOptions(Options.instance(context));
        main.filenames = filenames;
        main.classnames = classnames;
        
        Log.instance(context);
        fileManager = context.get(JavaFileManager.class);
        
        files = null;
      //  files = main.processArgs(FILES);
        
					
		comp = JavaCompiler.instance(context);
		comp.keepComments = true; //context.put(Log.outKey, pw);
        
		
        if (!files.isEmpty()) {
            // add filenames to fileObjects
            comp = JavaCompiler.instance(context);
            List<JavaFileObject> otherFiles = List.nil();
            JavacFileManager dfm = (JavacFileManager)fileManager;
            for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(files))
                otherFiles = otherFiles.prepend(fo);
            for (JavaFileObject fo : otherFiles)
                fileObjects = fileObjects.prepend(fo);
        }
		
        List<JCCompilationUnit> trees = comp.parseFiles(fileObjects);
		
        pw.flush();
        
		if (fileManager instanceof JavacFileManager) {
            // A fresh context was created above, so jfm must be a JavacFileManager
            ((JavacFileManager)fileManager).close();
        }
		
		for(JCCompilationUnit clas : trees){
			if(skip(clas)){
				continue;
			}
			File outfile =new File(OUT_PUT_DIR+File.pathSeparator +clas.sourcefile.getName());
			System.out.println(outfile.getCanonicalPath());
			StringBuilder b = new StringBuilder("(){");
			processClass(b,clas);
			
			b.append("}();");
			
		}	
		 
	}

	private void processClass(StringBuilder b, JCCompilationUnit clas) {
		for(JCTree tree : clas.defs){
			if(skip(tree)){
				continue;
			}
			if(JCMethodDecl.class.isAssignableFrom(tree.getClass())){
				//((JCMethodDecl)tree).getTag
			}
		}
		
	}

	private boolean skip(JCTree clas) {
		switch(clas.getKind()){
			case IMPORT:
			case CLASS:	
			case TYPE_CAST:
				return true;
			default:
				return false;
		}
	}
	

}
