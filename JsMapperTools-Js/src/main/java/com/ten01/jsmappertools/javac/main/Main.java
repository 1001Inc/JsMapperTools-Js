package com.ten01.jsmappertools.javac.main;

import static com.sun.tools.javac.main.Option.FULLVERSION;
import static com.sun.tools.javac.main.Option.HELP;
import static com.sun.tools.javac.main.Option.PLUGIN;
import static com.sun.tools.javac.main.Option.VERSION;
import static com.sun.tools.javac.main.Option.X;
import static com.sun.tools.javac.main.Option.XDOCLINT;
import static com.sun.tools.javac.main.Option.XDOCLINT_CUSTOM;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.doclint.DocLint;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.file.CacheFSInfo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.CommandLine;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.processing.AnnotationProcessingError;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.ClientCodeException;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Log.PrefixKind;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.PropagatedException;
import com.sun.tools.javac.util.ServiceLoader;
import com.ten01.jsmappertools.javac.processing.JavacJsProcessingEnvironment;

/*
 * @author chidveer chinthauntla
 *
 * Copied from com.sun.tools.javac.main.Main.
 * OptionHeloper functionality is removed. 
 * @see com.sun.tools.javac.main.Main
 */
public class Main extends com.sun.tools.javac.main.Main{
	
	//TODO: use spring @InClassPath to determine the classes to ignore
	// from the javac - ex: "BaseJs."
	
	public Main(String name) {
        super(name);
    }

    /**
     * Construct a compiler instance.
     */
    public Main(String name, PrintWriter out) {
        super(name, out);
    }
    
	public Result translate(String[] args) {
		Context context = new Context();
        JavacFileManager.preRegister(context);
        Result result = translate(args, context);
        if (fileManager instanceof JavacFileManager) {
            ((JavacFileManager)fileManager).close();
        }
        return result;
	}
    
	public Result translate(String[] args, Context context) {
        return translate(args, context, List.<JavaFileObject>nil(), null);
    }

    public Result translate(String[] args,
                       Context context,
                       List<JavaFileObject> fileObjects,
                       Iterable<? extends Processor> processors)
    {
        return translate(args,  null, context, fileObjects, processors);
    }

    public Result translate(String[] args,
                          String[] classNames,
                          Context context,
                          List<JavaFileObject> fileObjects,
                          Iterable<? extends Processor> processors){
    	context.put(Log.outKey, out);
        log = Log.instance(context);

        if (options == null)
            options = Options.instance(context); // creates a new one

        filenames = new LinkedHashSet<File>();
        classnames = new ListBuffer<String>();
        JavaJsCompiler comp = null;
        /*
         * TODO: Logic below about what is an acceptable command line
         * should be updated to take annotation processing semantics
         * into account.
         */
        try {
            if (args.length == 0
                    && (classNames == null || classNames.length == 0)
                    && fileObjects.isEmpty()) {
                Option.HELP.process(optionHelper, "-help");
                return Result.CMDERR;
            }

            Collection<File> files;
            try {
                files = processArgs(CommandLine.parse(args), classNames);
                if (files == null) {
                    // null signals an error in options, abort
                    return Result.CMDERR;
                } else if (files.isEmpty() && fileObjects.isEmpty() && classnames.isEmpty()) {
                    // it is allowed to compile nothing if just asking for help or version info
                    if (options.isSet(HELP)
                        || options.isSet(X)
                        || options.isSet(VERSION)
                        || options.isSet(FULLVERSION))
                        return Result.OK;
                    if (JavaCompiler.explicitAnnotationProcessingRequested(options)) {
                        error("err.no.source.files.classes");
                    } else {
                        error("err.no.source.files");
                    }
                    return Result.CMDERR;
                }
            } catch (java.io.FileNotFoundException e) {
                warning("err.file.not.found", e.getMessage());
                return Result.SYSERR;
            }

            boolean forceStdOut = options.isSet("stdout");
            if (forceStdOut) {
                log.flush();
                log.setWriters(new PrintWriter(System.out, true));
            }

            // allow System property in following line as a Mustang legacy
            boolean batchMode = (options.isUnset("nonBatchMode")
                        && System.getProperty("nonBatchMode") == null);
            if (batchMode)
                CacheFSInfo.preRegister(context);

            // FIXME: this code will not be invoked if using JavacTask.parse/analyze/generate
            // invoke any available plugins
            String plugins = options.get(PLUGIN);
            if (plugins != null) {
                JavacProcessingEnvironment pEnv = JavacJsProcessingEnvironment.instance(context);
                ClassLoader cl = pEnv.getProcessorClassLoader();
                ServiceLoader<Plugin> sl = ServiceLoader.load(Plugin.class, cl);
                Set<List<String>> pluginsToCall = new LinkedHashSet<List<String>>();
                for (String plugin: plugins.split("\\x00")) {
                    pluginsToCall.add(List.from(plugin.split("\\s+")));
                }
                JavacTask task = null;
                Iterator<Plugin> iter = sl.iterator();
                while (iter.hasNext()) {
                    Plugin plugin = iter.next();
                    for (List<String> p: pluginsToCall) {
                        if (plugin.getName().equals(p.head)) {
                            pluginsToCall.remove(p);
                            try {
                                if (task == null)
                                    task = JavacTask.instance(pEnv);
                                plugin.init(task, p.tail.toArray(new String[p.tail.size()]));
                            } catch (Throwable ex) {
                                if (apiMode)
                                    throw new RuntimeException(ex);
                                pluginMessage(ex);
                                return Result.SYSERR;
                            }
                        }
                    }
                }
                for (List<String> p: pluginsToCall) {
                    log.printLines(PrefixKind.JAVAC, "msg.plugin.not.found", p.head);
                }
            }

            comp = JavaJsCompiler.instance(context);

            // FIXME: this code will not be invoked if using JavacTask.parse/analyze/generate
            String xdoclint = options.get(XDOCLINT);
            String xdoclintCustom = options.get(XDOCLINT_CUSTOM);
            if (xdoclint != null || xdoclintCustom != null) {
                Set<String> doclintOpts = new LinkedHashSet<String>();
                if (xdoclint != null)
                    doclintOpts.add(DocLint.XMSGS_OPTION);
                if (xdoclintCustom != null) {
                    for (String s: xdoclintCustom.split("\\s+")) {
                        if (s.isEmpty())
                            continue;
                        doclintOpts.add(s.replace(XDOCLINT_CUSTOM.text, DocLint.XMSGS_CUSTOM_PREFIX));
                    }
                }
                if (!(doclintOpts.size() == 1
                        && doclintOpts.iterator().next().equals(DocLint.XMSGS_CUSTOM_PREFIX + "none"))) {
                    JavacTask t = BasicJavacTask.instance(context);
                    // standard doclet normally generates H1, H2
                    doclintOpts.add(DocLint.XIMPLICIT_HEADERS + "2");
                    new DocLint().init(t, doclintOpts.toArray(new String[doclintOpts.size()]));
                    comp.keepComments = true;
                }
            }

            fileManager = context.get(JavaFileManager.class);

            if (!files.isEmpty()) {
                // add filenames to fileObjects
                comp = JavaJsCompiler.instance(context);
                List<JavaFileObject> otherFiles = List.nil();
                JavacFileManager dfm = (JavacFileManager)fileManager;
                for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(files))
                    otherFiles = otherFiles.prepend(fo);
                for (JavaFileObject fo : otherFiles)
                    fileObjects = fileObjects.prepend(fo);
            }
            comp.translate(fileObjects,
                         classnames.toList(),
                         processors);

            if (log.expectDiagKeys != null) {
                if (log.expectDiagKeys.isEmpty()) {
                    log.printRawLines("all expected diagnostics found");
                    return Result.OK;
                } else {
                    log.printRawLines("expected diagnostic keys not found: " + log.expectDiagKeys);
                    return Result.ERROR;
                }
            }

            if (comp.errorCount() != 0)
                return Result.ERROR;
        } catch (IOException ex) {
            ioMessage(ex);
            return Result.SYSERR;
        } catch (OutOfMemoryError ex) {
            resourceMessage(ex);
            return Result.SYSERR;
        } catch (StackOverflowError ex) {
            resourceMessage(ex);
            return Result.SYSERR;
        } catch (FatalError ex) {
            feMessage(ex);
            return Result.SYSERR;
        } catch (AnnotationProcessingError ex) {
            if (apiMode)
                throw new RuntimeException(ex.getCause());
            apMessage(ex);
            return Result.SYSERR;
        } catch (ClientCodeException ex) {
            // as specified by javax.tools.JavaCompiler#getTask
            // and javax.tools.JavaCompiler.CompilationTask#call
            throw new RuntimeException(ex.getCause());
        } catch (PropagatedException ex) {
            throw ex.getCause();
        } catch (Throwable ex) {
            // Nasty.  If we've already reported an error, compensate
            // for buggy compiler error recovery by swallowing thrown
            // exceptions.
            if (comp == null || comp.errorCount() == 0 ||
                options == null || options.isSet("dev"))
                bugMessage(ex);
            return Result.ABNORMAL;
        } finally {
            if (comp != null) {
                try {
                    comp.close();
                } catch (ClientCodeException ex) {
                    throw new RuntimeException(ex.getCause());
                }
            }
            filenames = null;
            options = null;
        }
        return Result.OK;
    	
    }

	public List<JCCompilationUnit> parseTree(String[] args) {
        Context context = new Context();
        JavacFileManager.preRegister(context); // can't create it until Log has been set up
        List<JCCompilationUnit> result = parseTree(args, context);
        if (fileManager instanceof JavacFileManager) {
            // A fresh context was created above, so jfm must be a JavacFileManager
            ((JavacFileManager)fileManager).close();
        }
        return result;
    }

    public List<JCCompilationUnit> parseTree(String[] args, Context context) {
        return parseTree(args, context, List.<JavaFileObject>nil(), null);
    }

    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public List<JCCompilationUnit> parseTree(String[] args,
                       Context context,
                       List<JavaFileObject> fileObjects,
                       Iterable<? extends Processor> processors)
    {
        return parseTree(args,  null, context, fileObjects, processors);
    }

    public List<JCCompilationUnit> parseTree(String[] args,
                          String[] classNames,
                          Context context,
                          List<JavaFileObject> fileObjects,
                          Iterable<? extends Processor> processors)
    {
        context.put(Log.outKey, out);
        log = Log.instance(context);

        if (options == null)
            options = Options.instance(context); // creates a new one

        filenames = new LinkedHashSet<File>();
        classnames = new ListBuffer<String>();
        JavaJsCompiler comp = null;
        /*
         * TODO: Logic below about what is an acceptable command line
         * should be updated to take annotation processing semantics
         * into account.
         */
        try {
        	
        	
            if (args.length == 0
                    && (classNames == null || classNames.length == 0)
                    && fileObjects.isEmpty()) {
              
            	/* FIXME: use the var 'optionHelper' when jdk updates the scope of mtds in it 
            	--Option.HELP.process(optionHelper, "-help");*/
            	
                throw new RuntimeException("Result.CMDERR");
            }

            Collection<File> files;
            try {
                files = processArgs(CommandLine.parse(args), classNames);
                if (files == null) {
                    // null signals an error in options, abort
                    throw new RuntimeException("Result.CMDERR");
                } else if (files.isEmpty() && fileObjects.isEmpty() && classnames.isEmpty()) {
                    // it is allowed to compile nothing if just asking for help or version info
                    if (options.isSet(HELP)
                        || options.isSet(X)
                        || options.isSet(VERSION)
                        || options.isSet(FULLVERSION))
                        return List.nil();
                    if (JavaCompiler.explicitAnnotationProcessingRequested(options)) {
                        error("err.no.source.files.classes");
                    } else {
                        error("err.no.source.files");
                    }
                    throw new RuntimeException("Result.CMDERR");
                }
            } catch (java.io.FileNotFoundException e) {
                warning("err.file.not.found", e.getMessage());
                throw new RuntimeException("Result.SYSERR");
            }

            boolean forceStdOut = options.isSet("stdout");
            if (forceStdOut) {
                log.flush();
                log.setWriters(new PrintWriter(System.out, true));
            }

            // allow System property in following line as a Mustang legacy
            boolean batchMode = (options.isUnset("nonBatchMode")
                        && System.getProperty("nonBatchMode") == null);
            if (batchMode)
                CacheFSInfo.preRegister(context);

            // FIXME: this code will not be invoked if using JavacTask.parse/analyze/generate
            // invoke any available plugins
            String plugins = options.get(PLUGIN);
            if (plugins != null) {
                JavacProcessingEnvironment pEnv = JavacJsProcessingEnvironment.instance(context);
                ClassLoader cl = pEnv.getProcessorClassLoader();
                ServiceLoader<Plugin> sl = ServiceLoader.load(Plugin.class, cl);
                Set<List<String>> pluginsToCall = new LinkedHashSet<List<String>>();
                for (String plugin: plugins.split("\\x00")) {
                    pluginsToCall.add(List.from(plugin.split("\\s+")));
                }
                JavacTask task = null;
                Iterator<Plugin> iter = sl.iterator();
                while (iter.hasNext()) {
                    Plugin plugin = iter.next();
                    for (List<String> p: pluginsToCall) {
                        if (plugin.getName().equals(p.head)) {
                            pluginsToCall.remove(p);
                            try {
                                if (task == null)
                                    task = JavacTask.instance(pEnv);
                                plugin.init(task, p.tail.toArray(new String[p.tail.size()]));
                            } catch (Throwable ex) {
                                if (apiMode)
                                    throw new RuntimeException(ex);
                                pluginMessage(ex);
                                throw new RuntimeException("Result.SYSERR");
                            }
                        }
                    }
                }
                for (List<String> p: pluginsToCall) {
                    log.printLines(PrefixKind.JAVAC, "msg.plugin.not.found", p.head);
                }
            }
            
            comp = JavaJsCompiler.instance(context);

            // FIXME: this code will not be invoked if using JavacTask.parse/analyze/generate
            String xdoclint = options.get(XDOCLINT);
            String xdoclintCustom = options.get(XDOCLINT_CUSTOM);
            if (xdoclint != null || xdoclintCustom != null) {
                Set<String> doclintOpts = new LinkedHashSet<String>();
                if (xdoclint != null)
                    doclintOpts.add(DocLint.XMSGS_OPTION);
                if (xdoclintCustom != null) {
                    for (String s: xdoclintCustom.split("\\s+")) {
                        if (s.isEmpty())
                            continue;
                        doclintOpts.add(s.replace(XDOCLINT_CUSTOM.text, DocLint.XMSGS_CUSTOM_PREFIX));
                    }
                }
                if (!(doclintOpts.size() == 1
                        && doclintOpts.iterator().next().equals(DocLint.XMSGS_CUSTOM_PREFIX + "none"))) {
                    JavacTask t = BasicJavacTask.instance(context);
                    // standard doclet normally generates H1, H2
                    doclintOpts.add(DocLint.XIMPLICIT_HEADERS + "2");
                    new DocLint().init(t, doclintOpts.toArray(new String[doclintOpts.size()]));
                    comp.keepComments = true;
                }
            }

            fileManager = context.get(JavaFileManager.class);

            if (!files.isEmpty()) {
                // add filenames to fileObjects
                comp = JavaJsCompiler.instance(context);
                List<JavaFileObject> otherFiles = List.nil();
                JavacFileManager dfm = (JavacFileManager)fileManager;
                for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(files))
                    otherFiles = otherFiles.prepend(fo);
                for (JavaFileObject fo : otherFiles)
                    fileObjects = fileObjects.prepend(fo);
            }
            /*Ten01: comp.compile(fileObjects,
                         classnames.toList(),
                         processors);*/
            
            List<JCCompilationUnit> result =  comp.parseFiles(fileObjects);

            if (log.expectDiagKeys != null) {
                if (log.expectDiagKeys.isEmpty()) {
                    log.printRawLines("all expected diagnostics found");
                    throw new RuntimeException("Result.OK");
                } else {
                    log.printRawLines("expected diagnostic keys not found: " + log.expectDiagKeys);
                    throw new RuntimeException("Result.ERROR");
                }
            }
            if (comp.errorCount() != 0)
            	throw new RuntimeException("Result.ERROR");
            
            return result;
            
        } catch (IOException ex) {
            ioMessage(ex);
            throw new RuntimeException("Result.SYSERR");
        } catch (OutOfMemoryError ex) {
            resourceMessage(ex);
            throw new RuntimeException("Result.SYSERR");
        } catch (StackOverflowError ex) {
            resourceMessage(ex);
            throw new RuntimeException("Result.SYSERR");
        } catch (FatalError ex) {
            feMessage(ex);
            throw new RuntimeException("Result.SYSERR");
        } catch (AnnotationProcessingError ex) {
            if (apiMode)
                throw new RuntimeException(ex.getCause());
            apMessage(ex);
            throw new RuntimeException("Result.SYSERR");
        } catch (ClientCodeException ex) {
            // as specified by javax.tools.JavaCompiler#getTask
            // and javax.tools.JavaCompiler.CompilationTask#call
            throw new RuntimeException(ex.getCause());
        } catch (PropagatedException ex) {
            throw ex.getCause();
        } catch (Throwable ex) {
            // Nasty.  If we've already reported an error, compensate
            // for buggy compiler error recovery by swallowing thrown
            // exceptions.
            if (comp == null || comp.errorCount() == 0 ||
                options == null || options.isSet("dev"))
                bugMessage(ex);
            throw new RuntimeException("Result.ABNORMAL");
        } finally {
            if (comp != null) {
                try {
                    comp.close();
                } catch (ClientCodeException ex) {
                    throw new RuntimeException(ex.getCause());
                }
            }
            filenames = null;
            options = null;
        }        
    }

}
