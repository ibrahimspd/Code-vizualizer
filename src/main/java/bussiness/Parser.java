package bussiness;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import entites.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Parser
{
    CompilationUnit cu;


    public Parser(CompilationUnit cu)
    {
        this.cu = cu;
    }

    public Function parseFunctions()
    {
    return null;}
}
