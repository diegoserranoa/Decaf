/* Generated By:JJTree: Do not edit this line. ASTMETHOD_CALL.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTMETHOD_CALL extends SimpleNode {
  public ASTMETHOD_CALL(int id) {
    super(id);
  }

  public ASTMETHOD_CALL(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=e9f8259e6a5c139b2c061bae6831a9a6 (do not edit this line) */
