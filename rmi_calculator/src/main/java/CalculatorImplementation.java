import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.server.RemoteServer;

public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {
    // Stack to store values
    private static final Map<String, Stack<Integer>> clientStacks = new ConcurrentHashMap<>();
    //Using ThreadLocal to store the client ID
    private static final ThreadLocal<String> clientIdHolder = new ThreadLocal<>();

    public CalculatorImplementation() throws RemoteException {
        super();
    }

    public static void setClientId(String clientId) {
        clientIdHolder.set(clientId);
    }

    /**
     * Clear the client ID
     */
    public static void clearClientId() {
        clientIdHolder.remove();
    }
    /**
     * Get the current stack for the client
     *
     * @return the current stack for the client
     */
    private Stack<Integer> getCurrentStack() {
        String clientId = clientIdHolder.get();
        if (clientId == null) {
            throw new IllegalStateException("Client ID not set");
        }
        return clientStacks.computeIfAbsent(clientId, k -> new Stack<>());
    }

    /**
     * Push a value to the stack
     *
     * @param val the value to push
     * @throws RemoteException throws if the client is not registered
     */
    @Override
    public void pushValue(int val) throws RemoteException {
        Stack<Integer> stack = getCurrentStack();
        stack.push(val);
    }

    /**
     * Push an operation to the stack
     *
     * @param operator the operation to push
     * @throws RemoteException throws if the client is not registered
     */
    @Override
    public void pushOperation(String operator) throws RemoteException, ServerNotActiveException {
        Stack<Integer> stack = getCurrentStack();
        if (stack.size() < 2) {
            throw new RemoteException("Not enough operands in stack for operation");
        }
        List<Integer> values = new ArrayList<>();

        // Pop all values from the stack
        while (!stack.isEmpty() && stack.peek() != null) {
            values.add(stack.pop());
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

    /**
     * Pop a value from the stack
     *
     * @return the popped value
     * @throws RemoteException throws if the client is not registered
     */
    @Override
    public int pop() throws RemoteException, ServerNotActiveException {
        Stack<Integer> stack = getCurrentStack();
        if (stack.isEmpty()) {
            throw new RemoteException("Stack is empty");
        }
        return stack.pop();
    }

    /**
     * Check if the stack is empty
     *
     * @return true if the stack is empty, false otherwise
     * @throws RemoteException throws if the client is not registered
     */
    @Override
    public boolean isEmpty() throws RemoteException {
        Stack<Integer> stack = getCurrentStack();
        return stack.isEmpty();
    }

    /**
     * Pop a value from the stack with a delay
     *
     * @param millis the delay in milliseconds
     * @return the popped value
     * @throws RemoteException throws if the client is not registered
     */
    @Override
    public int delayPop(int millis) throws RemoteException, ServerNotActiveException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Delay interrupted", e);
        }
        return pop();
    }

    /**
     * Helper method to calculate LCM using GCD
     *
     * @param a the first number
     * @param b the second number
     * @return the LCM of a and b
     */
    private int lcm(int a, int b) {
        return Math.abs(a * b) / gcd(a, b);
    }

    /**
     * Helper method to calculate GCD
     *
     * @param a the first number
     * @param b the second number
     * @return the GCD of a and b
     */
    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }
}