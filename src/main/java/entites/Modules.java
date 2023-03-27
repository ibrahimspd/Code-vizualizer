package entites;

import java.util.List;

public class Modules
{
    private String name;
    private List<Classes> classes;
    private List<ClassPair> pairs;

    public Modules(String name, List<Classes> classes, List<ClassPair> pairs)
    {
        this.name = name;
        this.classes = classes;
        this.pairs = pairs;
    }

    public String getName()
    {
        return name;
    }

    public List<Classes> getClasses()
    {
        return classes;
    }

    public List<ClassPair> getPairs()
    {
        return pairs;
    }
}
