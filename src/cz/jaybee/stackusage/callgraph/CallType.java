package cz.jaybee.stackusage.callgraph;

/**
 * Type of function calls
 * @author Jan Breuer
 */
public enum CallType {
    UNUSED,
    CALL,
    REF,
    PTR1,
    PTR2,
    PTR3
}
