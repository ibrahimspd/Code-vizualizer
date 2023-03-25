package entites;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class MethodClassVisitor extends VoidVisitorAdapter<Void>
{
    private Map<String, Integer> callCounts = new HashMap<>();
    private Map<String, List<String>> callers = new HashMap<>();
    private Map<String, List<String>> callees = new HashMap<>();
    private Set<String> recursiveMethods = new HashSet<>();

    @Override
    public void visit(MethodDeclaration n, Void arg)
    {
        String methodName = n.getNameAsString();
        String returnType = n.getTypeAsString();
        List<String> parameters = new ArrayList<>();
        n.getParameters().forEach(p -> parameters.add(p.getTypeAsString()));
        super.visit(n, arg);
    }

    public Map<String, Integer> getCallCounts()
    {
        return callCounts;
    }

    public void setCallCounts(Map<String, Integer> callCounts)
    {
        this.callCounts = callCounts;
    }

    public Map<String, List<String>> getCallers()
    {
        return callers;
    }

    public void setCallers(Map<String, List<String>> callers)
    {
        this.callers = callers;
    }

    public Map<String, List<String>> getCallees()
    {
        return callees;
    }

    public void setCallees(Map<String, List<String>> callees)
    {
        this.callees = callees;
    }

    public Set<String> getRecursiveMethods()
    {
        return recursiveMethods;
    }

    public void setRecursiveMethods(Set<String> recursiveMethods)
    {
        this.recursiveMethods = recursiveMethods;
    }

    @Override
    public void visit(MethodCallExpr n, Void arg)
    {
        String callName = n.getNameAsString();
        if (callees.containsKey(callName)){
            callees.get(callName).add(n.toString());
        } else {
            callees.put(callName, new ArrayList<>(Collections.singletonList(n.getNameAsString())));
        }
        super.visit(n, arg);
    }
}
