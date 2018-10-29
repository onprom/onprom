package it.unibz.inf.kaos.data.query;

/**
 * 
 * AnnotationQueryVisitor
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public interface AnnotationQueryVisitor{

    void visit(BinaryAnnotationQuery baq);

    void visit(UnaryAnnotationQuery uaq);

}
