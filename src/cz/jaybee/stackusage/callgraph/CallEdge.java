package cz.jaybee.stackusage.callgraph;

import org.jgrapht.graph.DefaultEdge;

/**
 * Call graph edge
 * @author Jan Breuer
 */
public class CallEdge extends DefaultEdge {
    CallType type;
    
    public CallEdge(CallType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(getSource())
                .append(";")
                .append(getTarget())
                .append(";")
                .append(type);
        
        return sb.toString();
    }    
}
