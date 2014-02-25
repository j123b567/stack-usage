package cz.jaybee.stackusage.CallGraph;

import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author jaybee
 */
public class CallTypeEdge extends DefaultEdge {
    CallType type;
    
    public CallTypeEdge(CallType type) {
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
