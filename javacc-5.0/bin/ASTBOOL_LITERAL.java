/* Generated By:JJTree: Do not edit this line. ASTBOOL_LITERAL.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTBOOL_LITERAL extends SimpleNode {
  public ASTBOOL_LITERAL(int id) {
    super(id);
  }

  public ASTBOOL_LITERAL(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=723dabac1d5b45b4d9ce83346c3fb55d (do not edit this line) */
