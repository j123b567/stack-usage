package cz.jaybee.stackusage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jaybee
 */
public class FileUtils {

    public static List<File> ListFiles(String path, String extension) {

        List<File> result = new ArrayList<>();
        File root = new File(path);
        File[] list;
        
        if (root.isDirectory()) {
            list = root.listFiles();
        } else {
            list = new File[1];
            list[0] = root;
        }
        
        if (list == null) {
            return null;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                result.addAll(ListFiles(f.getPath(), extension));
            } else if (f.isFile()) {
                if (f.getName().endsWith(extension)) {
                    result.add(f);
                }
            }
        }
        
        return result;
    }
}
