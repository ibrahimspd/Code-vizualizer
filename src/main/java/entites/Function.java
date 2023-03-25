package entites;

import java.util.Map;

public class Function
{

    private String id;


    public Function(Functionbuilder functionbuilder)
    {
        this.id = functionbuilder.id;

    }


    public String getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "Function{" +
                "id='" + id + '\'' +
                '}';
    }

    public static class Functionbuilder
    {
        private String id;


        public Functionbuilder id(String id){
            this.id = id;
            return this;
        }

        public Function build(){
            return new Function(this);
        }
    }
}
