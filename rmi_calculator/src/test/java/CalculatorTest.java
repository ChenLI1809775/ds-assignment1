import org.junit.jupiter.api.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CalculatorTest {

    private static CalculatorServer server;

    @BeforeAll
    static void setUpServer() throws Exception {
        // Start the server
        new Thread(() -> {
            server = new CalculatorServer();
            server.run(1099);
        }).start();

        // Wait for the server to start
        Thread.sleep(2000);
    }

    @Test
    @DisplayName("Test pushValue method for single client")
    @Order(1)
    void testPushValueSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        // Get calculator remote object
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        String clientId = "Client_testPushValueSingleClient";
        assertTrue(calculator.isEmpty(clientId));

        calculator.pushValue(10, clientId);
        assertFalse(calculator.isEmpty(clientId));

        calculator.pushValue(20, clientId);
        calculator.pushValue(30, clientId);

        assertEquals(30, calculator.pop(clientId));
        assertEquals(20, calculator.pop(clientId));
        assertEquals(10, calculator.pop(clientId));
        assertTrue(calculator.isEmpty(clientId));
    }

    @Test
    @DisplayName("Test pushValue method for multiple clients")
    @Order(2)
    void testPushValueMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
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
                    String clientIdentifier = "testPushValueMultipleClients " + clientId;
                    // Each client pushes its own value
                    client.pushValue(clientId, clientIdentifier);
                    // Each client tries to pop a value, should get its own pushed value
                    int value = client.pop(clientIdentifier);
                    results.add(value);
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
    @Order(3)
    void testPushOperationSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        String clientId = "Client_testPushOperationSingleClient";
        // Test max operation
        calculator.pushValue(10, clientId);
        calculator.pushValue(20, clientId);
        calculator.pushValue(15, clientId);
        calculator.pushOperation("max", clientId);
        assertEquals(20, calculator.pop(clientId));
        assertTrue(calculator.isEmpty(clientId));

        // Test min operation
        calculator.pushValue(10, clientId);
        calculator.pushValue(20, clientId);
        calculator.pushValue(15, clientId);
        calculator.pushOperation("min", clientId);

        assertEquals(10, calculator.pop(clientId));
        assertTrue(calculator.isEmpty(clientId));

        // Test lcm operation
        calculator.pushValue(12, clientId);
        calculator.pushValue(18, clientId);
        calculator.pushOperation("lcm", clientId);

        assertEquals(36, calculator.pop(clientId)); // LCM of 12 and 18 is 36
        assertTrue(calculator.isEmpty(clientId));

        // Test gcd operation
        calculator.pushValue(12, clientId);
        calculator.pushValue(18, clientId);
        calculator.pushOperation("gcd", clientId);

        assertEquals(6, calculator.pop(clientId)); // GCD of 12 and 18 is 6
        assertTrue(calculator.isEmpty(clientId));

        // Test lcm operation with multiple values
        calculator.pushValue(4, clientId);
        calculator.pushValue(6, clientId);
        calculator.pushValue(8, clientId);
        calculator.pushOperation("lcm", clientId);

        assertEquals(24, calculator.pop(clientId)); // LCM of 4, 6, and 8 is 24
        assertTrue(calculator.isEmpty(clientId));

        // Test gcd operation with multiple values
        calculator.pushValue(12, clientId);
        calculator.pushValue(18, clientId);
        calculator.pushValue(24, clientId);
        calculator.pushOperation("gcd", clientId);

        assertEquals(6, calculator.pop(clientId)); // GCD of 12, 18, and 24 is 6
        assertTrue(calculator.isEmpty(clientId));
    }


    @Test
    @DisplayName("Test pushOperation method for multiple clients")
    @Order(4)
    void testPushOperationMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
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

        // Create 4 client tasks, each testing a different operation
        for (int i = 0; i < 4; i++) {
            final int clientIndex = i;
            final int clientId = i + 1;
            executor.submit(() -> {
                try {
                    // Each client gets its own remote object reference
                    Calculator client = (Calculator) registry.lookup("Calculator");
                    // Set unique client ID to distinguish clients in server-side
                    String clientIdentifier = "PushOpClient-" + clientId;
                    Thread.sleep(100);
                    // Push values for this client
                    for (int value : testValues[clientIndex]) {
                        client.pushValue(value, clientIdentifier);
                    }

                    // Execute operation
                    client.pushOperation(operations[clientIndex], clientIdentifier);

                    // Get result
                    int value = client.pop(clientIdentifier);
                    results.add(value);
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
    @Order(5)
    void testPopSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        String clientId = "Client_testPopSingleClient";
        calculator.pushValue(10, clientId);
        calculator.pushValue(20, clientId);

        assertEquals(20, calculator.pop(clientId));
        assertEquals(10, calculator.pop(clientId));
        assertTrue(calculator.isEmpty(clientId));
    }

    @Test
    @DisplayName("Test pop method for multiple clients")
    @Order(6)
    void testPopMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
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
                String clientId = "testPopMultipleClients-1";
                Thread.sleep(100);
                client1.pushValue(5, clientId);
                client1.pushValue(10, clientId);
                client1.pushValue(15, clientId);
                client1Result.set(client1.pop(clientId)); // Should get 15
                client1Empty.set(client1.isEmpty(clientId));
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
                String clientId = "testPopMultipleClients-2";
                Thread.sleep(100);
                client2.pushValue(10, clientId);
                // Should get 10
                client2Result.set(client2.pop(clientId));
                client2Empty.set(client2.isEmpty(clientId));
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
                String clientId = "testPopMultipleClients-3";
                Thread.sleep(100);
                client3.pushValue(1, clientId);
                client3.pushValue(2, clientId);
                client3.pushValue(5, clientId);
                // Should get 5
                client3Result.set(client3.pop(clientId));
                client3Empty.set(client3.isEmpty(clientId));
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
                String clientId = "testPopMultipleClients-4";
                Thread.sleep(100);
                client4.pushValue(20, clientId);
                // Should get 20
                client4Result.set(client4.pop(clientId));
                client4Empty.set(client4.isEmpty(clientId));
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
    @Order(7)
    void testDelayPopSingleClient() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        Calculator calculator = (Calculator) registry.lookup("Calculator");
        String clientId = "Client_testDelayPopSingleClient";
        Thread.sleep(100);
        calculator.pushValue(42, clientId);

        long startTime = System.currentTimeMillis();
        int result = calculator.delayPop(1000, clientId); // 1 second delay
        long endTime = System.currentTimeMillis();

        assertEquals(42, result);
        assertTrue(endTime - startTime >= 1000, "Delay should be at least 1000ms");
    }

    @Test
    @DisplayName("Test delayPop method for multiple clients")
    @Order(8)
    void testDelayPopMultipleClients() throws Exception {
        // Get registry
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        // Use thread pool to simulate multiple clients connecting concurrently
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        // Store results
        AtomicInteger result1 = new AtomicInteger(-1);
        AtomicInteger result2 = new AtomicInteger(-1);
        AtomicInteger result3 = new AtomicInteger(-1);
        AtomicInteger result4 = new AtomicInteger(-1);
        AtomicLong time1 = new AtomicLong(0);
        AtomicLong time2 = new AtomicLong(0);
        AtomicLong time3 = new AtomicLong(0);
        AtomicLong time4 = new AtomicLong(0);

        // Client 1 pushes value and delays pop
        executor.submit(() -> {
            try {
                Calculator client1 = (Calculator) registry.lookup("Calculator");
                String clientId = "testDelayPopMultipleClients-1";
                Thread.sleep(100);
                client1.pushValue(99, clientId);
                long startTime = System.currentTimeMillis();
                result1.set(client1.delayPop(500, clientId)); // 0.5 second delay
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
                String clientId = "testDelayPopMultipleClients-2";
                Thread.sleep(100);
                client2.pushValue(77, clientId);
                long startTime = System.currentTimeMillis();
                result2.set(client2.delayPop(300, clientId)); // 0.3 second delay
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
                String clientId = "testDelayPopMultipleClients-3";
                Thread.sleep(100);
                client3.pushValue(55, clientId);
                long startTime = System.currentTimeMillis();
                result3.set(client3.delayPop(700, clientId)); // 0.7 second delay
                time3.set(System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // Client 4 operates concurrently
        executor.submit(() -> {
            try {
                Calculator client4 = (Calculator) registry.lookup("Calculator");
                String clientId = "testDelayPopMultipleClients-4";
                Thread.sleep(100);
                client4.pushValue(33, clientId);
                long startTime = System.currentTimeMillis();
                result4.set(client4.delayPop(400, clientId)); // 0.4 second delay
                time4.set(System.currentTimeMillis() - startTime);
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
        assertEquals(33, result4.get());
        assertTrue(time1.get() >= 500, "Delay should be at least 500ms");
        assertTrue(time2.get() >= 300, "Delay should be at least 300ms");
        assertTrue(time3.get() >= 700, "Delay should be at least 700ms");
        assertTrue(time4.get() >= 400, "Delay should be at least 400ms");
    }

}