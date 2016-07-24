package com.ten01.jsmappertools.javac.main;

import static com.ten01.jsmappertools.javac.main.OptionJs.ChoiceKind.ANYOF;
import static com.ten01.jsmappertools.javac.main.OptionJs.ChoiceKind.ONEOF;
import static com.ten01.jsmappertools.javac.main.OptionJs.OptionGroup.BASIC;
import static com.ten01.jsmappertools.javac.main.OptionJs.OptionGroup.FILEMANAGER;
import static com.ten01.jsmappertools.javac.main.OptionJs.OptionGroup.INFO;
import static com.ten01.jsmappertools.javac.main.OptionJs.OptionKind.EXTENDED;
import static com.ten01.jsmappertools.javac.main.OptionJs.OptionKind.HIDDEN;
import static com.ten01.jsmappertools.javac.main.OptionJs.OptionKind.STANDARD;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;

import com.sun.tools.doclint.DocLint;
import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.Profile;
import com.sun.tools.javac.jvm.Target;
import com.ten01.jsmappertools.javac.main.OptionJs.ChoiceKind;
import com.ten01.jsmappertools.javac.main.OptionJs.OptionGroup;
import com.ten01.jsmappertools.javac.main.OptionJs.OptionKind;
import com.ten01.jsmappertools.javac.main.OptionJs.PkgInfo;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.StringUtils;
import com.sun.tools.javac.util.Log.PrefixKind;
import com.sun.tools.javac.util.Log.WriterKind;
import com.sun.tools.javac.util.Options;

public enum OptionJs{
    G("-g", "opt.g", STANDARD, BASIC),

    G_NONE("-g:none", "opt.g.none", STANDARD, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            helper.put("-g:", "none");
            return false;
        }
    },

    G_CUSTOM("-g:",  "opt.g.lines.vars.source",
            STANDARD, BASIC, ANYOF, "lines", "vars", "source"),

    XLINT("-Xlint", "opt.Xlint", EXTENDED, BASIC),

    XLINT_CUSTOM("-Xlint:", "opt.Xlint.suboptlist",
            EXTENDED,   BASIC, ANYOF, getXLintChoices()),

    XDOCLINT("-Xdoclint", "opt.Xdoclint", EXTENDED, BASIC),

    XDOCLINT_CUSTOM("-Xdoclint:", "opt.Xdoclint.subopts", "opt.Xdoclint.custom", EXTENDED, BASIC) {
        @Override
        public boolean matches(String option) {
            return DocLint.isValidOption(
                    option.replace(XDOCLINT_CUSTOM.text, DocLint.XMSGS_CUSTOM_PREFIX));
        }

        @Override
        public boolean process(OptionHelperJs helper, String option) {
            String prev = helper.get(XDOCLINT_CUSTOM);
            String next = (prev == null) ? option : (prev + " " + option);
            helper.put(XDOCLINT_CUSTOM.text, next);
            return false;
        }
    },

    // -nowarn is retained for command-line backward compatibility
    NOWARN("-nowarn", "opt.nowarn", STANDARD, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            helper.put("-Xlint:none", option);
            return false;
        }
    },

    VERBOSE("-verbose", "opt.verbose", STANDARD, BASIC),

    // -deprecation is retained for command-line backward compatibility
    DEPRECATION("-deprecation", "opt.deprecation", STANDARD, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            helper.put("-Xlint:deprecation", option);
            return false;
        }
    },

    CLASSPATH("-classpath", "opt.arg.path", "opt.classpath", STANDARD, FILEMANAGER),

    CP("-cp", "opt.arg.path", "opt.classpath", STANDARD, FILEMANAGER) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String arg) {
            return super.process(helper, "-classpath", arg);
        }
    },

    SOURCEPATH("-sourcepath", "opt.arg.path", "opt.sourcepath", STANDARD, FILEMANAGER),

    BOOTCLASSPATH("-bootclasspath", "opt.arg.path", "opt.bootclasspath", STANDARD, FILEMANAGER) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String arg) {
            helper.remove("-Xbootclasspath/p:");
            helper.remove("-Xbootclasspath/a:");
            return super.process(helper, option, arg);
        }
    },

    XBOOTCLASSPATH_PREPEND("-Xbootclasspath/p:", "opt.arg.path", "opt.Xbootclasspath.p", EXTENDED, FILEMANAGER),

    XBOOTCLASSPATH_APPEND("-Xbootclasspath/a:", "opt.arg.path", "opt.Xbootclasspath.a", EXTENDED, FILEMANAGER),

    XBOOTCLASSPATH("-Xbootclasspath:", "opt.arg.path", "opt.bootclasspath", EXTENDED, FILEMANAGER) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String arg) {
            helper.remove("-Xbootclasspath/p:");
            helper.remove("-Xbootclasspath/a:");
            return super.process(helper, "-bootclasspath", arg);
        }
    },

    EXTDIRS("-extdirs", "opt.arg.dirs", "opt.extdirs", STANDARD, FILEMANAGER),

    DJAVA_EXT_DIRS("-Djava.ext.dirs=", "opt.arg.dirs", "opt.extdirs", EXTENDED, FILEMANAGER) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String arg) {
            return super.process(helper, "-extdirs", arg);
        }
    },

    ENDORSEDDIRS("-endorseddirs", "opt.arg.dirs", "opt.endorseddirs", STANDARD, FILEMANAGER),

    DJAVA_ENDORSED_DIRS("-Djava.endorsed.dirs=", "opt.arg.dirs", "opt.endorseddirs", EXTENDED, FILEMANAGER) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String arg) {
            return super.process(helper, "-endorseddirs", arg);
        }
    },

    PROC("-proc:", "opt.proc.none.only", STANDARD, BASIC,  ONEOF, "none", "only"),

    PROCESSOR("-processor", "opt.arg.class.list", "opt.processor", STANDARD, BASIC),

    PROCESSORPATH("-processorpath", "opt.arg.path", "opt.processorpath", STANDARD, FILEMANAGER),

    PARAMETERS("-parameters","opt.parameters", STANDARD, BASIC),

    D("-d", "opt.arg.directory", "opt.d", STANDARD, FILEMANAGER),

    S("-s", "opt.arg.directory", "opt.sourceDest", STANDARD, FILEMANAGER),

    H("-h", "opt.arg.directory", "opt.headerDest", STANDARD, FILEMANAGER),

    IMPLICIT("-implicit:", "opt.implicit", STANDARD, BASIC, ONEOF, "none", "class"),

    ENCODING("-encoding", "opt.arg.encoding", "opt.encoding", STANDARD, FILEMANAGER) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String operand) {
            return super.process(helper, option, operand);
        }

    },

    SOURCE("-source", "opt.arg.release", "opt.source", STANDARD, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String operand) {
            Source source = Source.lookup(operand);
            if (source == null) {
                helper.error("err.invalid.source", operand);
                return true;
            }
            return super.process(helper, option, operand);
        }
    },

    TARGET("-target", "opt.arg.release", "opt.target", STANDARD, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String operand) {
            Target target = Target.lookup(operand);
            if (target == null) {
                helper.error("err.invalid.target", operand);
                return true;
            }
            return super.process(helper, option, operand);
        }
    },

    PROFILE("-profile", "opt.arg.profile", "opt.profile", STANDARD, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String operand) {
            Profile profile = Profile.lookup(operand);
            if (profile == null) {
                helper.error("err.invalid.profile", operand);
                return true;
            }
            return super.process(helper, option, operand);
        }
    },

    VERSION("-version", "opt.version", STANDARD, INFO) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            Log log = helper.getLog();
            String ownName = helper.getOwnName();
            log.printLines(PrefixKind.JAVAC, "version", ownName,  JavaJsCompiler.version());
            return super.process(helper, option);
        }
    },

    FULLVERSION("-fullversion", null, HIDDEN, INFO) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            Log log = helper.getLog();
            String ownName = helper.getOwnName();
            log.printLines(PrefixKind.JAVAC, "fullVersion", ownName,  JavaJsCompiler.fullVersion());
            return super.process(helper, option);
        }
    },

    DIAGS("-XDdiags=", null, HIDDEN, INFO) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            option = option.substring(option.indexOf('=') + 1);
            String diagsOption = option.contains("%") ?
                "-XDdiagsFormat=" :
                "-XDdiags=";
            diagsOption += option;
            if (XD.matches(diagsOption))
                return XD.process(helper, diagsOption);
            else
                return false;
        }
    },

    HELP("-help", "opt.help", STANDARD, INFO) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            Log log = helper.getLog();
            String ownName = helper.getOwnName();
            log.printLines(PrefixKind.JAVAC, "msg.usage.header", ownName);
            for (OptionJs o: getJavaCompilerOptions()) {
                o.help(log, OptionKind.STANDARD);
            }
            log.printNewline();
            return super.process(helper, option);
        }
    },

    A("-A", "opt.arg.key.equals.value", "opt.A", STANDARD, BASIC, true) {
        @Override
        public boolean matches(String arg) {
            return arg.startsWith("-A");
        }

        @Override
        public boolean hasArg() {
            return false;
        }
        // Mapping for processor options created in
        // JavacProcessingEnvironment
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            int argLength = option.length();
            if (argLength == 2) {
                helper.error("err.empty.A.argument");
                return true;
            }
            int sepIndex = option.indexOf('=');
            String key = option.substring(2, (sepIndex != -1 ? sepIndex : argLength) );
            if (!JavacProcessingEnvironment.isValidOptionName(key)) {
                helper.error("err.invalid.A.key", option);
                return true;
            }
            return process(helper, option, option);
        }
    },

    X("-X", "opt.X", STANDARD, INFO) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            Log log = helper.getLog();
            for (OptionJs o: getJavaCompilerOptions()) {
                o.help(log, OptionKind.EXTENDED);
            }
            log.printNewline();
            log.printLines(PrefixKind.JAVAC, "msg.usage.nonstandard.footer");
            return super.process(helper, option);
        }
    },

    // This option exists only for the purpose of documenting itself.
    // It's actually implemented by the launcher.
    J("-J", "opt.arg.flag", "opt.J", STANDARD, INFO, true) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            throw new AssertionError
                ("the -J flag should be caught by the launcher.");
        }
    },

    MOREINFO("-moreinfo", null, HIDDEN, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            Type.moreInfo = true;
            return super.process(helper, option);
        }
    },

    // treat warnings as errors
    WERROR("-Werror", "opt.Werror", STANDARD, BASIC),

    // prompt after each error
    // new Option("-prompt",                                        "opt.prompt"),
    PROMPT("-prompt", null, HIDDEN, BASIC),

    // dump stack on error
    DOE("-doe", null, HIDDEN, BASIC),

    // output source after type erasure
    PRINTSOURCE("-printsource", null, HIDDEN, BASIC),

    // display warnings for generic unchecked operations
    WARNUNCHECKED("-warnunchecked", null, HIDDEN, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            helper.put("-Xlint:unchecked", option);
            return false;
        }
    },

    XMAXERRS("-Xmaxerrs", "opt.arg.number", "opt.maxerrs", EXTENDED, BASIC),

    XMAXWARNS("-Xmaxwarns", "opt.arg.number", "opt.maxwarns", EXTENDED, BASIC),

    XSTDOUT("-Xstdout", "opt.arg.file", "opt.Xstdout", EXTENDED, INFO) {
        @Override
        public boolean process(OptionHelperJs helper, String option, String arg) {
            try {
                Log log = helper.getLog();
                // TODO: this file should be closed at the end of compilation
                log.setWriters(new PrintWriter(new FileWriter(arg), true));
            } catch (java.io.IOException e) {
                helper.error("err.error.writing.file", arg, e);
                return true;
            }
            return super.process(helper, option, arg);
        }
    },

    XPRINT("-Xprint", "opt.print", EXTENDED, BASIC),

    XPRINTROUNDS("-XprintRounds", "opt.printRounds", EXTENDED, BASIC),

    XPRINTPROCESSORINFO("-XprintProcessorInfo", "opt.printProcessorInfo", EXTENDED, BASIC),

    XPREFER("-Xprefer:", "opt.prefer", EXTENDED, BASIC, ONEOF, "source", "newer"),

    // see enum PkgInfo
    XPKGINFO("-Xpkginfo:", "opt.pkginfo", EXTENDED, BASIC, ONEOF, "always", "legacy", "nonempty"),

    /* -O is a no-op, accepted for backward compatibility. */
    O("-O", null, HIDDEN, BASIC),

    /* -Xjcov produces tables to support the code coverage tool jcov. */
    XJCOV("-Xjcov", null, HIDDEN, BASIC),

    PLUGIN("-Xplugin:", "opt.arg.plugin", "opt.plugin", EXTENDED, BASIC) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            String p = option.substring(option.indexOf(':') + 1);
            String prev = helper.get(PLUGIN);
            helper.put(PLUGIN.text, (prev == null) ? p : prev + '\0' + p.trim());
            return false;
        }
    },

    XDIAGS("-Xdiags:", "opt.diags", EXTENDED, BASIC, ONEOF, "compact", "verbose"),

    /* This is a back door to the compiler's option table.
     * -XDx=y sets the option x to the value y.
     * -XDx sets the option x to the value x.
     */
    XD("-XD", null, HIDDEN, BASIC) {
        @Override
        public boolean matches(String s) {
            return s.startsWith(text);
        }
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            option = option.substring(text.length());
            int eq = option.indexOf('=');
            String key = (eq < 0) ? option : option.substring(0, eq);
            String value = (eq < 0) ? option : option.substring(eq+1);
            helper.put(key, value);
            return false;
        }
    },

    // This option exists only for the purpose of documenting itself.
    // It's actually implemented by the CommandLine class.
    AT("@", "opt.arg.file", "opt.AT", STANDARD, INFO, true) {
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            throw new AssertionError("the @ flag should be caught by CommandLine.");
        }
    },

    /*
     * TODO: With apt, the matches method accepts anything if
     * -XclassAsDecls is used; code elsewhere does the lookup to
     * see if the class name is both legal and found.
     *
     * In apt, the process method adds the candidate class file
     * name to a separate list.
     */
    SOURCEFILE("sourcefile", null, HIDDEN, INFO) {
        @Override
        public boolean matches(String s) {
            return s.endsWith(".java")  // Java source file
                || SourceVersion.isName(s);   // Legal type name
        }
        @Override
        public boolean process(OptionHelperJs helper, String option) {
            if (option.endsWith(".java") ) {
                File f = new File(option);
                if (!f.exists()) {
                    helper.error("err.file.not.found", f);
                    return true;
                }
                if (!f.isFile()) {
                    helper.error("err.file.not.file", f);
                    return true;
                }
                helper.addFile(f);
            } else {
                helper.addClassName(option);
            }
            return false;
        }
    };

    /** The kind of an Option. This is used by the -help and -X options. */
    public enum OptionKind {
        /** A standard option, documented by -help. */
        STANDARD,
        /** An extended option, documented by -X. */
        EXTENDED,
        /** A hidden option, not documented. */
        HIDDEN,
    }

    /** The group for an Option. This determines the situations in which the
     *  option is applicable. */
    enum OptionGroup {
        /** A basic option, available for use on the command line or via the
         *  Compiler API. */
        BASIC,
        /** An option for javac's standard JavaFileManager. Other file managers
         *  may or may not support these options. */
        FILEMANAGER,
        /** A command-line option that requests information, such as -help. */
        INFO,
        /** A command-line "option" representing a file or class name. */
        OPERAND
    }

    /** The kind of choice for "choice" options. */
    enum ChoiceKind {
        /** The expected value is exactly one of the set of choices. */
        ONEOF,
        /** The expected value is one of more of the set of choices. */
        ANYOF
    }

    public final String text;

    final OptionKind kind;

    final OptionGroup group;

    /** Documentation key for arguments.
     */
    final String argsNameKey;

    /** Documentation key for description.
     */
    final String descrKey;

    /** Suffix option (-foo=bar or -foo:bar)
     */
    final boolean hasSuffix;

    /** The kind of choices for this option, if any.
     */
    final ChoiceKind choiceKind;

    /** The choices for this option, if any, and whether or not the choices
     *  are hidden
     */
    final Map<String,Boolean> choices;


    OptionJs(String text, String descrKey,
            OptionKind kind, OptionGroup group) {
        this(text, null, descrKey, kind, group, null, null, false);
    }

    OptionJs(String text, String argsNameKey, String descrKey,
            OptionKind kind, OptionGroup group) {
        this(text, argsNameKey, descrKey, kind, group, null, null, false);
    }

    OptionJs(String text, String argsNameKey, String descrKey,
            OptionKind kind, OptionGroup group, boolean doHasSuffix) {
        this(text, argsNameKey, descrKey, kind, group, null, null, doHasSuffix);
    }

    OptionJs(String text, String descrKey,
            OptionKind kind, OptionGroup group,
            ChoiceKind choiceKind, Map<String,Boolean> choices) {
        this(text, null, descrKey, kind, group, choiceKind, choices, false);
    }

    OptionJs(String text, String descrKey,
            OptionKind kind, OptionGroup group,
            ChoiceKind choiceKind, String... choices) {
        this(text, null, descrKey, kind, group, choiceKind,
                createChoices(choices), false);
    }
    // where
        private static Map<String,Boolean> createChoices(String... choices) {
            Map<String,Boolean> map = new LinkedHashMap<String,Boolean>();
            for (String c: choices)
                map.put(c, false);
            return map;
        }

    private OptionJs(String text, String argsNameKey, String descrKey,
            OptionKind kind, OptionGroup group,
            ChoiceKind choiceKind, Map<String,Boolean> choices,
            boolean doHasSuffix) {
        this.text = text;
        this.argsNameKey = argsNameKey;
        this.descrKey = descrKey;
        this.kind = kind;
        this.group = group;
        this.choiceKind = choiceKind;
        this.choices = choices;
        char lastChar = text.charAt(text.length()-1);
        this.hasSuffix = doHasSuffix || lastChar == ':' || lastChar == '=';
    }

    public String getText() {
        return text;
    }

    public OptionKind getKind() {
        return kind;
    }

    public boolean hasArg() {
        return argsNameKey != null && !hasSuffix;
    }

    public boolean matches(String option) {
        if (!hasSuffix)
            return option.equals(text);

        if (!option.startsWith(text))
            return false;

        if (choices != null) {
            String arg = option.substring(text.length());
            if (choiceKind == ChoiceKind.ONEOF)
                return choices.keySet().contains(arg);
            else {
                for (String a: arg.split(",+")) {
                    if (!choices.keySet().contains(a))
                        return false;
                }
            }
        }

        return true;
    }

    public boolean process(OptionHelperJs helper, String option, String arg) {
        if (choices != null) {
            if (choiceKind == ChoiceKind.ONEOF) {
                // some clients like to see just one of option+choice set
                for (String s: choices.keySet())
                    helper.remove(option + s);
                String opt = option + arg;
                helper.put(opt, opt);
                // some clients like to see option (without trailing ":")
                // set to arg
                String nm = option.substring(0, option.length() - 1);
                helper.put(nm, arg);
            } else {
                // set option+word for each word in arg
                for (String a: arg.split(",+")) {
                    String opt = option + a;
                    helper.put(opt, opt);
                }
            }
        }
        helper.put(option, arg);
        return false;
    }

    public boolean process(OptionHelperJs helper, String option) {
        if (hasSuffix)
            return process(helper, text, option.substring(text.length()));
        else
            return process(helper, option, option);
    }

    void help(Log log, OptionKind kind) {
        if (this.kind != kind)
            return;

        log.printRawLines(WriterKind.NOTICE,
                String.format("  %-26s %s",
                    helpSynopsis(log),
                    log.localize(PrefixKind.JAVAC, descrKey)));

    }

    private String helpSynopsis(Log log) {
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        if (argsNameKey == null) {
            if (choices != null) {
                String sep = "{";
                for (Map.Entry<String,Boolean> e: choices.entrySet()) {
                    if (!e.getValue()) {
                        sb.append(sep);
                        sb.append(e.getKey());
                        sep = ",";
                    }
                }
                sb.append("}");
            }
        } else {
            if (!hasSuffix)
                sb.append(" ");
            sb.append(log.localize(PrefixKind.JAVAC, argsNameKey));

        }

        return sb.toString();
    }

    // For -XpkgInfo:value
    public enum PkgInfo {
        /**
         * Always generate package-info.class for every package-info.java file.
         * The file may be empty if there annotations with a RetentionPolicy
         * of CLASS or RUNTIME.  This option may be useful in conjunction with
         * build systems (such as Ant) that expect javac to generate at least
         * one .class file for every .java file.
         */
        ALWAYS,
        /**
         * Generate a package-info.class file if package-info.java contains
         * annotations. The file may be empty if all the annotations have
         * a RetentionPolicy of SOURCE.
         * This value is just for backwards compatibility with earlier behavior.
         * Either of the other two values are to be preferred to using this one.
         */
        LEGACY,
        /**
         * Generate a package-info.class file if and only if there are annotations
         * in package-info.java to be written into it.
         */
        NONEMPTY;

        public static PkgInfo get(Options options) {
            String v = options.get(XPKGINFO.text);
            return (v == null
                    ? PkgInfo.LEGACY
                    : PkgInfo.valueOf(StringUtils.toUpperCase(v)));
        }
    }

    private static Map<String,Boolean> getXLintChoices() {
        Map<String,Boolean> choices = new LinkedHashMap<String,Boolean>();
        choices.put("all", false);
        for (Lint.LintCategory c : Lint.LintCategory.values())
            choices.put(c.option, c.hidden);
        for (Lint.LintCategory c : Lint.LintCategory.values())
            choices.put("-" + c.option, c.hidden);
        choices.put("none", false);
        return choices;
    }

    static Set<OptionJs> getJavaCompilerOptions() {
        return EnumSet.allOf(OptionJs.class);
    }

    public static Set<OptionJs> getJavacFileManagerOptions() {
        return getOptions(EnumSet.of(FILEMANAGER));
    }

    public static Set<OptionJs> getJavacToolOptions() {
        return getOptions(EnumSet.of(BASIC));
    }

    static Set<OptionJs> getOptions(Set<OptionGroup> desired) {
        Set<OptionJs> options = EnumSet.noneOf(OptionJs.class);
        for (OptionJs option : OptionJs.values())
            if (desired.contains(option.group))
                options.add(option);
        return Collections.unmodifiableSet(options);
    }

}