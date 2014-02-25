package cz.jaybee.stackusage.CallGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Load *.expand files
 *
 * @author Jan Breuer
 */
public class CallGraphGCC extends CallGraph {

    public CallGraphGCC() {
        super();
    }

    private String removeAfterDot(String text) {
        int dotPos = text.indexOf(".");

        if (dotPos == -1) {
            return text;
        } else {
            return text.substring(0, dotPos);
        }
    }

    @Override
    public void load(File file) {

        Pattern pFunc = Pattern.compile("^;; Function (.*)\\s+\\((\\S+)(,.*)?\\)\\s*$");
        Pattern pCall = Pattern.compile("^.*\\(call.*\"(.*)\".*$");
        Pattern pRef = Pattern.compile("^.*\\(symbol_ref.*\"(.*)\".*$");
        Pattern pPtr = Pattern.compile("^.*\\(call.*\\(reg\\/.*\\*(.*)\\..*$");
        //(call (mem:SI (reg/v/f:SI 187 [ p_DataLoad ]) [0 *p_DataLoad_23(D) S4 A32])
        Pattern pPtr2 = Pattern.compile("^.*\\(call.*\\(reg\\/.* \\[ (.*) \\]\\).*$");
        Pattern pPtr3 = Pattern.compile("^.*\\(call.*\\(reg\\/[^ ]* ([^\\)]*)\\).*$");

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

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                Matcher m;
                m = pFunc.matcher(line);
                if (m.find()) {
                    caller = new FunctionNode(callerFile, removeAfterDot(m.group(2)));
                    addFunction(caller);
                    continue;
                }

                m = pCall.matcher(line);
                if (m.find()) {
                    callee = new FunctionNode("", removeAfterDot(m.group(1)));
                    addBinding(new BindingNode(caller, callee, CallType.CALL));
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
                    addBinding(new BindingNode(caller, callee, CallType.PTR1));
                    continue;
                }

                m = pPtr2.matcher(line);
                if (m.find()) {
                    callee = new FunctionNode("", m.group(1));
                    addBinding(new BindingNode(caller, callee, CallType.PTR2));
                    continue;
                }

                m = pPtr3.matcher(line);
                if (m.find()) {
                    callee = new FunctionNode("", "__PTR_FUNC_" + m.group(1));
                    addBinding(new BindingNode(caller, callee, CallType.PTR3));
                    continue;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CallGraphGCC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
