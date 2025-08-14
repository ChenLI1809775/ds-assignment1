import org.junit.jupiter.api.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {

    private static CalculatorServer server;

    @BeforeAll
    static void setUpServer() throws Exception {
        // Start the server
        new Thread(() -> {
            server = new CalculatorServer();
            server.run();
        }).start();

        // Wait for the server to start
        Thread.sleep(2000);


    }

    @Test
    @DisplayName("Test pushValue method for single client")
    void testPushValueSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        // Get calculator remote object
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        calculator.setClientId("Client_testPushValueSingleClient");
        assertTrue(calculator.isEmpty());

        calculator.pushValue(10);
        assertFalse(calculator.isEmpty());

        calculator.pushValue(20);
        calculator.pushValue(30);

        assertEquals(30, calculator.pop());
        assertEquals(20, calculator.pop());
        assertEquals(10, calculator.pop());
        assertTrue(calculator.isEmpty());
    }

    @Test
    @DisplayName("Test pushValue method for multiple clients")
    void testPushValueMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        // Use thread pool to simulate multiple clients connecting concurrently
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        // Store results from each client
        ConcurrentLinkedQueue<Integer> results = new ConcurrentLinkedQueue<>();

        // Create 4 client tasks
        for (int i = 1; i <= 4; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    // Each client gets its own remote object reference
                    Calculator client = (Calculator) registry.lookup("Calculator");
                    client.setClientId("Client " + clientId);
                    // Each client pushes its own value
                    client.pushValue(clientId);
                    // Each client tries to pop a value, should get its own pushed value
                    int value = client.pop();
                    results.add(value);
                    Thread.sleep((long) (Math.random() * 2000));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all tasks to complete
        latch.await();
        executor.shutdown();

        // Verify that each client got its own pushed value
        assertEquals(4, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(2));
        assertTrue(results.contains(3));
        assertTrue(results.contains(4));
    }

    @Test
    @DisplayName("Test pushOperation method for single client")
    void testPushOperationSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        calculator.setClientId("Client_testPushOperationSingleClient");
        // Test max operation
        calculator.pushValue(10);
        calculator.pushValue(20);
        calculator.pushValue(15);
        calculator.pushOperation("max");

        assertEquals(20, calculator.pop());
        assertTrue(calculator.isEmpty());

        // Test min operation
        calculator.pushValue(10);
        calculator.pushValue(20);
        calculator.pushValue(15);
        calculator.pushOperation("min");

        assertEquals(10, calculator.pop());
        assertTrue(calculator.isEmpty());

        // Test lcm operation
        calculator.pushValue(12);
        calculator.pushValue(18);
        calculator.pushOperation("lcm");

        assertEquals(36, calculator.pop()); // LCM of 12 and 18 is 36
        assertTrue(calculator.isEmpty());

        // Test gcd operation
        calculator.pushValue(12);
        calculator.pushValue(18);
        calculator.pushOperation("gcd");

        assertEquals(6, calculator.pop()); // GCD of 12 and 18 is 6
        assertTrue(calculator.isEmpty());

        // Test lcm operation with multiple values
        calculator.pushValue(4);
        calculator.pushValue(6);
        calculator.pushValue(8);
        calculator.pushOperation("lcm");

        assertEquals(24, calculator.pop()); // LCM of 4, 6, and 8 is 24
        assertTrue(calculator.isEmpty());

        // Test gcd operation with multiple values
        calculator.pushValue(12);
        calculator.pushValue(18);
        calculator.pushValue(24);
        calculator.pushOperation("gcd");

        assertEquals(6, calculator.pop()); // GCD of 12, 18, and 24 is 6
        assertTrue(calculator.isEmpty());
    }


    @Test
    @DisplayName("Test pushOperation method for multiple clients")
    void testPushOperationMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        // Use thread pool to simulate multiple clients connecting concurrently
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        // Store results from each client operation
        ConcurrentLinkedQueue<Integer> results = new ConcurrentLinkedQueue<>();

        // Define operations and values for each client
        String[] operations = {"min", "max", "lcm", "gcd"};
        int[][] testValues = {
                {10, 20, 30},  // For min operation: expected result 10
                {10, 20, 30},  // For max operation: expected result 30
                {4, 6, 8},     // For lcm operation: expected result 24
                {12, 18, 24}   // For gcd operation: expected result 6
        };
        int[] expectedResults = {10, 30, 24, 6};

        // Create 4 client tasks, each testing a different operation
        for (int i = 0; i < 4; i++) {
            final int clientIndex = i;
            final int clientId = i + 1;
            executor.submit(() -> {
                try {
                    // Each client gets its own remote object reference
                    Calculator client = (Calculator) registry.lookup("Calculator");
                    // Set unique client ID to distinguish clients in server-side
                    client.setClientId("PushOpClient-" + clientId);

                    // Push values for this client
                    for (int value : testValues[clientIndex]) {
                        client.pushValue(value);
                    }

                    // Execute operation
                    client.pushOperation(operations[clientIndex]);

                    // Get result
                    int value = client.pop();
                    results.add(value);
                    Thread.sleep((long) (Math.random() * 2000));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all tasks to complete
        latch.await();
        executor.shutdown();

        // Verify that each client got the expected result
        assertEquals(4, results.size());
        assertTrue(results.contains(10));  // min result
        assertTrue(results.contains(30));  // max result
        assertTrue(results.contains(24));  // lcm result
        assertTrue(results.contains(6));   // gcd result
    }


    @Test
    @DisplayName("Test pop method for single client")
    void testPopSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        calculator.setClientId("Client_testPopSingleClient");
        calculator.pushValue(10);
        calculator.pushValue(20);

        assertEquals(20, calculator.pop());
        assertEquals(10, calculator.pop());
        assertTrue(calculator.isEmpty());
    }

    @Test
    @DisplayName("Test pop method for multiple clients")
    void testPopMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        // Create individual threads for each client to ensure no thread reuse
        CountDownLatch latch = new CountDownLatch(4);

        // Store client operation results
        AtomicInteger client1Result = new AtomicInteger(-1);
        AtomicInteger client2Result = new AtomicInteger(-1);
        AtomicInteger client3Result = new AtomicInteger(-1);
        AtomicInteger client4Result = new AtomicInteger(-1);
        AtomicBoolean client1Empty = new AtomicBoolean(false);
        AtomicBoolean client2Empty = new AtomicBoolean(false);
        AtomicBoolean client3Empty = new AtomicBoolean(false);
        AtomicBoolean client4Empty = new AtomicBoolean(false);

        // Client 1 pushes values
        Thread client1Thread = new Thread(() -> {
            try {
                Calculator client1 = (Calculator) registry.lookup("Calculator");
                client1.setClientId("Client-1");
                client1.pushValue(5);
                client1.pushValue(10);
                client1.pushValue(15);
                client1Result.set(client1.pop()); // Should get 15
                client1Empty.set(client1.isEmpty());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Client 2 operates concurrently
        Thread client2Thread = new Thread(() -> {
            try {
                Calculator client2 = (Calculator) registry.lookup("Calculator");
                client2.setClientId("Client-2");
                client2.pushValue(10);
                // Should get 10
                client2Result.set(client2.pop());
                client2Empty.set(client2.isEmpty());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Client 3 operates concurrently
        Thread client3Thread = new Thread(() -> {
            try {
                Calculator client3 = (Calculator) registry.lookup("Calculator");
                client3.setClientId("Client-3");
                client3.pushValue(1);
                client3.pushValue(2);
                client3.pushValue(5);
                // Should get 5
                client3Result.set(client3.pop());
                client3Empty.set(client3.isEmpty());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Client 4 pushes a new value
        Thread client4Thread = new Thread(() -> {
            try {
                Calculator client4 = (Calculator) registry.lookup("Calculator");
                client4.setClientId("Client-4");
                client4.pushValue(20);
                // Should get 20
                client4Result.set(client4.pop());
                client4Empty.set(client4.isEmpty());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Start all client threads
        client1Thread.start();
        client2Thread.start();
        client3Thread.start();
        client4Thread.start();

        // Wait for all threads to complete
        latch.await();

        // Verify results
        assertEquals(15, client1Result.get());
        assertEquals(10, client2Result.get());
        assertEquals(5, client3Result.get());
        assertEquals(20, client4Result.get());
        assertFalse(client1Empty.get());
        assertTrue(client2Empty.get());
        assertFalse(client3Empty.get());
        assertTrue(client4Empty.get());
    }

    @Test
    @DisplayName("Test delayPop method for single client")
    void testDelayPopSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        calculator.setClientId("Client_testDelayPopSingleClient");
        calculator.pushValue(42);

        long startTime = System.currentTimeMillis();
        int result = calculator.delayPop(1000); // 1 second delay
        long endTime = System.currentTimeMillis();

        assertEquals(42, result);
        assertTrue(endTime - startTime >= 1000, "Delay should be at least 1000ms");
    }

    @Test
    @DisplayName("Test delayPop method for multiple clients")
    void testDelayPopMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        // Use thread pool to simulate multiple clients connecting concurrently
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        // Store results
        AtomicInteger result1 = new AtomicInteger(-1);
        AtomicInteger result2 = new AtomicInteger(-1);
        AtomicInteger result3 = new AtomicInteger(-1);
        AtomicLong time1 = new AtomicLong(0);
        AtomicLong time2 = new AtomicLong(0);
        AtomicLong time3 = new AtomicLong(0);

        // Client 1 pushes value and delays pop
        executor.submit(() -> {
            try {
                Calculator client1 = (Calculator) registry.lookup("Calculator");
                client1.setClientId("Client-1");
                client1.pushValue(99);
                long startTime = System.currentTimeMillis();
                result1.set(client1.delayPop(500)); // 0.5 second delay
                time1.set(System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Client 2 operates concurrently
        executor.submit(() -> {
            try {
                Calculator client2 = (Calculator) registry.lookup("Calculator");
                client2.setClientId("Client-2");
                client2.pushValue(77);
                long startTime = System.currentTimeMillis();
                result2.set(client2.delayPop(300)); // 0.3 second delay
                time2.set(System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Client 3 operates concurrently
        executor.submit(() -> {
            try {
                Calculator client3 = (Calculator) registry.lookup("Calculator");
                client3.setClientId("Client-3");
                client3.pushValue(55);
                long startTime = System.currentTimeMillis();
                result3.set(client3.delayPop(700)); // 0.7 second delay
                time3.set(System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Wait for all tasks to complete
        latch.await();
        executor.shutdown();

        // Verify results
        assertEquals(99, result1.get());
        assertEquals(77, result2.get());
        assertEquals(55, result3.get());
        assertTrue(time1.get() >= 500, "Delay should be at least 500ms");
        assertTrue(time2.get() >= 300, "Delay should be at least 300ms");
        assertTrue(time3.get() >= 700, "Delay should be at least 700ms");
    }

}