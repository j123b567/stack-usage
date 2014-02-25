package cz.jaybee.stackusage.MapFile;

import cz.jaybee.stackusage.CallGraph.FunctionNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Map file interface
 * @author Jan Breuer
 */
public abstract class MapFile {

    private List<FunctionNode> discardedFunctions;

    public MapFile() {
        discardedFunctions = new ArrayList<>();
    }
    
    public void addDiscardedFunction(FunctionNode fn) {
        discardedFunctions.add(fn);
    }
    
    public boolean isFunctionDiscarded(FunctionNode fn) {        
        return discardedFunctions.indexOf(fn) >= 0;
    }
    
    public abstract void load(File file);
}
