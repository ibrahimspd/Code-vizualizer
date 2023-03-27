package entites;

public class FunctionPair
{
    private String fromFunction;
    private String toFunction;
    private int calls = 0;

    public FunctionPair(String fromFunction, String toFunction)
    {
        this.fromFunction = fromFunction;
        this.toFunction = toFunction;
    }

    public String getFromFunction()
    {
        return fromFunction;
    }

    public String getToFunction()
    {
        return toFunction;
    }

    public void incrementCalls()
    {
        calls += calls;
    }
}
