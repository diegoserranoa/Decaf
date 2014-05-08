import java.util.*;

public class Semantico implements ParserVisitor
{
	public HashMap<String, Map<String, Object>> simbolos;
    public HashMap<String, Map<String, Object>> tempSimbolos;
    public ArrayList<SymbolNode> tree;
    public HashMap<String, Method> methods;


    public int varTempCounter;
    public int lineNumber;
    public int labels;   

	private class Method{
        public String type;
        public HashMap<String, String> parameters;
        public HashMap<String, String> parametersIndex;
        
        public Method(){
            this.parameters = new HashMap<>();
            this.parametersIndex = new HashMap<>();
        }        
        
        public String getParameterWithKey(String key){
            return this.parameters.get(key);
        }
    }

	public Semantico(){
		this.simbolos = new HashMap<>();
        this.methods = new HashMap<>();
        this.tempSimbolos = new HashMap<>();
        this.tree = new ArrayList<SymbolNode>();
        SymbolNode root = new SymbolNode(null);
        this.tree.add(root);
        this.varTempCounter = 0;
        this.lineNumber = 0;
        this.labels = 0;
	}

	public void tablaSimbolos(){
    	System.out.println("Simbolos");
		// Variables globales
        for(SymbolNode i : tree) {
            Set<String> keys = i.simbolos.keySet();
            Iterator<String> e = keys.iterator();
            while(e.hasNext()){
                Object nextElement = e.next();
                System.out.print("< " + nextElement + " , ");
                Map mapa = i.simbolos.get(nextElement);
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
        }
        
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
        node.childrenAccept(this, data);
        boolean hasMain = this.methods.containsKey("main");
        /*
        Set<String> keys = this.methods.keySet();
        Iterator<String> e = keys.iterator();
        while(e.hasNext()){
            Object nextElement = e.next();
            if (nextElement.equals("main")){
                hasMain = true;
                break;
            }
        }
        */
        if (!hasMain){
            throw new RuntimeException ("El programa no tiene medoto main");
        }
		return true;
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
            
            boolean keyDoesExist = this.tree.get(0).simbolos.containsKey(id);
            if( !keyDoesExist ){
                this.tree.get(0).simbolos.put(id, symbol);
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
        if ((int) node.jjtGetChild(0).jjtAccept(this, null) <= 0){
            throw new RuntimeException("No se puede declarar arreglos con tamaño menor o igual a 0");
        }
        return (int) node.jjtGetChild(0).jjtAccept(this, null);
	}
	public Object visit(ASTMETHOD_DECLARATION node, Object data){

        boolean mainMethodExists = methods.containsKey("main");
        if (!mainMethodExists){
            // Nuevo nodo en el arbol de los simbolos
            // El padre es el root
            SymbolNode newNode = new SymbolNode(this.tree.get(0));
            tree.add(newNode);
            Method method = new Method();
            method.type = (String) node.jjtGetChild(0).jjtAccept(this, null);
            String idMethod = (String) node.jjtGetChild(1).jjtAccept(this, null);
        
            // Existen parámetros
            if( node.jjtGetNumChildren() > 3 ){
                int i = 2;
                int index = 0;
                while( i + 1 < node.jjtGetNumChildren() ){
                    String typeParameter = (String) node.jjtGetChild(i).jjtAccept(this, null);
                    String idParameter = (String) node.jjtGetChild(i+1).jjtAccept(this, null);
                    
                    boolean idParameterDoesExist = method.parameters.containsKey(idParameter);
                    if( !idParameterDoesExist ){
                        method.parameters.put(idParameter, typeParameter);
                        method.parametersIndex.put(idParameter, index + "");
                    }else{
                        throw new RuntimeException("ID" + idParameter + " ya existe.");
                    }
                    index++;
                    i += 2;
                }
            }
            
            boolean idMethodDoesExist = methods.containsKey(idMethod);
            if( !idMethodDoesExist ){
                methods.put(idMethod, method);
            }else{
                throw new RuntimeException("Method ID " + idMethod + " ya existe.");
            }

            ArrayList<Object> datos = new ArrayList<Object>();
            Map <String, String> mapa = new Map<>("METHOD", method.type);
            datos.add(mapa);
            datos.add(newNode);

            node.jjtGetChild(node.jjtGetNumChildren() - 1).jjtAccept(this, datos);
        }
        return 0;
	}
	public Object visit(ASTTYPE_VOID node, Object data){
		return "VOID";
	}
	public Object visit(ASTBLOCK node, Object data){
        if (data instanceof ArrayList){
            ArrayList <Object> array = (ArrayList)data;
            
            if (array.get(0) instanceof Map) {
                Map <String, Object> mapita = (Map)array.get(0);
                if (mapita.type == "METHOD"){
                    if (node.jjtGetNumChildren() > 0){
                        int i = 0;
                        while (i < node.jjtGetNumChildren()){
                            //if(node.jjtGetChild(i).toString().equals("VARIABLE_DECLARATION")){
                            SymbolNode nodo = (SymbolNode) ((ArrayList)data).get(1);
                            node.jjtGetChild(i).jjtAccept(this, nodo);
                            i++;
                        }
                    }
                    /*
                    if (mapita.value == "INT"){
                        boolean hasReturn = false;
                        if( node.jjtGetNumChildren() > 0 ){
                            int i = 0;
                            while( i < node.jjtGetNumChildren() ){
                                String child = (String) node.jjtGetChild(i).jjtAccept(this, "INT");
                                if (child.equals("RETURN")){
                                    hasReturn = true;
                                    break;
                                }
                                i++;
                            }
                            if (!hasReturn){
                                throw new RuntimeException ("El metodo debe tener un return.");
                            }
                        } else {
                            throw new RuntimeException ("El metodo debe tener un return.");
                        }
                    }
                    */
                }
            } else if (array.get(0) instanceof String){
                if (array.get(0).equals("FOR")){
                    int i = 0;
                    while (i < node.jjtGetNumChildren()){
                        SymbolNode nodo = (SymbolNode) array.get(1);   
                        node.jjtGetChild(i).jjtAccept(this, nodo);
                        i++;
                    }
                } else if (array.get(0).equals("IF")){
                    int i = 0;
                    while (i < node.jjtGetNumChildren()){
                        SymbolNode nodo = (SymbolNode) array.get(1);   
                        node.jjtGetChild(i).jjtAccept(this, nodo);
                        i++;
                    }
                }
            }
        } 
		return 0;
	}

	public Object visit(ASTVARIABLE_DECLARATION node, Object data){
        SymbolNode nodo = (SymbolNode)data;
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
            
            boolean keyDoesExist = nodo.simbolos.containsKey(id);
            if(!keyDoesExist){
                nodo.simbolos.put(id, symbol);
            }else{
                throw new RuntimeException("ID " + id + " ya existe.");
            }
        }
        
        return 0;
    }

    
	public Object visit(ASTTYPE_INT node, Object data){
		return "INT";
	}
	public Object visit(ASTTYPE_BOOLEAN node, Object data){
		return "BOOL";
	}
	public Object visit(ASTIF node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        SymbolNode newNode = new SymbolNode(nodo);
        tree.add(newNode);

        System.out.println(node.jjtGetChild(0));
        // expr
        String expr = (String) node.jjtGetChild(0).jjtAccept(this, nodo).toString();
        // block
        String block = (String) node.jjtGetChild(1).jjtAccept(this, nodo).toString();

        typeCheck(nodo, expr, "BOOLEAN");

        ArrayList<Object> datos = new ArrayList<Object>();
        String mapa = "IF";
        datos.add(mapa);
        datos.add(newNode);

        node.jjtGetChild(node.jjtGetNumChildren() - 1).jjtAccept(this, datos);
		return 0;
	}
	public Object visit(ASTFOR node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        SymbolNode newNode = new SymbolNode(nodo);
        tree.add(newNode);
        // id
        String id = (String) node.jjtGetChild(0).jjtAccept(this, null).toString();
        // expr
        String initialExpr = (String) node.jjtGetChild(1).jjtAccept(this, null).toString();
        // expr
        String endingExpr = (String) node.jjtGetChild(2).jjtAccept(this, null).toString();

        typeCheck(nodo, id, "INT");
        typeCheck(nodo, initialExpr, "INT");
        typeCheck(nodo, endingExpr, "INT");

        ArrayList<Object> datos = new ArrayList<Object>();
        String mapa = "FOR";
        datos.add(mapa);
        datos.add(newNode);

        node.jjtGetChild(node.jjtGetNumChildren() - 1).jjtAccept(this, datos);

		return 0;
	}
	public Object visit(ASTRETURN node, Object data){
        if (data.equals("VOID")){
            if (node.jjtGetNumChildren() != 0){
                throw new RuntimeException("Metodo de tipo VOID. Return no debe regresar un valor.");
            }
        } else if (node.jjtGetNumChildren() > 0){
            String child = (String)node.jjtGetChild(0).jjtAccept(this, null).toString();
            if (data.equals("INT")){
                //typeCheck(data, child, "INT");
            } else if (data.equals("BOOL")){
                //typeCheck(data, child, "BOOL");
            }
        } else {
            throw new RuntimeException("Return debe regresar un valor.");
        }
		return "RETURN";
	}
	public Object visit(ASTBREAK node, Object data){
        if (!data.equals("FOR")){
            throw new RuntimeException("BREAK solo puede estar dentro de un ciclo FOR.");
        }
		return null;
	}
	public Object visit(ASTCONTINUE node, Object data){
        if (!data.equals("FOR")){
            throw new RuntimeException("CONTINUE solo puede estar dentro de un ciclo FOR.");
        }
		return null;
	}

    void addTempVar(String id, String type){
        Map<String, Object> symbol;
        
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
        
        this.tempSimbolos.put(id, symbol);
    }

    SymbolNode idExist(SymbolNode n, String key){
        boolean idDoesExist = n.simbolos.containsKey(key);
        if(idDoesExist){
            return n;
        } else if (n.parent != null){
             return idExist(n.parent, key);
        }
        return null;
    }

	public Object visit(ASTASSIGN node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        /*
        Map<String, Object> symbol = (Map<String, Object>) node.jjtGetChild(i).jjtAccept(this, null);
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
        
        System.out.println("leftChild: " + leftChild);
        //System.out.println("rightChild: " + rightChild);

        */
        SymbolNode n = idExist(nodo, leftChild);
        if (n == null){
            throw new RuntimeException("ID " + leftChild + " no ha sido declarado.");
        }
        String type = n.simbolos.get(leftChild).type;
        n = idExist(nodo, rightChild);
        if (!tempSimbolos.containsKey(rightChild)){
            typeCheck(n, rightChild, type);
        }

        printCode(leftChild + " = " + rightChild);
        return 0;
	}
	public Object visit(ASTADD_ASSIGN node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        boolean idDoesExist = simbolos.containsKey(id);
        if(!idDoesExist){
            throw new RuntimeException("Id " + id + " no ha sido declarado.");
        }

        typeCheck(nodo, id, "INT");

        
        return defaultVisit(node, data);
	}
	public Object visit(ASTSUB_ASSIGN node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        boolean idDoesExist = simbolos.containsKey(id);
        if(!idDoesExist){
            throw new RuntimeException("Id " + id + " no ha sido declarado.");
        }

        typeCheck(nodo, id, "INT");
        
        return defaultVisit(node, data);
	}
	public Object visit(ASTMETHOD_CALL node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild = (String) node.jjtGetChild(0).jjtAccept(this, data);        
        boolean methodExists = methods.containsKey(leftChild);
        
        // TODO: se puede llamar al metodo main? 
        if (methodExists){
            Method mapa = this.methods.get(leftChild);
            int parameters = 0;
            Set<String> indexParameters = mapa.parametersIndex.keySet();
            String[] typeParameters = new String[indexParameters.size()];

            if(!indexParameters.isEmpty()){
                Iterator<String> itParameters = indexParameters.iterator();
                while(itParameters.hasNext()){
                    String keyParameter = itParameters.next();
                    int indexParameter = Integer.parseInt(mapa.parametersIndex.get(keyParameter));
                    String typeParameter = mapa.parameters.get(keyParameter);
                    typeParameters[indexParameter] = typeParameter;
                    parameters++;
                }
            }

            // Existen parámetros
            if(node.jjtGetNumChildren() - 1  == parameters){
                int i = 1;
                while(i < node.jjtGetNumChildren()){
                    String idParameter = node.jjtGetChild(i).jjtAccept(this, null) + "";

                    // type check
                    typeCheck(nodo, idParameter, typeParameters[i - 1]);

                    i++;
                }
            } else {
                throw new RuntimeException("El metodo " + leftChild + " recibe " + parameters + " parámetro(s).");
            }
        } else {
            throw new RuntimeException("El metodo " + leftChild + " no existe.");
        }

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
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // Both have to be BOOLEAN
        typeCheck(nodo, leftChild, "BOOLEAN");
        typeCheck(nodo, rightChild, "BOOLEAN");

		return defaultVisit(node, data);
	}

	public Object visit(ASTAND node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // Both have to be BOOLEAN
        typeCheck(nodo, leftChild, "BOOLEAN");
        typeCheck(nodo, rightChild, "BOOLEAN");

		return defaultVisit(node, data);
	}

	public Object visit(ASTEQUAL node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        /* type check
        if (leftChild.type.equals("INT")){
            typeCheck(leftChild, "INT");
            typeCheck(rightChild, "INT");
        } else {
            typeCheck(leftChild, "BOOLEAN");
            typeCheck(rightChild, "BOOLEAN");            
        }
        */
		return defaultVisit(node, data);
	}
	public Object visit(ASTNOT_EQUAL node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        /* type check
        if (leftChild.type.equals("INT")){
            typeCheck(leftChild, "INT");
            typeCheck(rightChild, "INT");
        } else {
            typeCheck(leftChild, "BOOLEAN");
            typeCheck(rightChild, "BOOLEAN");            
        }
        */

		return defaultVisit(node, data);
	}
	public Object visit(ASTGREATER node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // Both have to be INT
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");

		return defaultVisit(node, data);
	}
	public Object visit(ASTGREATER_OR_EQUAL node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // Both have to be INT
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");

		return defaultVisit(node, data);
	}
	public Object visit(ASTLESSER node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // Both have to be INT
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");

		return defaultVisit(node, data);
	}
	public Object visit(ASTLESSER_OR_EQUAL node, Object data){
        SymbolNode nodo = (SymbolNode) data;
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // Both have to be INT
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");

		return defaultVisit(node, data);
	}

    boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    void typeCheck(SymbolNode nodo, String s, String type){
        if (type.equals("INT")){
            // ver si el valor es una variable o int
            if (isInteger(s)){
                // es entero
            } else if (s.equals("false") || s.equals("true")) {
                    throw new RuntimeException("Tipo " + s + " encontrado. Se esperaba un tipo INT.");
            } else if (!tempSimbolos.containsKey(s)){
                // es una variable
                // checar si ya fue declarada
                SymbolNode n = idExist(nodo, s);
                if (n == null){
                    throw new RuntimeException("ID " + s + " no declarado.");
                }

                // checar su tipo, debe de ser INT
                Map id = n.simbolos.get(s);
                if (!"INT".equals(id.type)) {
                    throw new RuntimeException("ID " + s + " no es de tipo INT.");
                }

            }
        } else if (type.equals("BOOL")) {
            // ver si el valor de la variable es boolean
            if (isInteger(s)){
                // es entero
                    throw new RuntimeException("Valor " + s + " es un entero. Se esperaba un tipo BOOLEAN.");
            } else if (!tempSimbolos.containsKey(s) && !"true".equals(s) && !"false".equals(s)){
                // es una variable
                SymbolNode n = idExist(nodo, s);
                // checar si ya fue declarada
                if (n == null){
                    throw new RuntimeException("ID " + s + " no declarado.");
                }

                // checar su tipo, debe de ser BOOL
                Map id = n.simbolos.get(s);
                if (!"BOOL".equals(id.type)) {
                    throw new RuntimeException("ID " + s + " no es de tipo BOOLEAN.");
                }

                
            }
        } else if (type.equals("INT[]")){
            // arreglo de enteros
        }
        
    }

	public Object visit(ASTPLUS node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // typeCheck
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");

        varTempCounter++;
        String varTemp = "t"+varTempCounter;
        addTempVar(varTemp, "INT");
        printCode(varTemp + " = " + leftChild.toString() + " + " + rightChild);
        return varTemp;
	}
	public Object visit(ASTMINUS node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // typeCheck
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");
        
        varTempCounter++;
        String varTemp = "t"+varTempCounter;
        addTempVar(varTemp, "INT");
        printCode(varTemp + " = " + leftChild.toString() + " - " + rightChild);
        return varTemp;
	}
	public Object visit(ASTTIMES node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // typeCheck
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");
        
        varTempCounter++;
        String varTemp = "t"+varTempCounter;
        addTempVar(varTemp, "INT");
        printCode(varTemp + " = " + leftChild.toString() + " * " + rightChild);
        return varTemp;
	}
	public Object visit(ASTOVER node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // typeCheck
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");
        
        varTempCounter++;
        String varTemp = "t"+varTempCounter;
        addTempVar(varTemp, "INT");
        printCode(varTemp + " = " + leftChild.toString() + " / " + rightChild);
        return varTemp;
	}
	public Object visit(ASTMODULE node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();

        // typeCheck
        typeCheck(nodo, leftChild, "INT");
        typeCheck(nodo, rightChild, "INT");
        
        varTempCounter++;
        String varTemp = "t"+varTempCounter;
        addTempVar(varTemp, "INT");
        printCode(varTemp + " = " + leftChild.toString() + " MOD " + rightChild);
        return varTemp;
	}
	public Object visit(ASTLOGICAL_NOT node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String child    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        
        typeCheck(nodo, child, "BOOL");

        varTempCounter++;
        printCode("t"+varTempCounter + " = !" + child);
        return "t"+varTempCounter;
	}
	public Object visit(ASTUNARY_MINUS node, Object data){
        SymbolNode nodo = (SymbolNode) data;
		String child = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();

        // typeCheck
        typeCheck(nodo, child, "INT");
        
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