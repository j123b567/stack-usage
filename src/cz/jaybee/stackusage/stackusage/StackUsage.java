package cz.jaybee.stackusage.stackusage;

import cz.jaybee.stackusage.callgraph.FunctionVertex;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stack usage interface
 *
 * @author Jan Breuer
 */
public abstract class StackUsage {

    private List<FunctionVertex> nodes;

    public StackUsage() {
        nodes = new ArrayList<>();
    }

    private static boolean isNumber(String num) {
        try {
            Integer.parseInt(num);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    protected static List<FunctionVertex> removeDuplicateFunctions(List<FunctionVertex> fileNodes) {
        List<FunctionVertex> result = new ArrayList<>();
        int index;
        FunctionVertex cmpnode;

        for (FunctionVertex node : fileNodes) {
            for (index = result.size() - 1; index >= 0; index--) {
                cmpnode = result.get(index);

                if (cmpnode.line == node.line) {
                    if (isNumber(cmpnode.func)) {
                        result.set(index, node);
                    }
                    node.stack = Math.max(cmpnode.stack, node.stack);
                    continue;
                }
            }
            result.add(node);
        }

        return result;
    }

    protected void addNodes(List<FunctionVertex> _nodes) {
        nodes.addAll(_nodes);
    }

    public FunctionVertex findNode(FunctionVertex func) {
        int index = nodes.indexOf(func);
        if (index >= 0) {
            return nodes.get(index);
        } else {
            return null;
        }

    }

    public void removeUsed() {
        Iterator<FunctionVertex> iter = nodes.iterator();
        while (iter.hasNext()) {
            if (iter.next().used) {
                iter.remove();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (FunctionVertex node : nodes) {
            sb.append(node);
            sb.append("\n");
        }

        return sb.toString();
    }

    public abstract void load(File file);

    public void load(List<File> files) {
        for (File f : files) {
            load(f);
        }
    }
}
