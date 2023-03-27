package entites;


import java.util.HashMap;
import java.util.Map;

public class Files
{
    private String fileName;
    private Map<Object, Classes> classes = new HashMap<>();
    private int nrOfFunctions = 0;

    public Files(String fileName, int nrOfFunctions, Map<Object, Classes> classes)
    {
        this.fileName = fileName;
        this.nrOfFunctions = nrOfFunctions;
        this.classes = classes;
    }
    {
        this.nrOfFunctions = nrOfFunctions;
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public Map<Object, Classes> getClasses()
    {
        return classes;
    }

    public void appendClass(Object key, Classes classe)
    {
        classes.put(key, classe);
    }
}

