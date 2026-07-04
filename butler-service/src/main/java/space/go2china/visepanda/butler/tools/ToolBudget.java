package space.go2china.visepanda.butler.tools;

public class ToolBudget {
    private final int maxCalls;
    private int calls;

    public ToolBudget(int maxCalls) {
        this.maxCalls = maxCalls;
    }

    public boolean tryUse() {
        if (calls >= maxCalls) return false;
        calls += 1;
        return true;
    }

    public int calls() {
        return calls;
    }

    public int maxCalls() {
        return maxCalls;
    }
}
