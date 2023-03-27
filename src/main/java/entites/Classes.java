package entites;

import java.util.List;
import java.util.Map;

public class Classes
{
    private String id;
    private Map<Object,Function> functions;
    private String fileName;
    private String name;
    private List<FunctionPair> pairs;
    public Classes(Map<Object,Function> functions, String name, List<FunctionPair> pairs, String id, String fileName)
    {
        this.fileName = fileName;
        this.id = id;
        this.functions = functions;
        this.name = name;
        this.pairs = pairs;
    }

    @Override
    public String toString()
    {
        return "Classes{" +
                "id='" + id + '\'' +
                ", functions=" + functions +
                ", fileName='" + fileName + '\'' +
                ", name='" + name + '\'' +
                ", pairs=" + pairs +
                '}';
    }

    public Map<Object, Function> getFunctions()
    {
        return functions;
    }

    public String getId()
    {
        return id;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getName()
    {
        return name;
    }

    public List<FunctionPair> getPairs()
    {
        return pairs;
    }
}
