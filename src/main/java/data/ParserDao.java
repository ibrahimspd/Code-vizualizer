package data;

import bussiness.Parser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import entites.Files;
import entites.Function;
import entites.Modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ParserDao
{
    private final Map<String,Function> functions = new HashMap<>();
    private static ParserDao pythonDao = null;

    private ParserDao()
    {
    }
    public static ParserDao getInstance(){
        if (pythonDao == null){
            pythonDao = new ParserDao();
        }
        return pythonDao;
    }

    public Map<String,Function> parseFunctions(){
        Map<String,Function> functions = new HashMap<>();
        try( ZipFile zipFile = new ZipFile("/Users/ibra/filer/fil.zip")) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            zipFile.entries().asIterator().forEachRemaining(s -> {
                if (s.getName().endsWith(".java"))
                {
                    InputStream inputStream = null;
                    try
                    {
                        inputStream = zipFile.getInputStream(s);
                    } catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                    Scanner scanner = new Scanner(inputStream);
                    StringBuilder javaCode = new StringBuilder();
                    while (scanner.hasNext())
                    {
                        javaCode.append(scanner.nextLine() + "\n");
                    }
                    CompilationUnit cu = StaticJavaParser.parse(javaCode.toString());
                    Parser parser = new Parser(cu);
                    Function function = parser.parseFunctions();
                }
            });
            while (entries.hasMoreElements()){

                ZipEntry entry = entries.nextElement();
                System.out.println(entry.getName());
                if (entry.getName().endsWith(".java"))
                {



                }

            }
            System.out.println(functions);
        } catch (Exception e)
        {

        }
return null;
    }

}
