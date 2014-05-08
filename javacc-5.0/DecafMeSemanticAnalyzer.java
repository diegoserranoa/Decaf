
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Pablo
 */
class DecafMeSemanticAnalyzer implements DecafMeParserVisitor {
    HashMap<String, SymbolTuple<String, Object>> symbolTable;
    HashMap<String, Method> methods;
    
    public int varTempCounter;
    public int lineNumber;
    public int labels;
    
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
    
    public DecafMeSemanticAnalyzer(){
        this.symbolTable = new HashMap<>();
        this.methods = new HashMap<>();
        this.varTempCounter = 0;
        this.lineNumber = 0;
        this.labels = 0;
    }
    
    public void printCode(String lineCode){
        String line = "";
        line += String.format("%5d:\t", this.lineNumber++);
        line += lineCode;
        
        System.out.println(line);
    }
    
    private class SymbolTuple<Type, Value>{
        public Type type;
        public Value value;
        
        public SymbolTuple(Type type, Value value){
            this.type = type;
            this.value = value;
        }
    }
    
    public void printSymbolTable()
    {
        System.out.println(" Symbol Table:");

        Set<String> keys = this.symbolTable.keySet();
        if(keys.isEmpty())
        {
                System.out.println("There are no entries on this table.");
        }
        else
        {
                System.out.println(String.format("%-10s\t%-10s\t%-10s", "Identifier", "Type", "Value"));
                System.out.println("----------------------------------------------------------------------");
        }
        
        Iterator<String> it = keys.iterator();
        while(it.hasNext())
        {
                String key = it.next();
                SymbolTuple<String, Object> record = this.symbolTable.get(key);
                String value = "";
                if( record.value != null ){
                    if( record.value instanceof String ){
                        value = record.value.toString();
                    }else if( record.value instanceof int[] ){
                        int[] array = (int[]) record.value;
                        for (int element : array) {
                            value += element + "  ";
                        }
                    }else if( record.value instanceof boolean[] ){
                        boolean[] array = (boolean[]) record.value;
                        for (boolean element : array) {
                            value += element + "  ";
                        }
                    }
                }
                
                System.out.println(String.format("%-10s\t%-10s\t%-10s", key, record.type.toString(), value ));
        }
        System.out.println("\n");
    }
    
    
    public void printMethodTable()
    {
        System.out.println(" Method Table:");

        Set<String> keys = this.methods.keySet();
        if(keys.isEmpty())
        {
                System.out.println("There are no entries on this table.");
        }
        else
        {
                System.out.println(String.format("%-10s\t%-10s\t%-10s", "Identifier", "Type", "Parameters"));
                System.out.println("----------------------------------------------------------------------");
        }
        
        Iterator<String> it = keys.iterator();
        while(it.hasNext())
        {
            String parameters = "";
            String key = it.next();
            Method record = this.methods.get(key);
            Set<String> keysParameters = record.parameters.keySet();

            if( keysParameters.isEmpty() ){
                parameters = "None";
            }else{
                Iterator<String> itParameters = keysParameters.iterator();
                while(itParameters.hasNext()){
                    String keyParameter = itParameters.next();
                    String typeParameter = record.parameters.get(keyParameter);
                    parameters += typeParameter + " " + keyParameter + ", ";
                }
            }
            System.out.println(String.format("%-10s\t%-10s\t%-10s", key, record.type, parameters));
        }
        System.out.println("\n");
    }
    
    
   
    
    public Object defaultVisit(SimpleNode node, Object data){
        node.childrenAccept(this, data);
        return data;
    }
    
    @Override
    public Object visit(SimpleNode node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTPROGRAM node, Object data) {
        node.childrenAccept(this, null);
        return null;
    }

    @Override
    public Object visit(ASTFIELD_DECLARATION node, Object data) {
        String type = (String) node.jjtGetChild(0).jjtAccept(this, null);
        SymbolTuple<String, Object> symbol;
        
        for( int i = 1; i<node.jjtGetNumChildren(); i++){
            symbol = (SymbolTuple<String, Object>) node.jjtGetChild(i).jjtAccept(this, null);
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
            
            boolean keyDoesExist = this.symbolTable.containsKey(id);
            if( !keyDoesExist ){
                this.symbolTable.put(id, symbol);
            }else{
                throw new RuntimeException("Identifyer " + id + " already does exist.");
            }
        }
        
        return type;
    }

    @Override
    public Object visit(ASTDECLARATION node, Object data) {
        String idName = (String) node.jjtGetChild(0).jjtAccept(this, null);
        SymbolTuple<String, Object> symbol = new SymbolTuple<>(idName, null);
        
        if( node.jjtGetNumChildren() == 2 ){
            int size = (int) node.jjtGetChild(1).jjtAccept(this, null);
            symbol.value = size;
        }
                
        return symbol;       
    }

    @Override
    public Object visit(ASTID_SIZE node, Object data) {
        return (int) node.jjtGetChild(0).jjtAccept(this, null);
    }

    @Override
    public Object visit(ASTMETHOD_DECLARATION node, Object data) {
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
                    throw new RuntimeException("Parameter Identifier " + idParameter + " already does exist.");
                }
                
                i += 2;
            }
        }
        
        boolean idMethodDoesExist = methods.containsKey(idMethod);
        if( !idMethodDoesExist ){
            methods.put(idMethod, method);
        }else{
            throw new RuntimeException("Method Identifier " + idMethod + " already does exist.");
        }
        
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTTYPE_VOID node, Object data) {
        return "VOID";
    }

    @Override
    public Object visit(ASTBLOCK node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTVARIABLE_DECLARATION node, Object data) {
        String type = (String) node.jjtGetChild(0).jjtAccept(this, null);
        SymbolTuple<String, Object> symbol;
        String id;
        
        for( int i = 1; i<node.jjtGetNumChildren(); i++){
            
            id = (String) node.jjtGetChild(i).jjtAccept(this, null);
            symbol = new SymbolTuple<>(type, null);
            
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
            
            boolean keyDoesExist = this.symbolTable.containsKey(id);
            if( !keyDoesExist ){
                this.symbolTable.put(id, symbol);
            }else{
                throw new RuntimeException("Identifyer " + id + " already does exist.");
            }
        }
        
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTTYPE_INT node, Object data) {
        return "INT";
    }

    @Override
    public Object visit(ASTTYPE_BOOLEAN node, Object data) {
        return "BOOL";
    }

    @Override
    public Object visit(ASTIF_STATEMENT node, Object data) {
        String lineCode = label++ + ":\t ";
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTFOR_STATEMENT node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTRETURN_STATEMENT node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTBREAK_STATEMENT node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTCONTINUE_STATEMENT node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTASSIGN node, Object data) {
        String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        boolean idDoesExist = symbolTable.containsKey(id);
        if(!idDoesExist){
            throw new RuntimeException("Identifier " + id + " has not been declared.");
        }
        
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTADD_ASSIGN node, Object data) {
        String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        boolean idDoesExist = symbolTable.containsKey(id);
        if(!idDoesExist){
            throw new RuntimeException("Identifier " + id + " has not been declared.");
        }
        
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTSUB_ASSIGN node, Object data) {
        String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        boolean idDoesExist = symbolTable.containsKey(id);
        if(!idDoesExist){
            throw new RuntimeException("Identifier " + id + " has not been declared.");
        }
        
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTMETHOD_CALL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTCALLOUT_CALL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTLOCATION_OFFSET node, Object data) {
        String id = (String) node.jjtGetChild(0).jjtAccept(this, data);
        int expresionResult = (int) node.jjtGetChild(1).jjtAccept(this, data);
        
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTOR node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTAND node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTEQUAL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTNOT_EQUAL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTGREATER node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTGREATER_OR_EQUAL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTLESSER node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTLESSER_OR_EQUAL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTPLUS node, Object data) {
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();
        
        /*if (!symbolTable.containsKey(leftChild)){
            System.out.println("Identifier " + leftChild + " has not been declared.");
        } else if (!symbolTable.containsKey(rightChild)){
            System.out.println("Identifier " + rightChild + " has not been declared.");
        } else {
            if (!"INT".equals(symbolTable.get(leftChild).type)) {
                System.out.println("Identifier " + leftChild + " is not of type INT.");
            } else if (!"INT".equals(symbolTable.get(rightChild).type)) {
                System.out.println("Identifier " + rightChild + " is not of type INT.");
            } else {
                
            }
        }
                */
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " + " + rightChild);
        return "t"+varTempCounter;
    }

    @Override
    public Object visit(ASTMINUS node, Object data) {
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " - " + rightChild);
        return "t"+varTempCounter;
    }

    @Override
    public Object visit(ASTTIMES node, Object data) {
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " * " + rightChild);
        return "t"+varTempCounter;
    }

    @Override
    public Object visit(ASTOVER node, Object data) {
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " / " + rightChild);
        return "t"+varTempCounter;
    }

    @Override
    public Object visit(ASTMODULE node, Object data) {
        String leftChild    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        String rightChild   = (String) node.jjtGetChild(1).jjtAccept(this, data).toString();
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = " + leftChild.toString() + " MOD " + rightChild);
        return "t"+varTempCounter;
    }

    @Override
    public Object visit(ASTLOGICAL_NOT node, Object data) {
        String child    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = !" + child);
        return "t"+varTempCounter;
    }

    @Override
    public Object visit(ASTUNARY_MINUS node, Object data) {
        String child    = (String) node.jjtGetChild(0).jjtAccept(this, data).toString();
        
        varTempCounter++;
        printCode("t"+varTempCounter + " = -" + child);
        return "t"+varTempCounter;
    }

    @Override
    public Object visit(ASTIDENTIFYER node, Object data) {
        return node.value.toString();
    }

    @Override
    public Object visit(ASTDECIMAL_LITERAL node, Object data) {
        return (int) Integer.parseInt((String)node.value.toString());
    }

    @Override
    public Object visit(ASTHEX_LITERAL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTBOOL_LITERAL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTCHARACTER_LITERAL node, Object data) {
        return defaultVisit(node, data);
    }

    @Override
    public Object visit(ASTSTRING_LITERAL node, Object data) {
        return defaultVisit(node, data);
    }
    
}
