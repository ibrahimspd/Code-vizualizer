import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entites.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.util.FileUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main
{
    private static final Map<String, MethodDeclaration> methodDeclarations = new HashMap<>();

    public static void main(String[] args)
    {
        Javalin app = Javalin.create(/**/).start(8080);
        app.before(ctx -> ctx.header("Access-Control-Allow-Origin", "*"));
        app.post("/parse", Main::handle);
    }

    private static void handle(Context context) {
        long start = System.nanoTime();

        ReflectionTypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        JavaParserTypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("/tmp/CodeViz/"));
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver(reflectionTypeSolver, javaParserTypeSolver);


        UploadedFile uploadedFile = context.uploadedFiles().get(0);
        try (InputStream inputStream = uploadedFile.content()) {
            FileUtil.streamToFile(inputStream, "/tmp/fil.zip");
        } catch (Exception e) {
            e.printStackTrace();
        }


        TypeSolver jarTypeSolver = null;
        try {
            jarTypeSolver = new JarTypeSolver("/tmp/fil.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jarTypeSolver != null) {
            combinedTypeSolver.add(jarTypeSolver);
        }

        // Configure JavaParser to use the symbol solver
        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));

        StaticJavaParser.setConfiguration(parserConfiguration);


        Map<Object, Classes> classesMap = new ConcurrentHashMap<>();
        Map<String, FunctionCall> functionCalls = new ConcurrentHashMap<>();
        JsonObject newFilesMap = new JsonObject();
        List<CompilationUnit> fileData = new ArrayList<>();
        Gson gson = new Gson();

        try (ZipFile zipFile = new ZipFile("/tmp/fil.zip")) {
            Enumeration<? extends ZipEntry> filesFromZip = zipFile.entries();
            while (filesFromZip.hasMoreElements()) {
                // write the file to the disk



                ZipEntry fileFromZip = filesFromZip.nextElement();

                String filePath = fileFromZip.getName();
                if (filePath.endsWith(".java")) {
                    File file = new File("/tmp/CodeViz/"+ filePath);
                    file.getParentFile().mkdirs(); // Will create parent directories if not exists
                    file.createNewFile();
                    FileOutputStream s = new FileOutputStream(file,false);
                    InputStream inputStream2 = zipFile.getInputStream(fileFromZip);
                    InputStream inputStream = zipFile.getInputStream(fileFromZip);
                    s.write(inputStream2.readAllBytes());
                    System.out.println("File written to disk: " + file.getAbsolutePath());
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
                            int pathArraySize = pathArray.size();
                            String pathId = "";
                            for (int i = 0; i < pathArraySize; i++) {
                                String type = "";
                                if (i == pathArraySize -1) {
                                    type = "class";
                                } else {
                                    type = "package";
                                }
                                String path = pathArray.get(i);
                                pathId += "/" + path; // append path to pathId

                                if (!walker.has(path)) {
                                    JsonObject child = new JsonObject();
                                    child.addProperty("type", type);
                                    child.addProperty("id", pathId);
                                    if (type.equals("class")) {
                                        child.add("functions", gson.toJsonTree(classObj.getFunctions()));
                                    } else {
                                        child.add("children", new JsonObject());
                                    }
                                    walker.add(path, child);
                                }

                                walker = (JsonObject) walker.get(path).getAsJsonObject().get("children");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (CompilationUnit cu : fileData) {
              cu.accept(new MethodDeclarationVisitor(), null);
            }
            for (CompilationUnit cu : fileData) {
                cu.accept(new MethodCallVisitor(), null);
            }
            for (CompilationUnit cu : fileData) {
                // Find method calls and their corresponding method declarations
                /*for (String sourceFilePath : sourceFilePaths) {
                    File file = new File(sourceFilePath);
                    CompilationUnit cu = StaticJavaParser.parse(file);
                    cu.accept(new MethodCallVisitor(), null);
                }*/
                cu.findAll(MethodCallExpr.class).forEach(methodCallExpr ->
                    classesMap.forEach((s, classes) ->
                    {
                        try {

                            Optional<MethodDeclaration> classAncestor = methodCallExpr.findAncestor(MethodDeclaration.class);
                            String methodCaller = classAncestor.isPresent() ? classAncestor.get().getNameAsString() : null;
                            String methodClass = classAncestor.isPresent() ? methodCallExpr.findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString() : null;
                            String methodeCallExprName = methodCallExpr.getNameAsString();
                            Function function = classes.getFunctions().get(methodeCallExprName);

                            if (function != null) {
                                if (methodeCallExprName.equals(function.getId().split("#")[1])) {
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
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                );
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
        functionCalls.forEach((s, function) ->{

            function.getParents().forEach(parent ->
            {
                if (functionCalls.containsKey(parent)) {
                    functionCalls.get(parent).addChild(function.getName());
                }
                if(functionCalls.containsKey(parent)){

                functionCalls.get(parent).incrementOutGoing();
            }
            });
                }
        );
        JsonObject tree = new JsonObject();
        JsonObject program = new JsonObject();
        program.add("children", newFilesMap);
        tree.add("tree", program);

        tree.add("functions", gson.toJsonTree(functionCalls));
        context.result(gson.toJson(tree));
    }

    private static class MethodDeclarationVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            super.visit(n, arg);
            String methodCallSignature = "";
            try {
                String methodSignature = n.getName().asString();
                String methodClassname = n.findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString();
                methodCallSignature = methodClassname + "#" + methodSignature;
                System.out.println("method: " + methodCallSignature);
                methodDeclarations.put(methodCallSignature, n);
            }catch (Exception e) {
                System.err.println("Unable to resolve method declaration: " + methodCallSignature);
            }
        }
    }

    private static class MethodCallVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            try {
                System.out.println("methodcall: " + n.getName());
                ResolvedMethodDeclaration resolvedMethod = n.resolve();
                String methodCallSignature = resolvedMethod.getSignature();
                String className = resolvedMethod.declaringType().getQualifiedName();

                /*System.out.println("class: " + className.split("\\.")[className.split("\\.").length - 1]);
                System.out.println("method: " + methodCallSignature);
                System.out.println("--------------");*/
            } catch (Exception e) {
                e.printStackTrace();
               System.err.println("Unable to resolve method call: " + n);
            }
        }
    }
}
