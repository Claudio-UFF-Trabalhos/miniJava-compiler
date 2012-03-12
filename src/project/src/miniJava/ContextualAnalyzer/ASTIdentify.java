package miniJava.ContextualAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.ErrorType;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IndexedRef;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualifiedRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementType;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.SyntacticAnalyzer.SyntaxErrorException;

public class ASTIdentify implements Visitor<IdentificationTable, Void> {
	public int errorCount = 0;

	/**
	 * Creates an identification table for the program
	 * 
	 * @param ast
	 * @return IdentificationTable object
	 */
	public IdentificationTable createIdentificationTable(AST ast) {
		IdentificationTable table = new IdentificationTable();
		ast.visit(this, table);
		return table;
	}

	private void addDeclaration(IdentificationTable table, Declaration declaration) {
		try {
			table.set(declaration);
		} catch (SyntaxErrorException e) {
			reportError(e.getMessage());
		}
	}

	private void reportError(String msg) {
		errorCount++;
		System.err.println("*** " + msg);
	}

	@Override
	public Void visitPackage(Package prog, IdentificationTable table) {
		Iterator<ClassDecl> it = prog.classDeclList.iterator();
		ArrayList<Declaration> memberDeclarations = new ArrayList<Declaration>();

		while (it.hasNext()) {
			ClassDecl cd = it.next();
			addDeclaration(table, cd);

			IdentificationTable classTable = new IdentificationTable();
			cd.visit(this, classTable);

			HashMap<String, Declaration> classMembers = classTable.getClassMembers();
			if(classMembers != null) {
				Iterator<String> itm = classMembers.keySet().iterator();
				while (itm.hasNext()) {
					Declaration memberDecl = classMembers.get(itm.next());
					memberDecl.id.spelling = cd.id.spelling + "." + memberDecl.id.spelling;
					memberDeclarations.add(memberDecl);
				}
			}
		}
		
		table.openScope();
		for(int i = 0; i < memberDeclarations.size(); i++)
			addDeclaration(table, memberDeclarations.get(i));

		return null;
	}

	@Override
	public Void visitClassDecl(ClassDecl cd, IdentificationTable table) {
		table.openScope();

		Iterator<FieldDecl> itf = cd.fieldDeclList.iterator();
		while (itf.hasNext())
			itf.next().visit(this, table);

		Iterator<MethodDecl> itm = cd.methodDeclList.iterator();
		while (itm.hasNext())
			itm.next().visit(this, table);

		return null;
	}

	@Override
	public Void visitFieldDecl(FieldDecl fd, IdentificationTable table) {
		addDeclaration(table, fd);
		return null;
	}

	@Override
	public Void visitMethodDecl(MethodDecl md, IdentificationTable table) {
		addDeclaration(table, md);

		// Parameter scope
		table.openScope();

		Iterator<ParameterDecl> itp = md.parameterDeclList.iterator();
		while (itp.hasNext())
			itp.next().visit(this, table);

		// Scope of local variables
		table.openScope();

		Iterator<Statement> its = md.statementList.iterator();
		while (its.hasNext())
			its.next().visit(this, table);

		table.closeScope();

		table.closeScope();

		return null;
	}

	@Override
	public Void visitParameterDecl(ParameterDecl pd, IdentificationTable table) {
		addDeclaration(table, pd);
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl decl, IdentificationTable table) {
		addDeclaration(table, decl);
		return null;
	}

	@Override
	public Void visitBaseType(BaseType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitClassType(ClassType type, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType type, IdentificationTable table) {
		return null;
	}


	@Override
	public Void visitStatementType(StatementType type, IdentificationTable arg) {
		return null;
	}

	@Override
	public Void visitErrorType(ErrorType type, IdentificationTable arg) {
		return null;
	}

	@Override
	public Void visitBlockStmt(BlockStmt stmt, IdentificationTable table) {
		// nested scope
		table.openScope();

		Iterator<Statement> it = stmt.sl.iterator();
		while (it.hasNext())
			it.next().visit(this, table);

		table.closeScope();

		return null;
	}

	@Override
	public Void visitVardeclStmt(VarDeclStmt stmt, IdentificationTable table) {
		stmt.varDecl.visit(this, table);
		return null;
	}

	@Override
	public Void visitAssignStmt(AssignStmt stmt, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitCallStmt(CallStmt stmt, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitIfStmt(IfStmt stmt, IdentificationTable table) {
		if (stmt.thenStmt instanceof VarDeclStmt) {
			reportError("Variable declaration cannot be the only statement in a conditional statement at "
					+ stmt.thenStmt.posn);
		} else {
			stmt.thenStmt.visit(this, table);
		}

		if (stmt.elseStmt != null) {
			if (stmt.elseStmt instanceof VarDeclStmt) {
				reportError("Variable declaration cannot be the only statement in a conditional statement at "
						+ stmt.elseStmt.posn);
			} else {
				stmt.elseStmt.visit(this, table);
			}
		}

		return null;
	}

	@Override
	public Void visitWhileStmt(WhileStmt stmt, IdentificationTable table) {
		if (stmt.body instanceof VarDeclStmt) {
			reportError("Variable declaration cannot be the only statement in a while statement at " + stmt.body.posn);
		} else {
			stmt.body.visit(this, table);
		}

		return null;
	}

	@Override
	public Void visitUnaryExpr(UnaryExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitBinaryExpr(BinaryExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitRefExpr(RefExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitCallExpr(CallExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitLiteralExpr(LiteralExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitNewObjectExpr(NewObjectExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitNewArrayExpr(NewArrayExpr expr, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitQualifiedRef(QualifiedRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitIndexedRef(IndexedRef ref, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitIdentifier(Identifier id, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitOperator(Operator op, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral num, IdentificationTable table) {
		return null;
	}

	@Override
	public Void visitBooleanLiteral(BooleanLiteral bool, IdentificationTable table) {
		return null;
	}
}
