import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculatorClient {

    public void run() {
        try {
            // Get the registry
            Registry registry = LocateRegistry.getRegistry("localhost");

            // Look up the calculator
            Calculator calculator = (Calculator) registry.lookup("Calculator");

            System.out.println("Testing Calculator RMI application...");

            String clientID = "Client_test";
            // Test pushValue and basic operations
            calculator.pushValue(10,clientID);
            calculator.pushValue(20,clientID);
            calculator.pushValue(30,clientID);

            System.out.println("Pushed values 10, 20, 30");

            // Test max operation
            calculator.pushOperation("max",clientID);
            System.out.println("After max operation");

            // Test push more values
            calculator.pushValue(5,clientID);
            calculator.pushValue(15,clientID);
            System.out.println("Pushed values 5, 15");

            // Test min operation
            calculator.pushOperation("min",clientID);
            System.out.println("After min operation");

            // Test gcd operation
            calculator.pushValue(24,clientID);
            calculator.pushValue(36,clientID);
            calculator.pushOperation("gcd",clientID);
            System.out.println("After pushing 24, 36 and gcd operation");

            // Test lcm operation
            calculator.pushValue(4,clientID);
            calculator.pushValue(6,clientID);
            calculator.pushOperation("lcm",clientID);
            System.out.println("After pushing 4, 6 and lcm operation");

            // Test delayPop
            System.out.println("Testing delayPop with 2000 milliseconds");
            int result = calculator.delayPop(2000,clientID);
            System.out.println("delayPop result: " + result);

            // Check if stack is empty
            System.out.println("Is stack empty? " + calculator.isEmpty(clientID));

            // Pop final value
            if (!calculator.isEmpty(clientID)) {
                int finalResult = calculator.pop(clientID);
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