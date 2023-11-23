import java.util.Stack;

public abstract class SchemeFunction {
    private String name;

    public SchemeFunction(String name) {
        this.name = name;
    }

    abstract int callFunction(Stack<Integer> integers) throws Exception;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
