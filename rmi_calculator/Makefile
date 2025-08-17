# Makefile for RMI Calculator Application

# Variables
SRC_DIR = src
MAIN_SRC_DIR = $(SRC_DIR)/main/java
TEST_SRC_DIR = $(SRC_DIR)/test/java
BUILD_DIR = target/classes
TEST_BUILD_DIR = target/test-classes
LIB_DIR = lib

# Java commands
JAVAC = javac
JAVA = java
# Java version flags
JAVA_VERSION = --release 17
# Classpath
MAIN_CLASSPATH = "$(BUILD_DIR):$(LIB_DIR)/*"
TEST_CLASSPATH = "$(BUILD_DIR):$(TEST_BUILD_DIR):$(LIB_DIR)/*"

# Find JUnit standalone jar
JUNIT_JAR = $(wildcard lib/junit-platform-console-standalone-*.jar)

# Default target
.PHONY: all clean compile run server client test

all: compile

# Create necessary directories
$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

$(TEST_BUILD_DIR):
	mkdir -p $(TEST_BUILD_DIR)

# Compile main Java files
compile: $(BUILD_DIR)
	$(JAVAC) -d $(BUILD_DIR) $(MAIN_SRC_DIR)/*.java
	@echo "Main classes compiled successfully."

# Compile test Java files
compile-test: $(TEST_BUILD_DIR) compile
	$(JAVAC) -cp $(MAIN_CLASSPATH) -d $(TEST_BUILD_DIR) $(TEST_SRC_DIR)/*.java
	@echo "Test classes compiled successfully."

# Run unit tests
test: compile-test
	@if [ -z "$(JUNIT_JAR)" ]; then \
		echo "Error: JUnit standalone jar not found in lib directory"; \
		exit 1; \
	fi
	$(JAVA) -jar $(JUNIT_JAR) \
		--class-path $(TEST_CLASSPATH) \
		--scan-classpath

# Clean build directories
clean:
	rm -rf target/
	@echo "Cleaned build directories."

# Help message
help:
	@echo "Available targets:"
	@echo "  all          - Compile main source files (default)"
	@echo "  compile      - Compile main source files"
	@echo "  test         - Compile and run all unit tests"
	@echo "  clean        - Remove all compiled classes"
	@echo "  help         - Show this help message"
