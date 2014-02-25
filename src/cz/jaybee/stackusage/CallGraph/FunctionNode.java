package cz.jaybee.stackusage.CallGraph;

import java.util.Objects;

/**
 * Call Graph node
 * @author Jan Breuer
 */
public class FunctionNode implements Comparable<FunctionNode> {
    public String file;
    public String func;
    public int stack;
       
    public int line;
    public int charPos;
    public String type;
    
    public boolean used;
    
    public int maxStack;
    
    public FunctionNode(String file, String func) {
        this.file = file;
        this.func = func;
        this.stack = -1;
        this.used = false;
        this.maxStack = -1;
    }

    public FunctionNode() {
        this.file = "";
        this.func = "";
        this.stack = -1;
        this.used = false;
        this.maxStack = -1;
    }
   
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(file).append(":").append(func).append(":").append(maxStack);
        
        return sb.toString();
    }    

    @Override
    public int compareTo(FunctionNode o) {
        if (file.equals(o.file)) {
            return func.compareTo(o.func);
        } else {
            return file.compareTo(o.file);            
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != FunctionNode.class) {
            return false;
        }
        return compareTo((FunctionNode) obj) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.file);
        hash = 97 * hash + Objects.hashCode(this.func);
        return hash;
    }
    
    public void clone(FunctionNode o) {
        this.charPos = o.charPos;
        this.file = o.file;
        this.func = o.func;
        this.line = o.line;
        this.stack = o.stack;
        this.type = o.type;
    }
}
