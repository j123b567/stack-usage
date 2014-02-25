package cz.jaybee.stackusage.StackUsage;

import cz.jaybee.stackusage.CallGraph.FunctionNode;
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

    private List<FunctionNode> nodes;

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

    protected static List<FunctionNode> removeDuplicateFunctions(List<FunctionNode> fileNodes) {
        List<FunctionNode> result = new ArrayList<>();
        int index;
        FunctionNode cmpnode;

        for (FunctionNode node : fileNodes) {
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

    protected void addNodes(List<FunctionNode> _nodes) {
        nodes.addAll(_nodes);
    }

    public FunctionNode findNode(FunctionNode func) {
        int index = nodes.indexOf(func);
        if (index >= 0) {
            return nodes.get(index);
        } else {
            return null;
        }

    }

    public void removeUsed() {
        Iterator<FunctionNode> iter = nodes.iterator();
        while (iter.hasNext()) {
            if (iter.next().used) {
                iter.remove();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (FunctionNode node : nodes) {
            sb.append(node);
            sb.append("\n");
        }

        return sb.toString();
    }
    
    public abstract void load(File file);
}
