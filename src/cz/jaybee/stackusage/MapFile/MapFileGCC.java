package cz.jaybee.stackusage.MapFile;

import cz.jaybee.stackusage.CallGraph.FunctionNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manipulating of gcc *.map file
 *
 * @author Jan Breuer
 */
public final class MapFileGCC extends MapFile {

    private enum Section {

        Undefined(""),
        ArchiveMemberIncludedBecauseOfFile("Archive member included because of file"),
        AllocatingCommonSymbols("Allocating common symbols"),
        DiscardedInputSections("Discarded input sections"),
        MemoryConfiguration("Memory Configuration"),
        LinkerScriptAndMemoryMap("Linker script and memory map");
        private String pattern;

        private Section(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }

    public MapFileGCC() {
        super();
    }

    @Override
    public void load(File file) {

        boolean newSection = false;
        Section section = Section.Undefined;

        Pattern p = Pattern.compile("^ *\\.text\\.([^ ]*) *[^ ]* *[^ ]* *(.*).o$");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                for (Section s : Section.values()) {
                    if (s == Section.Undefined) {
                        continue;
                    }
                    if (line.startsWith(s.getPattern())) {
                        section = s;
                        newSection = true;
                        break;
                    }
                }
                if (newSection) {
                    continue;
                }
                
                switch (section) {
                    case DiscardedInputSections:
                        if (line.contains(".text.")) {
                            if (!line.contains("0x00000000")) {
                                line += br.readLine();
                            }
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                FunctionNode fn = new FunctionNode(m.group(2), m.group(1));
                                fn.file = fn.file.substring(fn.file.lastIndexOf('\\') + 1) + ".c";

                                addDiscardedFunction(fn);
                            }
                        }
                        break;
                    default:

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MapFileGCC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
