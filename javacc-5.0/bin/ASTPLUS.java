/* Generated By:JJTree: Do not edit this line. ASTPLUS.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTPLUS extends SimpleNode {
  public ASTPLUS(int id) {
    super(id);
  }

  public ASTPLUS(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=0b13c8e34582c7adfd326f785a767ef9 (do not edit this line) */