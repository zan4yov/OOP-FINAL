#!/bin/bash
# Script untuk compile project
echo "Compiling project..."
mkdir -p bin
javac -d bin -sourcepath src src/com/tictactoe/server/*.java src/com/tictactoe/client/*.java
echo "Compilation complete! Classes are in bin/"
