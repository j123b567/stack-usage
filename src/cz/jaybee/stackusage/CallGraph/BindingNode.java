package cz.jaybee.stackusage.CallGraph;

/**
 *
 * @author Jan Breuer
 */
public class BindingNode {

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
