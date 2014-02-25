package cz.jaybee.stackusage.cli;

import cz.jaybee.stackusage.callgraph.CallGraph;
import cz.jaybee.stackusage.callgraph.CallGraphGCC;
import java.io.File;

/**
 * Main example code
 *
 * @author Jan Breuer
 */
public class CliMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String baseDir = "./";

        if (args.length == 1) {
            baseDir = args[0];
        }

        CallGraph cg = new CallGraphGCC(new File(baseDir));

        System.out.println("Recursive functions:");
        System.out.println(cg.printRecursiveFunctions());
        System.out.println();

        System.out.println("Root functions:");
        System.out.println(cg.printRootFunctions());
        System.out.println();

        // System.out.println("Full call graph:");
        // System.out.println(cg.printFunctions());
        // System.out.println();


    }
}
