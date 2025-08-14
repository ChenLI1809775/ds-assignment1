import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorClient {

    public void run() {
        try {
            // Get the registry
            Registry registry = LocateRegistry.getRegistry("localhost");

            // Look up the calculator
            Calculator calculator = (Calculator) registry.lookup("Calculator");

            System.out.println("Testing Calculator RMI application...");

            // Test pushValue and basic operations
            calculator.pushValue(10);
            calculator.pushValue(20);
            calculator.pushValue(30);

            System.out.println("Pushed values 10, 20, 30");

            // Test max operation
            calculator.pushOperation("max");
            System.out.println("After max operation");

            // Test push more values
            calculator.pushValue(5);
            calculator.pushValue(15);
            System.out.println("Pushed values 5, 15");

            // Test min operation
            calculator.pushOperation("min");
            System.out.println("After min operation");

            // Test gcd operation
            calculator.pushValue(24);
            calculator.pushValue(36);
            calculator.pushOperation("gcd");
            System.out.println("After pushing 24, 36 and gcd operation");

            // Test lcm operation
            calculator.pushValue(4);
            calculator.pushValue(6);
            calculator.pushOperation("lcm");
            System.out.println("After pushing 4, 6 and lcm operation");

            // Test delayPop
            System.out.println("Testing delayPop with 2000 milliseconds");
            int result = calculator.delayPop(2000);
            System.out.println("delayPop result: " + result);

            // Check if stack is empty
            System.out.println("Is stack empty? " + calculator.isEmpty());

            // Pop final value
            if (!calculator.isEmpty()) {
                int finalResult = calculator.pop();
                System.out.println("Final result: " + finalResult);
            }

            System.out.println("Client test completed.");
        } catch (Exception e) {
            System.err.println("Calculator Client exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CalculatorClient().run();
    }

}