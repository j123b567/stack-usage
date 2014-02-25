package cz.jaybee.stackusage;

import cz.jaybee.stackusage.CallGraph.FunctionNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jaybee
 */
public class StackUsage {

    List<FunctionNode> nodes;

    public StackUsage() {
        nodes = new ArrayList<>();
    }

    public void LoadFile(File file) {
        List<FunctionNode> fileNodes = new ArrayList<>();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while (true) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }


                    FunctionNode node = new FunctionNode();
                    String[] sline = line.split(":");
                    node.file = sline[0];
                    node.line = Integer.parseInt(sline[1]);
                    node.charPos = Integer.parseInt(sline[2]);

                    sline = sline[3].split("\t");
                    node.func = sline[0];
                    node.stack = Integer.parseInt(sline[1]);
                    node.type = sline[2];

                    fileNodes.add(node);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StackUsage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StackUsage.class.getName()).log(Level.SEVERE, null, ex);
        }

        nodes.addAll(removeDuplicateFunctions(fileNodes));
    }

    private boolean isNumber(String num) {
        try {
            Integer.parseInt(num);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private List<FunctionNode> removeDuplicateFunctions(List<FunctionNode> fileNodes) {
        List<FunctionNode> result = new ArrayList<>();
        int index;
        FunctionNode cmpnode;

        for (FunctionNode node : fileNodes) {
            for (index = result.size() - 1; index >=0; index --) {
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
}
