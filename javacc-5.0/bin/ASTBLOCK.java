/* Generated By:JJTree: Do not edit this line. ASTBLOCK.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTBLOCK extends SimpleNode {
  public ASTBLOCK(int id) {
    super(id);
  }

  public ASTBLOCK(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5f07865200b5cce3b06f60cd857a999f (do not edit this line) */