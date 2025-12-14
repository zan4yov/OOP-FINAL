# Makefile untuk OOP-Final

.PHONY: all clean compile server client

all: compile

clean:
	rm -rf bin
	@echo "Cleaned bin directory."

compile:
	@echo "Compiling..."
	@mkdir -p bin
	@javac -d bin -sourcepath src src/com/tictactoe/server/*.java src/com/tictactoe/client/*.java
	@echo "Done."

server: compile
	@echo "Running Server..."
	@java -cp bin com.tictactoe.server.ServerMain

client: compile
	@echo "Running Client..."
	@java -cp bin com.tictactoe.client.ClientMain
