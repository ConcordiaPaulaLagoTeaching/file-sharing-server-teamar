#!/bin/bash

# Kill any existing server
pkill -f "ca.concordia.Main" 2>/dev/null
sleep 1

# Start fresh server
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2/FileServer"
rm -f filesystem.dat
java -cp out ca.concordia.Main > /tmp/server_output.log 2>&1 &
SERVER_PID=$!
echo "Server started with PID: $SERVER_PID"
sleep 3

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║          FILE SERVER COMPREHENSIVE TEST SUITE              ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Test 1: Basic Operations
echo "┌─ TEST 1: Basic CREATE, WRITE, READ, DELETE ─────────────┐"
(
cat << 'EOF'
LIST
CREATE file1.txt
WRITE file1.txt Hello World from COEN 346
READ file1.txt
DELETE file1.txt
LIST
QUIT
EOF
) | nc localhost 12345
echo "└──────────────────────────────────────────────────────────┘"
echo ""
sleep 2

# Test 2: Multiple Files
echo "┌─ TEST 2: Multiple Files Management ──────────────────────┐"
java -cp out ca.concordia.Main > /tmp/server_output.log 2>&1 &
SERVER_PID=$!
sleep 2
(
cat << 'EOF'
CREATE file1.txt
CREATE file2.txt
CREATE file3.txt
LIST
WRITE file1.txt Content A
WRITE file2.txt Content B
WRITE file3.txt Content C
READ file1.txt
READ file2.txt
READ file3.txt
LIST
QUIT
EOF
) | nc localhost 12345
echo "└──────────────────────────────────────────────────────────┘"
echo ""
sleep 2

# Test 3: Error Cases
echo "┌─ TEST 3: Error Handling ─────────────────────────────────┐"
java -cp out ca.concordia.Main > /tmp/server_output.log 2>&1 &
SERVER_PID=$!
sleep 2
(
cat << 'EOF'
CREATE test.txt
CREATE test.txt
READ nonexistent.txt
DELETE nonexistent.txt
WRITE nonexistent.txt some content
CREATE verylongname.txt
QUIT
EOF
) | nc localhost 12345
echo "└──────────────────────────────────────────────────────────┘"
echo ""
sleep 2

# Test 4: Maximum Files
echo "┌─ TEST 4: Maximum Files (5 files max) ────────────────────┐"
java -cp out ca.concordia.Main > /tmp/server_output.log 2>&1 &
SERVER_PID=$!
sleep 2
(
cat << 'EOF'
CREATE f1.txt
CREATE f2.txt
CREATE f3.txt
CREATE f4.txt
CREATE f5.txt
LIST
CREATE f6.txt
QUIT
EOF
) | nc localhost 12345
echo "└──────────────────────────────────────────────────────────┘"
echo ""
sleep 2

# Test 5: Long Content (Multi-block)
echo "┌─ TEST 5: Multi-block File (>128 bytes) ─────────────────┐"
java -cp out ca.concordia.Main > /tmp/server_output.log 2>&1 &
SERVER_PID=$!
sleep 2
(
cat << 'EOF'
CREATE large.txt
WRITE large.txt This is a long text content that should span multiple blocks since each block is 128 bytes. Adding more text to ensure we cross the block boundary and test multi-block file handling properly.
READ large.txt
QUIT
EOF
) | nc localhost 12345
echo "└──────────────────────────────────────────────────────────┘"
echo ""
sleep 2

# Test 6: Overwrite File
echo "┌─ TEST 6: Overwrite Existing File ───────────────────────┐"
java -cp out ca.concordia.Main > /tmp/server_output.log 2>&1 &
SERVER_PID=$!
sleep 2
(
cat << 'EOF'
CREATE file.txt
WRITE file.txt Original content
READ file.txt
WRITE file.txt New content
READ file.txt
QUIT
EOF
) | nc localhost 12345
echo "└──────────────────────────────────────────────────────────┘"
echo ""
sleep 2

# Test 7: Delete and Recreate
echo "┌─ TEST 7: Delete and Recreate File ───────────────────────┐"
java -cp out ca.concordia.Main > /tmp/server_output.log 2>&1 &
SERVER_PID=$!
sleep 2
(
cat << 'EOF'
CREATE temp.txt
WRITE temp.txt First version
READ temp.txt
DELETE temp.txt
CREATE temp.txt
WRITE temp.txt Second version
READ temp.txt
QUIT
EOF
) | nc localhost 12345
echo "└──────────────────────────────────────────────────────────┘"
echo ""

# Cleanup
pkill -f "ca.concordia.Main" 2>/dev/null

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║              ALL TESTS COMPLETED                           ║"
echo "╚════════════════════════════════════════════════════════════╝"

