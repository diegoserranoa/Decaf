import java.util.*;

public class Semantico implements ParserVisitor
{
	private class Map {
		public String id;
		public Object value;

		public Map(String id, Object value){
			this.id = id;
			this.value = value;
		}
	}

	public Hashtable<String, Map> simbolos;

	public Semantico(){
		simbolos = new Hashtable<String, Map>();
	}

	public void tablaSimbolos(){
		System.out.println("Simbolos");
		// Variables globales
		Enumeration<String> e = this.simbolos.keys();
    	while(e.hasMoreElements()){
    		Object nextElement = e.nextElement();
    		System.out.print("< " + nextElement + " , ");
    		Map mapa = this.simbolos.get(nextElement);
    		System.out.print("< " + mapa.id + " , ");
    		if (mapa.value instanceof int[]){
    			System.out.print(mapa.value.toString() + "[" + ((int[])mapa.value).length + "]>");
    			// hacer for e imprimir valores del arreglo
    		} else {
    			System.out.print(mapa.value.toString() + ">");
    		}
    		System.out.print(">");
    		System.out.println("");
		}

	}

	public Object defaultVisit(SimpleNode node, Object data){
    	node.childrenAccept(this, data);
    	return data;
  	}

	public Object visit(SimpleNode node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTPROGRAM node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTGLOBAL_DECLARATION node, Object data){
		// Tipo de dato (int o boolean)
			String type = node.jjtGetChild(0).toString();

		for (int i = 1; i < node.jjtGetNumChildren(); i++){
			Map valor = new Map(null, null);

			valor = (Map)node.jjtGetChild(i).jjtAccept(this, null);

			// id de la variable
			String id = valor.id;
			// Objeto tipo Map
			Object objeto = valor.value;

			// Checar si ya existe variable en el Hash de simbolos
			if (this.simbolos.containsKey(id)){
				throw new RuntimeException("Identificador " + id + " ya existe.");
			} else {
				if (objeto instanceof int[]){
					valor.id = "TYPE_INT_ARRAY";
				} else {
					valor.id = type;
				}
				valor.value = objeto;
				this.simbolos.put(id, valor);
			}
		}

		return defaultVisit(node, data);
	}
	public Object visit(ASTDECLARATION node, Object data){
		Map valor = new Map(null, null);
		Object objeto = new Object();

		String id = node.jjtGetChild(0).jjtAccept(this, null).toString();
		if (node.jjtGetNumChildren() > 1){
			int size = Integer.parseInt(node.jjtGetChild(1).jjtAccept(this, null).toString());
			int[] arreglo = new int[size];
			objeto = arreglo;
		}
		System.out.println(id);
		valor.id = id;
		valor.value = objeto;
		return valor;
	}
	public Object visit(ASTID_SIZE node, Object data){
		return node.jjtGetChild(0).jjtAccept(this, null);
	}
	public Object visit(ASTMETHOD_DECLARATION node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTTYPE_VOID node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTBLOCK node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTVARIABLE_DECLARATION node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTTYPE_INT node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTTYPE_BOOLEAN node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTIF node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTFOR node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTRETURN node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTBREAK node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTCONTINUE node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTASSIGN node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTADD_ASSIGN node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTSUB_ASSIGN node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTMETHOD_CALL node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTCALLOUT node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTLOCATION_OFFSET node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTOR node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTAND node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTEQUAL node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTNOT_EQUAL node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTGREATER node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTGREATER_OR_EQUAL node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTLESSER node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTLESSER_OR_EQUAL node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTPLUS node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTMINUS node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTTIMES node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTOVER node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTMODULE node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTLOGICAL_NOT node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTUNARY_MINUS node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTIDENTIFYER node, Object data){
		return node.value.toString();
	}
	public Object visit(ASTDECIMAL_LITERAL node, Object data){
		int value = Integer.parseInt(node.value.toString());
		return value;
	}
	public Object visit(ASTHEX_LITERAL node, Object data){
		return node.value.toString();
	}
	public Object visit(ASTBOOL_LITERAL node, Object data){
		return node.value.toString();
	}
	public Object visit(ASTCHARACTER_LITERAL node, Object data){
		return node.value.toString();
	}
	public Object visit(ASTSTRING_LITERAL node, Object data){
		return node.value.toString();
	}
}