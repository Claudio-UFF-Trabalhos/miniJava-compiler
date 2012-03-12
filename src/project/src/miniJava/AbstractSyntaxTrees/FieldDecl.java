/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class FieldDecl extends MemberDecl {

	public FieldDecl(boolean isPrivate, boolean isStatic, Type t, Identifier id, SourcePosition posn) {
		super(isPrivate, isStatic, t, id, posn);
	}

	public FieldDecl(MemberDecl md, SourcePosition posn) {
		super(md, posn);
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitFieldDecl(this, o);
	}
}
