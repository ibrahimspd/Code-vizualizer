package entites;

import java.util.HashMap;
import java.util.Map;

public class Tree
{
    Map<String, FunctionCall> functionCalls = new HashMap<>();

    public Tree(Map<String, FunctionCall> functionCalls)
    {
        this.functionCalls = functionCalls;
    }
}
