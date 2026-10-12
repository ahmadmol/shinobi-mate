package com.group_finity.mascot.script;
import java.util.Set;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;
/** Pack configuration is a bounded expression language, not a script/program loader. */
public final class PackExpression {
 private static final Set<String> MATH=Set.of("abs","min","max","pow","random","floor","ceil","round","sqrt","sin","cos","tan","atan2","log","exp");
 private PackExpression(){ }
 public static void validate(String source) {
  if(source.length()>4096)throw new IllegalArgumentException("Expression too long");
  AstRoot root=new Parser().parse(source,"<pack-expression>",1);
  if(root.getStatements().size()!=1||!(root.getStatements().getFirst() instanceof ExpressionStatement))throw new IllegalArgumentException("Only one expression is allowed");
  root.visit(node->{
   if(node instanceof AstRoot||node instanceof ExpressionStatement||node instanceof Name||node instanceof NumberLiteral||node instanceof StringLiteral||node instanceof ParenthesizedExpression||node instanceof PropertyGet||node instanceof ConditionalExpression)return true;
   if(node instanceof KeywordLiteral&&(node.getType()==Token.TRUE||node.getType()==Token.FALSE||node.getType()==Token.NULL))return true;
   if(node.getClass()==InfixExpression.class)return true;
   if(node instanceof UnaryExpression&&(node.getType()==Token.NOT||node.getType()==Token.BITNOT||node.getType()==Token.POS||node.getType()==Token.NEG||node.getType()==Token.TYPEOF))return true;
   if(node instanceof FunctionCall call&&call.getTarget() instanceof PropertyGet property) {
    String method=property.getProperty().getIdentifier();
    if(method.equals("isOn")||property.getTarget() instanceof Name name&&name.getIdentifier().equals("Math")&&MATH.contains(method))return true;
   }
   throw new IllegalArgumentException("Only data expressions, Math and border predicates are allowed");
  });
 }
}
