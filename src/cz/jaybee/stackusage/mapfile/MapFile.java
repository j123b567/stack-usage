package cz.jaybee.stackusage.mapfile;

import cz.jaybee.stackusage.callgraph.FunctionVertex;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Map file interface
 *
 * @author Jan Breuer
 */
public abstract class MapFile {

    private List<FunctionVertex> discardedFunctions;

    public MapFile() {
        discardedFunctions = new ArrayList<>();
    }

    public void addDiscardedFunction(FunctionVertex fn) {
        discardedFunctions.add(fn);
    }

    public boolean isFunctionDiscarded(FunctionVertex fn) {
        return discardedFunctions.indexOf(fn) >= 0;
    }

    public abstract void load(File file);

    public void load(List<File> files) {
        for (File f : files) {
            load(f);
        }
    }
}
