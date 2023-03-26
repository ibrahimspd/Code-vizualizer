import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import endpoints.GraphController;
import entites.*;
import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import io.javalin.util.FileUtil;
import org.eclipse.jetty.plus.jndi.Link;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Main
{

    public static void main(String[] args)
    {
        Javalin app = Javalin.create(/**/).start(8080);
        app.post("/parse", context ->
        {
            UploadedFile uploadedFile = context.uploadedFiles().get(0);
            try (InputStream inputStream = uploadedFile.content())
            {
                FileUtil.streamToFile(inputStream, "/tmp/fil.zip");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            Map<Object, Files> filesMap = new HashMap<>();
            Map<Object, Classes> classesMap = new HashMap<>();
            Map<String, FunctionCall> functionCalls = new HashMap<>();
            JsonObject newFilesMap = new JsonObject();
            try (ZipFile zip = new ZipFile("/tmp/fil.zip"))
            {

                Enumeration<? extends ZipEntry> filesFromZip = zip.entries();
                Enumeration<? extends ZipEntry> secondEntry = zip.entries();

                while (filesFromZip.hasMoreElements())
                {
                    ZipEntry fileFromZip = filesFromZip.nextElement();

                    String filePath = fileFromZip.getName();
                    if (filePath.endsWith(".java"))
                    {
                        InputStream inputStream = zip.getInputStream(fileFromZip);
                        Scanner scanner = new Scanner(inputStream);
                        StringBuilder javaCode = new StringBuilder();
                        while (scanner.hasNext())
                        {
                            String nl = scanner.nextLine();
                            javaCode.append(nl + "\n");

                        }
                        CompilationUnit cu = StaticJavaParser.parse(javaCode.toString());

                        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classOrInterfaceDeclaration ->
                        {
                            List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.getMethods();
                            Map<Object, Object> parametersMap = new HashMap<>();
                            Map<Object, Function> functions = new HashMap<>();
                            Map<Object, Classes> classesHashMap = new HashMap<>();
                            methodDeclarations.forEach(method ->
                            {
                                method.getParameters().forEach(parameter ->
                                {
                                    parametersMap.put(parameter.getNameAsString(), parameter.getTypeAsString());
                                });
                                String functionName = method.getNameAsString();
                                String functionCallName = classOrInterfaceDeclaration.getNameAsString() + "#" + functionName;
                                Function function = new Function.Functionbuilder()
                                        .id(functionCallName)
                                        .build();
                                functions.put(method.getNameAsString(), function);

                                FunctionCall functionCall = new FunctionCall
                                        .FunctionCallBuilder()
                                        .setChildren(new ArrayList<>())
                                        .setInComing(0).setOutGoing(0)
                                        .setNumberOfCalls(0)
                                        .setParameters(parametersMap)
                                        .setParents(new ArrayList<>()).setRecursive("false")
                                        .setName(functionCallName).build();
                                functionCalls.put(functionCallName, functionCall);
                            });
                            Classes classObj = new Classes(functions, classOrInterfaceDeclaration.getNameAsString(), null, UUID.randomUUID().toString(), filePath);
                            classesMap.put(classObj.getName(), classObj);
                            classesHashMap.put(classObj.getName(), classObj);
                            List<String> pathArray = Arrays.asList(filePath.split("/"));
                            JsonObject walker = newFilesMap;
                            for (int i = 0;  i < pathArray.size(); i++){
                                String path = pathArray.get(i);
                                if(walker == null){
                                    walker = new JsonObject();
                                    walker.add(path, new JsonObject());
                                }
                                else if (!walker.has(path)) {
                                    walker.add(path, new JsonObject());walker = (JsonObject) walker.get(path);
                                }
                                if(i == pathArray.size() - 1){
                                    walker.addProperty("name", classOrInterfaceDeclaration.getNameAsString());
                                    walker.add("class", new Gson().toJsonTree(classObj));
                                }
                                walker = (JsonObject) walker.get(path);

                            }

                        });
                    }
                }
                System.out.println(newFilesMap);

                while (secondEntry.hasMoreElements())
                {
                    ZipEntry entry = secondEntry.nextElement();
                    String fileName = entry.getName();

                    if (fileName.endsWith(".java"))
                    {
                        InputStream inputStream = zip.getInputStream(entry);
                        Scanner scanner = new Scanner(inputStream);
                        StringBuilder javaCode = new StringBuilder();
                        while (scanner.hasNext())
                        {
                            javaCode.append(scanner.nextLine() + "\n");
                        }
                        CompilationUnit cu = StaticJavaParser.parse(javaCode.toString());
                        cu.findAll(MethodCallExpr.class).forEach(methodCallExpr ->
                        {
                            classesMap.forEach((s, classes) ->
                            {
                                String methodCaller = methodCallExpr.findAncestor(MethodDeclaration.class).isPresent() ? methodCallExpr.findAncestor(MethodDeclaration.class).get().getNameAsString() : null;
                                String methodClass = methodCallExpr.findAncestor(MethodDeclaration.class).isPresent() ? methodCallExpr.findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString() : null;
                                Function function = classes.getFunctions().get(methodCallExpr.getNameAsString());
                                if (function != null)
                                {
                                    String className = function.getId().split("#")[0];
                                    String methodName = function.getId().split("#")[1];
                                    if (methodCallExpr.getNameAsString().equals(methodName))
                                    {
                                        FunctionCall functionCall = functionCalls.get(className + "#" + methodCallExpr.getNameAsString());
                                        String functionCaller = methodClass + "#" + methodCaller;

                                        if (!functionCall.getParents().contains(functionCaller))
                                        {
                                            functionCall.addParent(functionCaller);
                                        }
                                        functionCall.incrementNumberOfCalls();
                                        functionCall.incrementInComing();
                                    }
                                }
                                ;
                            });
                        });
                    }
                }


            } catch (IOException io)
            {
                io.printStackTrace();
            }
            Gson Gson = new Gson();
            functionCalls.forEach((s, function) ->
            {
                function.getParents().forEach(parent ->
                {

                    if (functionCalls.containsKey(parent))
                    {

                        functionCalls.get(parent).addChild(function.getName());
                    }
                });
            });
            JsonObject tree = new JsonObject();


            tree.add("tree", newFilesMap);
            tree.add("functions", new Gson().toJsonTree(functionCalls));
            context.result(Gson.toJson(tree));
        });
    }
}
