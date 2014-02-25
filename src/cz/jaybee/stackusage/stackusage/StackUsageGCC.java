package cz.jaybee.stackusage.stackusage;

import cz.jaybee.stackusage.callgraph.FunctionVertex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stack usage implementation using gcc *.su files
 *
 * @author Jan Breuer
 */
public class StackUsageGCC extends StackUsage {

    public StackUsageGCC() {
        super();
    }

    /**
     * Load *.su file
     *
     * @param file
     */
    @Override
    public void load(File file) {
        List<FunctionVertex> fileNodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                FunctionVertex node = new FunctionVertex();
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
        } catch (IOException ex) {
            Logger.getLogger(StackUsageGCC.class.getName()).log(Level.SEVERE, null, ex);
        }

        addNodes(removeDuplicateFunctions(fileNodes));
    }
}
