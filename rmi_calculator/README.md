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
1. Push values 10, 20, 30 to the stack
2. Perform max operation (result: 30)
3. Push values 5, 15 to the stack
4. Perform min operation (result: 5)
5. Push values 24, 36 and perform GCD operation (result: 12)
6. Push values 4, 6 and perform LCM operation (result: 12)
7. Test delayPop with 2000ms delay
8. Check if stack is empty

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
