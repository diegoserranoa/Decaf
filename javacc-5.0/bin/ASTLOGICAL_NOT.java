/* Generated By:JJTree: Do not edit this line. ASTLOGICAL_NOT.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTLOGICAL_NOT extends SimpleNode {
  public ASTLOGICAL_NOT(int id) {
    super(id);
  }

  public ASTLOGICAL_NOT(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=ffb6f9f40535c616936a5bcf10fbbc0a (do not edit this line) */