# RMI Calculator Application

## Prerequisites

- Java 17 or higher, installed and added to the PATH:
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
```

## Project Structure

```
src/
├── main/
│   └── java/
│       ├── Calculator.java              # Remote interface
│       ├── CalculatorClient.java        # Client implementation
│       ├── CalculatorImplementation.java # Server implementation
│       └── CalculatorServer.java        # Server launcher
└── test/
    └── java/
        └── CalculatorTest.java          # Unit tests

```

## How to Compile, Run and Test

Note: Before running the commands below, first run `cd` to switch to the project folder that contains the `src` subdirectory.

### 1. Compile All Java Files

```bash
mkdir -p target/classes
javac -d target/classes src/main/java/*.java
```

### 2. Launch the Server

Start the server:

```bash
 java -cp "target/classes:lib/*" CalculatorServer
```

You should see the message "Calculator Server is ready." when the server starts successfully.

### 3. Run the Client and Test All Remote Operations

In a new terminal, run the client:

```bash
 java -cp "target/classes:lib/*" CalculatorClient
```

The client will perform the following operations:
```
Testing Calculator RMI application...
1. Pushed values 10, 20, 30
2. After max operation
   Max value: 30
3. Pushed values 5, 15
4. After min operation
   Min value: 5
5. After pushing 24, 36 and gcd operation
   GCD value: 12
6. After pushing 4, 6 and lcm operation
   LCM value: 12
7. Pushed value 22
8. Testing delayPop with 2000 milliseconds
   delayPop result: 22 elapsed time(ms): 2002
9. Is stack empty? true
10. Client test completed.
```

### 4. Run Unit Tests

First,  compile the test files:

```bash
mkdir -p target/classes 
mkdir -p target/test-classes
javac -cp "lib/*" \
      -d target/classes \
      $(find src/main/java -name "*.java")
javac -cp "target/classes:lib/*" \
      -d target/test-classes \
      $(find src/test/java -name "*.java")
```

Run the automated tests:

```bash
java -jar lib/junit-platform-console-standalone-*.jar \
     --class-path target/classes:target/test-classes:lib/* \
     --scan-classpath
```

The unit tests include single-client and multi-client simulation scenarios:

1. Single or multiple clients pushing values concurrently
2. Single or multiple clients performing different operations (min, max, gcd, lcm)
3. Single or multiple clients using delayPop with different delays
4. Single or multiple clients performing pop operations