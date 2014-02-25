package cz.jaybee.stackusage;

import cz.jaybee.stackusage.CallGraph.CallGraph;
import java.io.File;
import java.util.List;

/**
 *
 * @author jaybee
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
        
        StackUsage su = new StackUsage();
        CallGraph cg = new CallGraph();
        MapFile mf = new MapFile();
        
        for (File f: filesMap) {
            mf.LoadFile(f);
        }
        
        for (File f: filesStackUsage) {
            su.LoadFile(f);
        }
        
        for (File f: filesExpand) {
            cg.LoadFile(f);
        }           
        
        cg.process(su, mf);
        

        System.out.println("Rekurzivni funkce:");
        System.out.println(cg.printRecursiveFunctions());
        System.out.println();
        
        System.out.println("Funkce bez volajiciho:");
//        System.out.println(cg.printFunctions());
        System.out.println(cg.printRootFunctions());
        System.out.println();
        
    }
}
