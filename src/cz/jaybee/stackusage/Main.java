package cz.jaybee.stackusage;

import cz.jaybee.stackusage.MapFile.MapFileGCC;
import cz.jaybee.stackusage.StackUsage.StackUsageGCC;
import cz.jaybee.stackusage.CallGraph.CallGraphGCC;
import java.io.File;
import java.util.List;

/**
 * Main example code
 *
 * @author Jan Breuer
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String basedir = "./";

        if (args.length == 1) {
            basedir = args[0];
        }

        List<File> filesExpand = FileUtils.ListFiles(basedir, ".expand");
        List<File> filesStackUsage = FileUtils.ListFiles(basedir, ".su");
        List<File> filesMap = FileUtils.ListFiles(basedir, ".map");

        StackUsageGCC su = new StackUsageGCC();
        CallGraphGCC cg = new CallGraphGCC();
        MapFileGCC mf = new MapFileGCC();

        for (File f : filesMap) {
            mf.load(f);
        }

        for (File f : filesStackUsage) {
            su.load(f);
        }

        for (File f : filesExpand) {
            cg.load(f);
        }

        cg.process(su, mf);


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
