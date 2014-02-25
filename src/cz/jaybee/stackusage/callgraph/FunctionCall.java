package cz.jaybee.stackusage.callgraph;

/**
 *
 * @author Jan Breuer
 */
public class FunctionCall {

    FunctionVertex caller;
    FunctionVertex callee;
    CallType type;

    public FunctionCall(FunctionVertex caller, FunctionVertex callee, CallType type) {
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
