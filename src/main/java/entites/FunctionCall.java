package entites;

import java.util.List;
import java.util.Map;

public class FunctionCall
{
    private Map<Object, Object> parameters;
    private String name;
    private String recursive;
    private int inComing;
    private int outGoing;
    private int numberOfCalls;
    private String returnType;
    private List<String> parents;
    private List<String> children;

    // make a builder for this class
    public FunctionCall(FunctionCallBuilder functionCallBuilder)
    {
        this.parameters = functionCallBuilder.parameters;
        this.recursive = functionCallBuilder.recursive;
        this.inComing = functionCallBuilder.inComing;
        this.outGoing = functionCallBuilder.outGoing;
        this.numberOfCalls = functionCallBuilder.numberOfCalls;
        this.returnType = functionCallBuilder.returnType;
        this.parents = functionCallBuilder.parents;
        this.children = functionCallBuilder.children;
        this.name = functionCallBuilder.name;
    }


    @Override
    public String toString()
    {
        return "FunctionCall{" +
                "parameters=" + parameters +
                ", name='" + name + '\'' +
                ", recursive='" + recursive + '\'' +
                ", inComing='" + inComing + '\'' +
                ", outGoing='" + outGoing + '\'' +
                ", numberOfCalls='" + numberOfCalls + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parents=" + parents +
                ", children=" + children +
                '}';
    }

    public Map<Object, Object> getParameters()
    {
        return parameters;
    }

    public String getRecursive()
    {
        return recursive;
    }

    public int getInComing()
    {
        return inComing;
    }

    public int getOutGoing()
    {
        return outGoing;
    }

    public int getNumberOfCalls()
    {
        return numberOfCalls;
    }

    public String getName()
    {
        return name;
    }

    public String getReturnType()
    {
        return returnType;
    }

    public List<String> getParents()
    {
        return parents;
    }

    public List<String> getChildren()
    {
        return children;
    }
    public void addParent(String parent)
    {
        this.parents.add(parent);
    }
    public void addChild(String child)
    {
        this.children.add(child);
    }
    public void incrementInComing()
    {
        this.inComing++;
    }
    public void incrementOutGoing()
    {
        this.outGoing++;
    }
    public void incrementNumberOfCalls()
    {
        this.numberOfCalls++;
    }
    public void addRecursive(String recursive)
    {
        this.recursive = recursive;
    }

    // make the class FunctionCallBuilder
    public static class FunctionCallBuilder
    {
        private Map<Object, Object> parameters;
        private String recursive;
        private int inComing;
        private int outGoing;
        private int numberOfCalls;
        private String returnType;
        private List<String> parents;
        private List<String> children;
        private String name;

        public FunctionCallBuilder()
        {
        }

        public FunctionCallBuilder setParameters(Map<Object, Object> parameters)
        {
            this.parameters = parameters;
            return this;
        }

        public FunctionCallBuilder setRecursive(String recursive)
        {
            this.recursive = recursive;
            return this;
        }
        public FunctionCallBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public FunctionCallBuilder setInComing(int inComing)
        {
            this.inComing = inComing;
            return this;
        }

        public FunctionCallBuilder setOutGoing(int outGoing)
        {
            this.outGoing = outGoing;
            return this;
        }

        public FunctionCallBuilder setNumberOfCalls(int numberOfCalls)
        {
            this.numberOfCalls = numberOfCalls;
            return this;
        }

        public FunctionCallBuilder setReturnType(String returnType)
        {
            this.returnType = returnType;
            return this;
        }

        public FunctionCallBuilder setParents(List<String> parents)
        {
            this.parents = parents;
            return this;
        }

        public FunctionCallBuilder setChildren(List<String> children)
        {
            this.children = children;
            return this;
        }

        public FunctionCall build()
        {
            return new FunctionCall(this);
        }
    }
}
