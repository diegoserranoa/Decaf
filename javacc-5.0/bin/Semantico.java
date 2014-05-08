import java.util.*;

public class Semantico implements ParserVisitor
{
	public HashMap<String, Map<String, Object>> simbolos;
    public HashMap<String, Method> methods;


    public int varTempCounter;
    public int lineNumber;
    public int labels;
   

	private class Map<Type, Value>{
        public Type type;
        public Value value;
        
        public Map(Type type, Value value){
            this.type = type;
            this.value = value;
        }
    }

	private class Method{
        public String type;
        public HashMap<String, String> parameters;
        
        public Method(){
            this.parameters = new HashMap<>();
        }        
        
        public String getParameterWithKey(String key){
            return this.parameters.get(key);
        }
    }

	public Semantico(){
		this.simbolos = new HashMap<>();
        this.methods = new HashMap<>();
        this.varTempCounter = 0;
        this.lineNumber = 0;
        this.labels = 0;
	}

	public void tablaSimbolos(){
    	System.out.println("Simbolos");
		// Variables globales
		Set<String> keys = this.simbolos.keySet();
		Iterator<String> e = keys.iterator();
    	while(e.hasNext()){
			Object nextElement = e.next();
    		System.out.print("< " + nextElement + " , ");
    		Map mapa = this.simbolos.get(nextElement);
    		System.out.print("< " + mapa.type + " , ");
    		if (mapa.value instanceof int[]){
    			System.out.print("int[" + ((int[])mapa.value).length + "]>");
    			// hacer for e imprimir valores del arreglo
    		} else if (mapa.value instanceof boolean[]){
    			System.out.print("boolean[" + ((boolean[])mapa.value).length + "]>");
    			// hacer for e imprimir valores del arreglo
    		} else {
    			System.out.print(mapa.value.toString() + ">");
    		}
    		System.out.print(">");
    		System.out.println("");
		}


		System.out.println("--------------------------------------");
		System.out.println("");
		System.out.println("");
		tablaMetodos();

	}

	public void tablaMetodos(){
    	System.out.println("Metodos");
		// Metodos
		Set<String> keys = this.methods.keySet();
		Iterator<String> e = keys.iterator();
    	while(e.hasNext()){
            String parameters = "";
			Object nextElement = e.next();
    		System.out.print(nextElement + " , ");
    		Method mapa = this.methods.get(nextElement);
    		System.out.print(mapa.type + " , ");

            Set<String> keysParameters = mapa.parameters.keySet();
			if( keysParameters.isEmpty() ){
                parameters = " - ";
            }else{
                Iterator<String> itParameters = keysParameters.iterator();
                while(itParameters.hasNext()){
                    String keyParameter = itParameters.next();
                    String typeParameter = mapa.parameters.get(keyParameter);
                    parameters += typeParameter + " " + keyParameter + ", ";
                }
            }
            System.out.println(parameters);
        
		}


		System.out.println("--------------------------------------");
		System.out.println("");
		System.out.println("");

	}

	public void printCode(String lineCode){
        String line = "";
        line += this.lineNumber++ + ":	" +lineCode;
        
        System.out.println(line);
    }

    public void debug(String s){
        System.out.println(s);
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
		String type = (String) node.jjtGetChild(0).jjtAccept(this, null);
        Map<String, Object> symbol;
        
        for( int i = 1; i<node.jjtGetNumChildren(); i++){
            symbol = (Map<String, Object>) node.jjtGetChild(i).jjtAccept(this, null);
            String id = symbol.type;
            
            symbol.type = type;
            if( symbol.value != null ){
                symbol.type += "[]";
                
                int size = (int) symbol.value;
                switch (type) {
                    case "INT":
                        {
                            int[] array = new int[size];
                            symbol.value = array;
                            break;
                        }
                    case "BOOL":
                        {
                            boolean[] array = new boolean[size];
                            symbol.value = array;
                            break;
                        }
                }
            }else{
                switch (type) {
                    case "INT":
                        {
                            symbol.value = "0";
                            break;
                        }
                    case "BOOL":
                        {
                            symbol.value = "false";
                            break;
                        }
                }
            }
            
            boolean keyDoesExist = this.simbolos.containsKey(id);
            if( !keyDoesExist ){
                this.simbolos.put(id, symbol);
            }else{
                throw new RuntimeException("ID  " + id + " ya existe.");
            }
        }
        
        return type;
	}
	public Object visit(ASTDECLARATION node, Object data){
		String idName = (String) node.jjtGetChild(0).jjtAccept(this, null);
        Map<String, Object> symbol = new Map<>(idName, null);
        
        if( node.jjtGetNumChildren() == 2 ){
            int size = (int) node.jjtGetChild(1).jjtAccept(this, null);
            symbol.value = size;
        }
                
        return symbol;
	}

	public Object visit(ASTID_SIZE node, Object data){
        return (int) node.jjtGetChild(0).jjtAccept(this, null);
	}
	public Object visit(ASTMETHOD_DECLARATION node, Object data){
		Method method = new Method();
        method.type = (String) node.jjtGetChild(0).jjtAccept(this, null);
        String idMethod = (String) node.jjtGetChild(1).jjtAccept(this, null);
        
        // Existen parámetros
        if( node.jjtGetNumChildren() > 3 ){
            int i = 2;
            while( i + 1 < node.jjtGetNumChildren() ){
                String typeParameter = (String) node.jjtGetChild(i).jjtAccept(this, null);
                String idParameter = (String) node.jjtGetChild(i+1).jjtAccept(this, null);
                
                boolean idParameterDoesExist = method.parameters.containsKey(idParameter);
                if( !idParameterDoesExist ){
                    method.parameters.put(idParameter, typeParameter);
                }else{
                    throw new RuntimeException("ID" + idParameter + " ya existe.");
                }
                
                i += 2;
            }
        }
        
        boolean idMethodDoesExist = methods.containsKey(idMethod);
        if( !idMethodDoesExist ){
            methods.put(idMethod, method);
        }else{
            throw new RuntimeException("Method ID " + idMethod + " ya existe.");
        }
        
        return defaultVisit(node, data);
	}
	public Object visit(ASTTYPE_VOID node, Object data){
		return "VOID";
	}
	public Object visit(ASTBLOCK node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTVARIABLE_DECLARATION node, Object data){
		String type = (String) node.jjtGetChild(0).jjtAccept(this, null);
        Map<String, Object> symbol;
        String id;
        
        for( int i = 1; i<node.jjtGetNumChildren(); i++){
            
            id = (String) node.jjtGetChild(i).jjtAccept(this, null);
            symbol = new Map<>(type, null);
            
            switch (type) {
                case "INT":
                    {
                        symbol.value = "0";
                        break;
                    }
                case "BOOL":
                    {
                        symbol.value = "false";
                        break;
                    }
            }
            
            boolean keyDoesExist = this.simbolos.containsKey(id);
            if( !keyDoesExist ){
                this.simbolos.put(id, symbol);
            }else{
                throw new RuntimeException("ID " + id + " ya existe.");
            }
        }
        
        return defaultVisit(node, data);
    }

    
	public Object visit(ASTTYPE_INT node, Object data){
		return "INT";
	}
	public Object visit(ASTTYPE_BOOLEAN node, Object data){
		return "BOOL";
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

    void addTempVar(SimpleNode node){
        String type = (String) node.jjtGetChild(0).jjtAccept(this, null);
        Map<String, Object> symbol;
        String id;
        
        for( int i = 1; i<node.jjtGetNumChildren(); i++){
            
            id = (String) node.jjtGetChild(i).jjtAccept(this, null);
            symbol = new Map<>(type, null);
            
            switch (type) {
                case "INT":
                    {
                        symbol.value = "0";
                        break;
                    }
                case "BOOL":
                    {
                        symbol.value = "false";
                        break;
                    }
            }
            
            boolean keyDoesExist = this.simbolos.containsKey(id);
            if( !keyDoesExist ){
                this.simbolos.put(id, symbol);
            }else{
                throw new RuntimeException("ID " + id + " ya existe.");
            }
        }
    }

	public Object visit(ASTASSIGN node, Object data){
		String leftChild = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();
        boolean idDoesExist = simbolos.containsKey(leftChild);
        if(!idDoesExist){
            throw new RuntimeException("ID " + leftChild + " no ha sido declarado.");
        }
        

        printCode(leftChild + " = " + rightChild);
        return defaultVisit(node, data);
	}
	public Object visit(ASTADD_ASSIGN node, Object data){
		String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        boolean idDoesExist = simbolos.containsKey(id);
        if(!idDoesExist){
            throw new RuntimeException("Id " + id + " no ha sido declarado.");
        }
        
        return defaultVisit(node, data);
	}
	public Object visit(ASTSUB_ASSIGN node, Object data){
		String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        boolean idDoesExist = simbolos.containsKey(id);
        if(!idDoesExist){
            throw new RuntimeException("Id " + id + " no ha sido declarado.");
        }
        
        return defaultVisit(node, data);
	}
	public Object visit(ASTMETHOD_CALL node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTCALLOUT node, Object data){
		return defaultVisit(node, data);
	}
	public Object visit(ASTLOCATION_OFFSET node, Object data){
		String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        int expresionResult = (int) node.jjtGetChild(1).jjtAccept(this, data);
        
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

    public boolean isInteger(String s) {
    try {
        Integer.parseInt(s);
        return true;
    }
    catch(NumberFormatException e) {
        return false;
    }
}
	public Object visit(ASTPLUS node, Object data){
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // ver si el valor es una variable o int
        if (isInteger(leftChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            if (!simbolos.containsKey(leftChild)){
                throw new RuntimeException("ID " + leftChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT

            Map id = this.simbolos.get(leftChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + leftChild + " no es de tipo INT.");
            }
        }

        if (isInteger(rightChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            
            if (!simbolos.containsKey(rightChild)){
                throw new RuntimeException("ID " + rightChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT
            Map id = this.simbolos.get(rightChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + rightChild + " no es de tipo INT.");
            }
        }
        
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " + " + rightChild);
        return "t"+varTempCounter;
	}
	public Object visit(ASTMINUS node, Object data){
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // ver si el valor es una variable o int
        if (isInteger(leftChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            if (!simbolos.containsKey(leftChild)){
                throw new RuntimeException("ID " + leftChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT

            Map id = this.simbolos.get(leftChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + leftChild + " no es de tipo INT.");
            }
        }

        if (isInteger(rightChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            
            if (!simbolos.containsKey(rightChild)){
                throw new RuntimeException("ID " + rightChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT
            Map id = this.simbolos.get(rightChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + rightChild + " no es de tipo INT.");
            }
        }


        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " - " + rightChild);
        return "t"+varTempCounter;
	}
	public Object visit(ASTTIMES node, Object data){
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // ver si el valor es una variable o int
        if (isInteger(leftChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            if (!simbolos.containsKey(leftChild)){
                throw new RuntimeException("ID " + leftChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT

            Map id = this.simbolos.get(leftChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + leftChild + " no es de tipo INT.");
            }
        }

        if (isInteger(rightChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            
            if (!simbolos.containsKey(rightChild)){
                throw new RuntimeException("ID " + rightChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT
            Map id = this.simbolos.get(rightChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + rightChild + " no es de tipo INT.");
            }
        }
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " * " + rightChild);
        return "t"+varTempCounter;
	}
	public Object visit(ASTOVER node, Object data){
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // ver si el valor es una variable o int
        if (isInteger(leftChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            if (!simbolos.containsKey(leftChild)){
                throw new RuntimeException("ID " + leftChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT

            Map id = this.simbolos.get(leftChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + leftChild + " no es de tipo INT.");
            }
        }

        if (isInteger(rightChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            
            if (!simbolos.containsKey(rightChild)){
                throw new RuntimeException("ID " + rightChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT
            Map id = this.simbolos.get(rightChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + rightChild + " no es de tipo INT.");
            }
        }
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " / " + rightChild);
        return "t"+varTempCounter;
	}
	public Object visit(ASTMODULE node, Object data){
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // ver si el valor es una variable o int
        if (isInteger(leftChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            if (!simbolos.containsKey(leftChild)){
                throw new RuntimeException("ID " + leftChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT

            Map id = this.simbolos.get(leftChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + leftChild + " no es de tipo INT.");
            }
        }

        if (isInteger(rightChild)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            
            if (!simbolos.containsKey(rightChild)){
                throw new RuntimeException("ID " + rightChild + " no declarado.");
            }
            //  checar su tipo, debe de ser INT
            Map id = this.simbolos.get(rightChild);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + rightChild + " no es de tipo INT.");
            }
        }
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " MOD " + rightChild);
        return "t"+varTempCounter;
	}
	public Object visit(ASTLOGICAL_NOT node, Object data){
		String child    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = !" + child);
        return "t"+varTempCounter;
	}
	public Object visit(ASTUNARY_MINUS node, Object data){
		String child = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();

        // ver si el valor es una variable o int
        if (isInteger(child)){
            // es entero
        } else {
            // es una variable
            // checar si ya fue declarada
            if (!simbolos.containsKey(child)){
                throw new RuntimeException("ID " + child + " no declarado.");
            }
            //  checar su tipo, debe de ser INT

            Map id = this.simbolos.get(child);
            if (!"INT".equals(id.type)) {
                throw new RuntimeException("ID " + child + " no es de tipo INT.");
            }
        }
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = -" + child);
        return "t"+varTempCounter;
	}
	public Object visit(ASTID node, Object data){
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