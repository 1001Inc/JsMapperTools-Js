package com.ten01.jsmappertools.javac.parser;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.ParserFactory;

/**
 * @author chidveer chinthauntla
 */
public class JavacJsParser extends JavacParser {

	protected JavacJsParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap,
			boolean keepEndPositions) {
		super(fac, S, true, keepLineMap, keepEndPositions);
	}
	/*
	 * FIXME: if type is class, accept it, otherwise say, JavaScript will not allow other types
	 * 
	 * Rater, have a list of allowable types in a list,  and call the method, "isAllowdType()" and let go the noraml flow
	 */
	/*@Override
	JCStatement classOrInterfaceOrEnumDeclaration(JCModifiers mods, Comment dc) {
		if (token.kind == CLASS) {
            return classDeclaration(mods, dc);
        } else {
        	int pos = token.pos;
            List<JCTree> errs;
            if (LAX_IDENTIFIER.accepts(token.kind)) {
                errs = List.<JCTree>of(mods, toP(F.at(pos).Ident(ident())));
                setErrorEndPos(token.pos);
            } else {
                errs = List.<JCTree>of(mods);
            }
            return toP(F.Exec(syntaxError(pos, errs, "expected2",
                                          CLASS, INTERFACE)));
        }
	} */
	

}
