package cz.jaybee.stackusage.CallGraph;

import cz.jaybee.stackusage.MapFile.MapFileGCC;
import cz.jaybee.stackusage.StackUsage.StackUsageGCC;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * Call Graph implementation
 *
 * @author Jan Breuer
 */
public abstract class CallGraph {

    private Set<FunctionNode> functions;
    private Set<FunctionNode> recursiveFunctions;
    private DirectedGraph<FunctionNode, CallTypeEdge> graph;
    private List<BindingNode> bindings;

    public CallGraph() {
        bindings = new ArrayList<>();
        functions = new TreeSet<>();
        recursiveFunctions = null;
    }

    private List<FunctionNode> getFunctionNodes(String func) {
        List<FunctionNode> result = new ArrayList<>();

        for (FunctionNode fn : functions) {
            if (fn.func.equals(func)) {
                result.add(fn);
            }
        }
        return result;
    }

    private void findCalleeFile(BindingNode node) {
        FunctionNode bestNode = null;
        for (FunctionNode fnode : getFunctionNodes(node.callee.func)) {
            if (fnode.file.equals(node.caller.file)) {
                bestNode = fnode;
            }

            if (bestNode == null) {
                bestNode = fnode;
            }
        }
        if (bestNode == null) {
            bestNode = node.callee;
            functions.add(bestNode);
        }

        node.callee = bestNode;
    }

    private void findCalleeFiles() {
        for (BindingNode bn : bindings) {
            findCalleeFile(bn);
        }
    }

    private void findStackUsage(StackUsageGCC su) {
        for (FunctionNode fn : functions) {
            FunctionNode fn2 = su.findNode(fn);
            if (fn2 != null) {
                fn.clone(fn2);
                fn2.used = true;
            }
        }

        su.removeUsed();

        for (FunctionNode fn : functions) {
            if (fn.stack >= 0) {
                continue;
            }
            if (fn.file.isEmpty()) {
                continue;
            }
            FunctionNode fn2 = su.findNode(new FunctionNode(fn.file, "0"));
            if (fn2 != null) {
                fn.stack = fn2.stack;
                fn2.used = true;
            }
        }

        su.removeUsed();
    }

    private void removeBadReferences() {
        List<FunctionNode> fnlist = new ArrayList<>();

        for (FunctionNode fn : functions) {
            fnlist.add(fn);
        }

        for (BindingNode bn : bindings) {
            bn.callee = fnlist.get(fnlist.indexOf(bn.callee));
            bn.caller = fnlist.get(fnlist.indexOf(bn.caller));
        }
    }

    private void createGraph() {
        DirectedGraph<FunctionNode, CallTypeEdge> directedGraph;
        directedGraph = new DefaultDirectedGraph<>(CallTypeEdge.class);

        for (FunctionNode fn : functions) {
            directedGraph.addVertex(fn);
        }

        for (BindingNode bn : bindings) {
            try {
                directedGraph.addEdge(bn.caller, bn.callee, new CallTypeEdge(bn.type));
            } catch (Exception ex) {
                //System.out.println(bn);
            }
        }

        graph = directedGraph;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        //for (CallTypeEdge node : graph.edgeSet()) {
        for (FunctionNode node : graph.vertexSet()) {
            sb.append(node);
            sb.append("\n");
        }

        return sb.toString();
    }

    private void removeDiscardedFunctions(MapFileGCC mf) {
        Iterator<FunctionNode> iter = functions.iterator();
        while (iter.hasNext()) {
            FunctionNode fn = iter.next();
            if (mf.isFunctionDiscarded(fn)) {
                //System.out.println(fn);
                iter.remove();
            }
        }

    }

    private boolean isRecursive(FunctionNode fn) {
        return getRecursiveFunctions().contains(fn);
    }

    public void printOutgoing(String func) {
        List<FunctionNode> result = new ArrayList<>();

        for (FunctionNode fn : graph.vertexSet()) {
            if (fn.func.equals(func)) {
                result.add(fn);
            }
        }

        FunctionNode fn = result.get(0);
        System.out.println(fn);

        for (CallTypeEdge cte : graph.outgoingEdgesOf(fn)) {
            System.out.println("->" + graph.getEdgeTarget(cte));
        }
    }

    private void traverseMaxStack() {
        int maxStack, fnStack;
        for (FunctionNode fn : graph.vertexSet()) {
            Set<CallTypeEdge> ctes = graph.outgoingEdgesOf(fn);
            if (ctes.isEmpty()) {
                if (fn.stack >= 0) {
                    fn.maxStack = fn.stack;
                } else {
                    fn.maxStack = 0;
                }
                continue;
            }

            maxStack = -1;
//            if (fn.func.equals("DoAlt")) {
//                System.out.println(fn);
//            }
            for (CallTypeEdge cte : ctes) {
//                if (fn.func.equals("DoAlt")) {
//                    System.out.println("->" + graph.getEdgeTarget(cte));
//                }
                fnStack = graph.getEdgeTarget(cte).maxStack;
                if (fnStack >= 0) {
                    maxStack = Math.max(maxStack, fnStack);
                } else {
                    maxStack = -1;
                    break;
                }
            }
            if (maxStack >= 0) {
                if (isRecursive(fn)) {
                } else {
                    fn.maxStack = maxStack + fn.stack;
                }
            } else if (isRecursive(fn)) {
                fn.maxStack = fn.stack;
            }
        }
    }

    public void process(StackUsageGCC su, MapFileGCC mf) {
        findCalleeFiles();
        findStackUsage(su);
        removeBadReferences();
        removeDiscardedFunctions(mf);
        createGraph();
        for (int i = 0; i < 1000; i++) {
            traverseMaxStack();
        }
    }

    public Set<FunctionNode> getRecursiveFunctions() {
        if (recursiveFunctions == null) {
            CycleDetector<FunctionNode, CallTypeEdge> cd = new CycleDetector<>(graph);
            recursiveFunctions = cd.findCycles();
        }
        return recursiveFunctions;
    }

    public String printRecursiveFunctions() {
        StringBuilder sb = new StringBuilder();

        for (FunctionNode fn : getRecursiveFunctions()) {
            sb.append(fn.file);
            sb.append("; ");
            sb.append(fn.func);
            sb.append("\n");
        }

        return sb.toString();

    }

    public String printFunctions() {
        StringBuilder sb = new StringBuilder();

        for (FunctionNode fn : graph.vertexSet()) {
            sb.append(fn);
            sb.append("\n");
        }

        return sb.toString();
    }

    public List<FunctionNode> getRootFunctions() {
        List<FunctionNode> result = new ArrayList<>();

        for (FunctionNode fn : graph.vertexSet()) {
            if (graph.inDegreeOf(fn) == 0) {
                result.add(fn);
            }
        }

        return result;
    }

    public String printRootFunctions() {
        StringBuilder sb = new StringBuilder();

        for (FunctionNode fn : getRootFunctions()) {
            sb.append(fn);
            sb.append("\n");
        }
        return sb.toString();
    }

    public abstract void load(File file);
    
    protected void addFunction(FunctionNode fn) {
        functions.add(fn);
    }
    
    protected void addBinding(BindingNode bn) {
        bindings.add(bn);
    }
}
