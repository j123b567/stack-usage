package cz.jaybee.stackusage;

import cz.jaybee.stackusage.CallGraph.FunctionNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jaybee
 */
public class MapFile {

    List<FunctionNode> discardedInputSections;

    public MapFile() {
        discardedInputSections = new ArrayList<>();
    }
    
    public void LoadFile(File file) {

        boolean discardedSections = false;;
        Pattern p = Pattern.compile("^ *\\.text\\.([^ ]*) *[^ ]* *[^ ]* *(.*).o$");
        
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while (true) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }

                    if (line.startsWith("Discarded")) {
                        discardedSections = true;
                        continue;
                    }
                    if (line.contains(".text.")) {
                        if (!line.contains("0x00000000")) {
                            line += br.readLine();
                        }
                        Matcher m = p.matcher(line);
                        if(m.find()) {
                            FunctionNode fn = new FunctionNode(m.group(2), m.group(1));
                            fn.file = fn.file.substring(fn.file.lastIndexOf('\\') + 1) + ".c";
                            
                            discardedInputSections.add(fn);
                        }
                    }
                    if (discardedSections && !line.isEmpty() && !line.startsWith(" ")) {
                        discardedSections = false;
                        break;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MapFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MapFile.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public boolean isFunctionDiscarded(FunctionNode fn) {
        
        return discardedInputSections.indexOf(fn) >= 0;
    }
}
