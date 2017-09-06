package it.unibz.inf.kaos.data.query;

/**
 * 
 * AnnotationQueryVisitor
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public interface AnnotationQueryVisitor{

	public void visit(BinaryAnnotationQuery baq);

	public void visit(UnaryAnnotationQuery uaq);

}
