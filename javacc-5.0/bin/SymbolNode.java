import java.util.*;

public class SymbolNode {
	public SymbolNode parent;
	public HashMap<String, Map<String, Object>> simbolos;

	public SymbolNode(SymbolNode p){
		this.parent = p;
		this.simbolos = new HashMap<>();
	}

	public SymbolNode getParent(){
		return this.parent;
	}

	public void setParent(SymbolNode p){
		this.parent = p;
	}

}