import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entites.*;
import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import io.javalin.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Main
{
    public static void main(String[] args)
    {
        Javalin app = Javalin.create(/**/).start(8080);
        app.before(ctx -> ctx.header("Access-Control-Allow-Origin", "*"));
        app.post("/parse", context ->
        {
            long start = System.nanoTime();

            UploadedFile uploadedFile = context.uploadedFiles().get(0);
            try (InputStream inputStream = uploadedFile.content())
            {
                FileUtil.streamToFile(inputStream, "/tmp/fil.zip");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            Map<Object, Classes> classesMap = new ConcurrentHashMap<>();
            Map<String, FunctionCall> functionCalls = new ConcurrentHashMap<>();
            JsonObject newFilesMap = new JsonObject();
            List<CompilationUnit> fileData = new ArrayList<>();
            Gson gson = new Gson();

            try (ZipFile zipFile = new ZipFile("/tmp/fil.zip")) {
                Enumeration<? extends ZipEntry> filesFromZip = zipFile.entries();
                while (filesFromZip.hasMoreElements())
                {
                    ZipEntry fileFromZip = filesFromZip.nextElement();

                    String filePath = fileFromZip.getName();
                    if (filePath.endsWith(".java"))
                    {
                        InputStream inputStream = zipFile.getInputStream(fileFromZip);
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(inputStream);
                            fileData.add(cu);
                                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classOrInterfaceDeclaration ->
                                {
                                    List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.getMethods();
                                    Map<Object, Function> functions = new HashMap<>();
                                    methodDeclarations.forEach(method ->
                                    {
                                        Map<Object, Object> parametersMap = new HashMap<>();
                                        method.getParameters().forEach(parameter ->
                                                parametersMap.put(parameter.getNameAsString(), parameter.getTypeAsString())
                                        );
                                        String functionName = method.getNameAsString();
                                        String functionCallName = classOrInterfaceDeclaration.getNameAsString() + "#" + functionName;
                                        Function function = new Function.Functionbuilder()
                                                .id(functionCallName)
                                                .build();
                                        functions.put(functionName, function);

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
                                    List<String> pathArray = Arrays.asList(filePath.split("/"));
                                    JsonObject walker = newFilesMap;
                                    for (int i = 0;  i < pathArray.size(); i++){
                                        String path = pathArray.get(i);
                                        if(walker == null){
                                            walker = new JsonObject();
                                            walker.add(path, new JsonObject());
                                        }
                                        else if (!walker.has(path)) {
                                            JsonObject temp = new JsonObject();
                                            if(path.endsWith(".java")){
                                                temp.addProperty("name", classOrInterfaceDeclaration.getNameAsString());
                                                temp.add("class", gson.toJsonTree(classObj));
                                            }
                                            walker.add(path, temp);
                                        }
                                        walker = (JsonObject) walker.get(path);
                                    }
                                });
                            }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                for (CompilationUnit cu : fileData){
                    cu.findAll(MethodCallExpr.class).forEach(methodCallExpr ->
                        classesMap.forEach((s, classes) ->
                        {
                            Optional<MethodDeclaration> classAncestor = methodCallExpr.findAncestor(MethodDeclaration.class);
                            String methodCaller = classAncestor.isPresent() ? classAncestor.get().getNameAsString() : null;
                            String methodClass = classAncestor.isPresent() ? methodCallExpr.findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString() : null;
                            String methodeCallExprName = methodCallExpr.getNameAsString();
                            Function function = classes.getFunctions().get(methodeCallExprName);
                            if (function != null) {
                                if (methodeCallExprName.equals(function.getId().split("#")[1]))
                                {
                                    FunctionCall functionCall = functionCalls.get(function.getId());
                                    String functionCaller = methodClass + "#" + methodCaller;

                                    if (!functionCall.getParents().contains(functionCaller))
                                    {
                                        functionCall.addParent(functionCaller);
                                    }
                                    functionCall.incrementNumberOfCalls();
                                    functionCall.incrementInComing();
                                }
                            }
                        })
                    );
                }
            } catch (IOException io) {
                io.printStackTrace();
            }
            functionCalls.forEach((s, function) ->
                function.getParents().forEach(parent ->
                {
                    if (functionCalls.containsKey(parent))
                    {
                        functionCalls.get(parent).addChild(function.getName());
                    }
                })
            );
            JsonObject tree = new JsonObject();

            tree.add("tree", newFilesMap);
            tree.add("functions", gson.toJsonTree(functionCalls));

            context.result(gson.toJson(tree));
        });
    }
}
