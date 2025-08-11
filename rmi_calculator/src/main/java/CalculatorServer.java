import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class CalculatorServer {
    public void run() {
        try {
            // Create the registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Create the calculator implementation
            CalculatorImplementation calculator = new CalculatorImplementation();
            
            // Bind the calculator to the registry
            registry.bind("Calculator", calculator);
            
            System.out.println("Calculator Server is ready.");
        } catch (Exception e) {
            System.err.println("Calculator Server exception: " + e);
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new CalculatorServer().run();
    }
}