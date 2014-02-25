package cz.jaybee.stackusage.callgraph;

import cz.jaybee.stackusage.mapfile.MapFile;
import cz.jaybee.stackusage.stackusage.StackUsage;
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

    private Set<FunctionVertex> functions;
    private Set<FunctionVertex> recursiveFunctions;
    private DirectedGraph<FunctionVertex, CallEdge> graph;
    private List<FunctionCall> functionCalls;

    public CallGraph() {
        functionCalls = new ArrayList<>();
        functions = new TreeSet<>();
        recursiveFunctions = null;
    }

    private List<FunctionVertex> getFunctionNodes(String func) {
        List<FunctionVertex> result = new ArrayList<>();

        for (FunctionVertex fn : functions) {
            if (fn.func.equals(func)) {
                result.add(fn);
            }
        }
        return result;
    }

    private void findCalleeFile(FunctionCall node) {
        FunctionVertex bestNode = null;
        for (FunctionVertex fnode : getFunctionNodes(node.callee.func)) {
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
        for (FunctionCall bn : functionCalls) {
            findCalleeFile(bn);
        }
    }

    private void findStackUsage(StackUsage su) {
        for (FunctionVertex fn : functions) {
            FunctionVertex fn2 = su.findNode(fn);
            if (fn2 != null) {
                fn.clone(fn2);
                fn2.used = true;
            }
        }

        su.removeUsed();

        for (FunctionVertex fn : functions) {
            if (fn.stack >= 0) {
                continue;
            }
            if (fn.file.isEmpty()) {
                continue;
            }
            FunctionVertex fn2 = su.findNode(new FunctionVertex(fn.file, "0"));
            if (fn2 != null) {
                fn.stack = fn2.stack;
                fn2.used = true;
            }
        }

        su.removeUsed();
    }

    private void removeBadReferences() {
        List<FunctionVertex> fnlist = new ArrayList<>();

        for (FunctionVertex fn : functions) {
            fnlist.add(fn);
        }

        for (FunctionCall bn : functionCalls) {
            bn.callee = fnlist.get(fnlist.indexOf(bn.callee));
            bn.caller = fnlist.get(fnlist.indexOf(bn.caller));
        }
    }

    private void createGraph() {
        DirectedGraph<FunctionVertex, CallEdge> directedGraph;
        directedGraph = new DefaultDirectedGraph<>(CallEdge.class);

        for (FunctionVertex fn : functions) {
            directedGraph.addVertex(fn);
        }

        for (FunctionCall bn : functionCalls) {
            try {
                directedGraph.addEdge(
                        bn.caller,
                        bn.callee,
                        new CallEdge(bn.type));
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
        for (FunctionVertex node : graph.vertexSet()) {
            sb.append(node);
            sb.append("\n");
        }

        return sb.toString();
    }

    private void removeDiscardedFunctions(MapFile mf) {
        Iterator<FunctionVertex> iter = functions.iterator();
        while (iter.hasNext()) {
            FunctionVertex fn = iter.next();
            if (mf.isFunctionDiscarded(fn)) {
                //System.out.println(fn);
                iter.remove();
            }
        }

    }

    private boolean isRecursive(FunctionVertex fn) {
        return getRecursiveFunctions().contains(fn);
    }

    public void printOutgoing(String func) {
        List<FunctionVertex> result = new ArrayList<>();

        for (FunctionVertex fn : graph.vertexSet()) {
            if (fn.func.equals(func)) {
                result.add(fn);
            }
        }

        FunctionVertex fn = result.get(0);
        System.out.println(fn);

        for (CallEdge cte : graph.outgoingEdgesOf(fn)) {
            System.out.println("->" + graph.getEdgeTarget(cte));
        }
    }

    private void traverseMaxStack(DirectedGraph<FunctionVertex, CallEdge> g) {
        int maxStack, fnStack;
        for (FunctionVertex fn : g.vertexSet()) {
            Set<CallEdge> ces = g.outgoingEdgesOf(fn);
            if (ces.isEmpty()) {
                if (fn.stack >= 0) {
                    fn.maxStack = fn.stack;
                } else {
                    fn.maxStack = 0;
                }
                continue;
            }

            maxStack = -1;
            for (CallEdge ce : ces) {
                fnStack = g.getEdgeTarget(ce).maxStack;
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

    private static DirectedGraph<FunctionVertex, CallEdge> cloneGraph(DirectedGraph<FunctionVertex, CallEdge> g) {
        DirectedGraph<FunctionVertex, CallEdge> result;
        result = new DefaultDirectedGraph<>(CallEdge.class);

        List<FunctionVertex> list = new ArrayList<>();

        for (FunctionVertex v : g.vertexSet()) {
            FunctionVertex v2 = new FunctionVertex();
            v2.clone(v);
            list.add(v2);
            result.addVertex(v2);
        }

        list.indexOf(g);
        FunctionVertex src;
        FunctionVertex dst;
        for (CallEdge e : g.edgeSet()) {
            src = g.getEdgeSource(e);
            dst = g.getEdgeTarget(e);

            /* get clonned vertex */
            src = list.get(list.indexOf(src));
            dst = list.get(list.indexOf(dst));

            result.addEdge(src, dst, new CallEdge(e.type));
        }

        return result;
    }

    public DirectedGraph<FunctionVertex, CallEdge> collapseGraph() {
        DirectedGraph<FunctionVertex, CallEdge> g = cloneGraph(graph);
        List<FunctionVertex> vToRemove = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            vToRemove.clear();
            /* remove all vertrtices with zero stack and no childs */
            for (FunctionVertex fn : g.vertexSet()) {
                Set<CallEdge> oe = g.outgoingEdgesOf(fn);
                if (oe.isEmpty() && fn.stack == 0) {
                    vToRemove.add(fn);
                }
            }
            g.removeAllVertices(vToRemove);
        }

        for (int i = 0; i < 1000; i++) {
            traverseMaxStack(g);
        }
        
        return g;
    }

    public void process(StackUsage su, MapFile mf) {
        findCalleeFiles();
        findStackUsage(su);
        removeBadReferences();
        removeDiscardedFunctions(mf);
        createGraph();
        /* TODO: rewrite to some inteligent algorithm */
        for (int i = 0; i < 1000; i++) {
            traverseMaxStack(graph);
        }
    }

    public Set<FunctionVertex> getRecursiveFunctions() {
        if (recursiveFunctions == null) {
            CycleDetector<FunctionVertex, CallEdge> cd;
            cd = new CycleDetector<>(graph);
            recursiveFunctions = cd.findCycles();
        }
        return recursiveFunctions;
    }

    public String printRecursiveFunctions() {
        StringBuilder sb = new StringBuilder();

        for (FunctionVertex fn : getRecursiveFunctions()) {
            sb.append(fn.file);
            sb.append("; ");
            sb.append(fn.func);
            sb.append("\n");
        }

        return sb.toString();

    }

    public String printCallGraphDot() {
        return printCallGraphDot(graph);
    }

    public String printCallGraphDot(DirectedGraph<FunctionVertex, CallEdge> g) {
        StringBuilder sb = new StringBuilder();

        String callerFile = "";
        int i = 0;

        sb.append("digraph callgraph {\n");

        for (FunctionVertex fn : g.vertexSet()) {
            if (!callerFile.equals(fn.file)) {
                if (!callerFile.isEmpty()) {
                    sb.append("\t\tlabel=\"").append(callerFile).append("\"\n");
                    sb.append("\t}\n\n");
                }
                callerFile = fn.file;
                sb.append("\tsubgraph cluster_").append(i++).append(" {\n");
            }

            sb.append("\t\t\"");
            sb.append(fn);
            sb.append("\"");
            sb.append(";\n");
        }

        if (!callerFile.isEmpty()) {
            sb.append("\t\tlabel=\"").append(callerFile).append("\"\n");
            sb.append("\t}\n\n");
        }


        for (CallEdge ce : g.edgeSet()) {
            sb.append("\t\"");
            sb.append(g.getEdgeSource(ce));
            sb.append("\"");
            sb.append(" -> ");
            sb.append("\"");
            sb.append(g.getEdgeTarget(ce));
            sb.append("\"");
            sb.append(";\n");
        }
        sb.append("}\n");

        return sb.toString();
    }

    public String printFunctions() {
        StringBuilder sb = new StringBuilder();

        for (FunctionVertex fn : graph.vertexSet()) {
            sb.append(fn);
            sb.append("\n");
        }

        return sb.toString();
    }

    public List<FunctionVertex> getRootFunctions() {
        List<FunctionVertex> result = new ArrayList<>();

        for (FunctionVertex fn : graph.vertexSet()) {
            if (graph.inDegreeOf(fn) == 0) {
                result.add(fn);
            }
        }

        return result;
    }

    public String printRootFunctions() {
        StringBuilder sb = new StringBuilder();

        for (FunctionVertex fn : getRootFunctions()) {
            sb.append(fn);
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

    protected void addFunction(FunctionVertex fn) {
        functions.add(fn);
    }

    protected void addFunctionCall(FunctionCall fnc) {
        functionCalls.add(fnc);
    }
}
