package cz.jaybee.stackusage.CallGraph;

import cz.jaybee.stackusage.MapFile;
import cz.jaybee.stackusage.StackUsage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgrapht.alg.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 *
 * @author jaybee
 */
public class CallGraph {

    private static class BindingNode {

        FunctionNode caller;
        FunctionNode callee;
        CallType type;

        public BindingNode(FunctionNode caller, FunctionNode callee, CallType type) {
            this.caller = caller;
            this.callee = callee;
            this.type = type;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(caller);
            sb.append(";");
            sb.append(callee);
            sb.append(";");
            sb.append(type);

            return sb.toString();
        }
    }
    List<BindingNode> nodes;
    Set<FunctionNode> functions;
    Set<FunctionNode> recursiveFunctions;
    DirectedGraph<FunctionNode, CallTypeEdge> graph;
    Pattern pFunc;
    Pattern pCall;
    Pattern pRef;
    Pattern pPtr;
    Pattern pPtr2;
    Pattern pPtr3;

    public CallGraph() {
        nodes = new ArrayList<>();
        functions = new TreeSet<>();
        recursiveFunctions = null;

        pFunc = Pattern.compile("^;; Function (.*)\\s+\\((\\S+)(,.*)?\\)\\s*$");
        pCall = Pattern.compile("^.*\\(call.*\"(.*)\".*$");
        pRef = Pattern.compile("^.*\\(symbol_ref.*\"(.*)\".*$");
        pPtr = Pattern.compile("^.*\\(call.*\\(reg\\/.*\\*(.*)\\..*$");
        //(call (mem:SI (reg/v/f:SI 187 [ p_DataLoad ]) [0 *p_DataLoad_23(D) S4 A32])
        pPtr2 = Pattern.compile("^.*\\(call.*\\(reg\\/.* \\[ (.*) \\]\\).*$");
        pPtr3 = Pattern.compile("^.*\\(call.*\\(reg\\/[^ ]* ([^\\)]*)\\).*$");
    }

    private String removeAfterDot(String text) {
        int dotPos = text.indexOf(".");

        if (dotPos == -1) {
            return text;
        } else {
            return text.substring(0, dotPos);
        }
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
        for (BindingNode bn : nodes) {
            findCalleeFile(bn);
        }
    }

    private void findStackUsage(StackUsage su) {
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

    public void LoadFile(File file) {
        String callerFile = file.getName();
        FunctionNode caller = null;
        FunctionNode callee;

        {
            Pattern p = Pattern.compile("^(.*)\\..*\\.expand$");
            Matcher m = p.matcher(callerFile);
            if (m.find()) {
                callerFile = m.group(1);
            }
        }

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while (true) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }

                    Matcher m;
                    m = pFunc.matcher(line);
                    if (m.find()) {
                        caller = new FunctionNode(callerFile, removeAfterDot(m.group(2)));
                        functions.add(caller);
                        continue;
                    }

                    m = pCall.matcher(line);
                    if (m.find()) {
                        callee = new FunctionNode("", removeAfterDot(m.group(1)));
                        nodes.add(new BindingNode(caller, callee, CallType.CALL));
                        continue;
                    }

                    /*
                     m = pRef.matcher(line);
                     if (m.find()) {
                     callee = m.group(1);
                     if (callee.contains("*")) {
                     continue;
                     }
                     callee = removeAfterDot(callee);
                     nodes.add(new CallGraphNode(curFile, caller, callee, CallGraphType.REF));
                     continue;
                     }
                     */

                    m = pPtr.matcher(line);
                    if (m.find()) {
                        callee = new FunctionNode("", removeAfterDot(m.group(1)));
//                        if (callee.func.contains("*")) {
//                            continue;
//                        }
                        nodes.add(new BindingNode(caller, callee, CallType.PTR1));
                        continue;
                    }

                    m = pPtr2.matcher(line);
                    if (m.find()) {
                        callee = new FunctionNode("", m.group(1));
                        nodes.add(new BindingNode(caller, callee, CallType.PTR2));
                        continue;
                    }

                    m = pPtr3.matcher(line);
                    if (m.find()) {
                        callee = new FunctionNode("", "__PTR_FUNC_" + m.group(1));
                        nodes.add(new BindingNode(caller, callee, CallType.PTR3));
                        continue;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CallGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CallGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeBadReferences() {
        List<FunctionNode> fnlist = new ArrayList<>();

        for (FunctionNode fn : functions) {
            fnlist.add(fn);
        }
                
        for (BindingNode bn: nodes) {
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
                
        for (BindingNode bn : nodes) {
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

    private void removeDiscardedFunctions(MapFile mf) {
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

    public void process(StackUsage su, MapFile mf) {
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
}
