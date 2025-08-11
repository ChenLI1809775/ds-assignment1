import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {
    
    private final Stack<Object> stack;
    
    public CalculatorImplementation() throws RemoteException {
        super();
        stack = new Stack<>();
    }
    
    @Override
    public void pushValue(int val) throws RemoteException {
        stack.push(val);
    }
    
    @Override
    public void pushOperation(String operator) throws RemoteException {
        List<Integer> values = new ArrayList<>();
        
        // Pop all values from the stack
        while (!stack.isEmpty() && stack.peek() instanceof Integer) {
            values.add((Integer) stack.pop());
        }
        
        if (values.isEmpty()) {
            return;
        }
        
        int result = values.get(0);

        switch (operator) {
            case "min" -> {
                for (int i = 1; i < values.size(); i++) {
                    result = Math.min(result, values.get(i));
                }
            }
            case "max" -> {
                for (int i = 1; i < values.size(); i++) {
                    result = Math.max(result, values.get(i));
                }
            }
            case "lcm" -> {
                for (int i = 1; i < values.size(); i++) {
                    result = lcm(result, values.get(i));
                }
            }
            case "gcd" -> {
                for (int i = 1; i < values.size(); i++) {
                    result = gcd(result, values.get(i));
                }
            }
        }
        
        stack.push(result);
    }
    
    @Override
    public int pop() throws RemoteException {
        if (!stack.isEmpty()) {
            return (int) stack.pop();
        }
        throw new RuntimeException("Stack is empty");
    }
    
    @Override
    public boolean isEmpty() throws RemoteException {
        return stack.isEmpty();
    }
    
    @Override
    public int delayPop(int millis) throws RemoteException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return pop();
    }
    
    // Helper method to calculate LCM
    private int lcm(int a, int b) {
        return Math.abs(a * b) / gcd(a, b);
    }
    
    // Helper method to calculate GCD using Euclidean algorithm
    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }
}