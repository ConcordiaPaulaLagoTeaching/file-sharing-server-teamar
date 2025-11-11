# Complete Testing Guide - File Server Assignment

## 📋 Table of Contents
1. [What This Program Does](#what-this-program-does)
2. [Available Commands](#available-commands)
3. [Quick Start Testing](#quick-start-testing)
4. [Step-by-Step Manual Testing](#step-by-step-manual-testing)
5. [Automated Testing](#automated-testing)
6. [Understanding the Results](#understanding-the-results)

---

## 🎯 What This Program Does

This is a **File Sharing Server** that simulates a simple file system with networking capabilities.

### Architecture:
- **FileServer**: A server that listens on port 12345 and manages files
- **FileClient**: A client that connects to the server and sends commands
- **File System**: Stores files in blocks (like a real hard drive)
  - Maximum 5 files
  - 10 blocks total (128 bytes each = 1,280 bytes total storage)
  - Files are stored in `filesystem.dat` on disk

---

## 📝 Available Commands

The server understands these 6 commands:

### 1. `LIST`
**What it does:** Shows all files in the file system  
**Example:**
```
LIST
```
**Response:**
```
SUCCESS: file1.txt
file2.txt
```

---

### 2. `CREATE <filename>`
**What it does:** Creates a new empty file  
**Rules:**
- Filename max 11 characters
- Can't create duplicate files
- Max 5 files total

**Example:**
```
CREATE myfile.txt
```
**Response:**
```
SUCCESS: File 'myfile.txt' created.
```

**Errors:**
- `ERROR: filename too large` (if > 11 chars)
- `ERROR: file already exists` (if duplicate)
- `ERROR: maximum number of files reached` (if 5 files exist)

---

### 3. `WRITE <filename> <content>`
**What it does:** Writes content to an existing file (overwrites old content)  
**Rules:**
- File must already exist
- Content can span multiple words
- Uses multiple 128-byte blocks if needed

**Example:**
```
WRITE myfile.txt Hello World from COEN 346!
```
**Response:**
```
SUCCESS: File 'myfile.txt' written.
```

**Errors:**
- `ERROR: file <name> does not exist` (if file not created first)
- `ERROR: file too large` (if content needs more than available blocks)

---

### 4. `READ <filename>`
**What it does:** Reads and returns the content of a file  

**Example:**
```
READ myfile.txt
```
**Response:**
```
SUCCESS: Hello World from COEN 346!
```

**Errors:**
- `ERROR: file <name> does not exist` (if file doesn't exist)

---

### 5. `DELETE <filename>`
**What it does:** Deletes a file and frees its blocks  

**Example:**
```
DELETE myfile.txt
```
**Response:**
```
SUCCESS: File 'myfile.txt' deleted.
```

**Errors:**
- `ERROR: file <name> does not exist` (if file doesn't exist)

---

### 6. `QUIT`
**What it does:** Disconnects from the server  

**Example:**
```
QUIT
```
**Response:**
```
SUCCESS: Disconnecting.
```

---

## 🚀 Quick Start Testing

### Method 1: Automated Test Script (Easiest!)

Just run this command:
```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2"
./run_all_tests.sh
```

This runs 7 comprehensive tests automatically and shows results!

---

## 🧪 Step-by-Step Manual Testing

### STEP 1: Start the Server

Open **Terminal 1** and run:
```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2/FileServer"
java -cp out ca.concordia.Main
```

You should see:
```
Hello and welcome!
Server started. Listening on port 12345...
```

✅ **Leave this terminal running!** This is your server.

---

### STEP 2: Connect a Client

Now you have 3 options to connect:

#### Option A: Use the Java Client (Interactive)

Open **Terminal 2** and run:
```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2/FileClient"
java -cp out ca.concordia.Main
```

Now you can type commands interactively!

#### Option B: Use netcat/nc (Quick Testing)

Open **Terminal 2** and run:
```bash
echo "LIST" | nc localhost 12345
```

#### Option C: Use netcat with multiple commands

```bash
(echo "CREATE test.txt"; echo "WRITE test.txt Hello World"; echo "READ test.txt"; echo "QUIT") | nc localhost 12345
```

---

### STEP 3: Basic Test Sequence

Let's test all commands! In your client terminal, type these commands one at a time:

#### Test 1: List empty system
```
LIST
```
Expected: `SUCCESS: No files in the system`

#### Test 2: Create a file
```
CREATE myfile.txt
```
Expected: `SUCCESS: File 'myfile.txt' created.`

#### Test 3: List files
```
LIST
```
Expected: 
```
SUCCESS: myfile.txt
```

#### Test 4: Write content
```
WRITE myfile.txt Hello this is my test content
```
Expected: `SUCCESS: File 'myfile.txt' written.`

#### Test 5: Read content back
```
READ myfile.txt
```
Expected: `SUCCESS: Hello this is my test content`

#### Test 6: Create more files
```
CREATE file2.txt
CREATE file3.txt
LIST
```
Expected:
```
SUCCESS: File 'file2.txt' created.
SUCCESS: File 'file3.txt' created.
SUCCESS: myfile.txt
file2.txt
file3.txt
```

#### Test 7: Delete a file
```
DELETE myfile.txt
LIST
```
Expected:
```
SUCCESS: File 'myfile.txt' deleted.
SUCCESS: file2.txt
file3.txt
```

#### Test 8: Quit
```
QUIT
```
Expected: `SUCCESS: Disconnecting.`

---

### STEP 4: Test Error Cases

Start the server again and try these error scenarios:

#### Error Test 1: Duplicate file
```
CREATE test.txt
CREATE test.txt
```
Expected: `ERROR: file already exists`

#### Error Test 2: Filename too long
```
CREATE verylongfilename.txt
```
Expected: `ERROR: filename too large`

#### Error Test 3: Read non-existent file
```
READ nothere.txt
```
Expected: `ERROR: file nothere.txt does not exist`

#### Error Test 4: Write to non-existent file
```
WRITE nothere.txt some content
```
Expected: `ERROR: file nothere.txt does not exist`

#### Error Test 5: Delete non-existent file
```
DELETE nothere.txt
```
Expected: `ERROR: file nothere.txt does not exist`

#### Error Test 6: Maximum files (5 max)
```
CREATE f1.txt
CREATE f2.txt
CREATE f3.txt
CREATE f4.txt
CREATE f5.txt
CREATE f6.txt
```
Expected: Last one should say `ERROR: maximum number of files reached`

---

### STEP 5: Test Multi-Block Files

Each block is 128 bytes. Let's test a file that needs 2 blocks:

```
CREATE large.txt
WRITE large.txt This is a very long text that will definitely exceed 128 bytes when stored. I am adding more and more text to make sure this content spans across multiple blocks in the file system to test proper block allocation and linking functionality.
READ large.txt
```

Expected: The READ should return the exact same long text you wrote!

---

### STEP 6: Test Overwrite

```
CREATE test.txt
WRITE test.txt Original content
READ test.txt
WRITE test.txt New different content
READ test.txt
```

Expected: Second READ should show "New different content" (not "Original content")

---

## 🤖 Automated Testing

### Run All Tests at Once

```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2"
./run_all_tests.sh
```

This script tests:
1. ✅ Basic operations (CREATE, WRITE, READ, DELETE, LIST)
2. ✅ Multiple files management
3. ✅ Error handling (all error cases)
4. ✅ Maximum file limit (5 files)
5. ✅ Multi-block files (>128 bytes)
6. ✅ File overwrite
7. ✅ Delete and recreate same filename

---

## 📊 Understanding the Results

### Success Messages
All commands that work properly return:
```
SUCCESS: <message or content>
```

### Error Messages
All errors return:
```
ERROR: <description of what went wrong>
```

### What the Server Shows
In Terminal 1 (server), you'll see:
```
Handling client: Socket[addr=/0:0:0:0:0:0:0:1,port=65402,localport=12345]
Received from client: CREATE test.txt
Received from client: WRITE test.txt Hello
Received from client: READ test.txt
```

This confirms the server is receiving and processing your commands!

---

## 🐛 Troubleshooting

### Problem: "Connection refused" when connecting
**Solution:** Make sure the server is running in Terminal 1

### Problem: Server doesn't respond
**Solution:** Check that port 12345 is not already in use:
```bash
lsof -i :12345
```
If something is using it, kill it:
```bash
pkill -f "ca.concordia.Main"
```

### Problem: "Command not found: javac" or "java"
**Solution:** Java is not installed or not in PATH. Check:
```bash
java -version
```

### Problem: Server crashes or exits unexpectedly
**Solution:** Check the server output for error messages

---

## 📁 Project Structure

```
programing assignment 2/
├── FileServer/                  # Server project
│   ├── src/main/java/ca/concordia/
│   │   ├── Main.java           # Server entry point
│   │   ├── server/
│   │   │   └── FileServer.java # Socket server & command handling
│   │   └── filesystem/
│   │       ├── FileSystemManager.java  # Core file system logic
│   │       └── datastructures/
│   │           ├── FEntry.java         # File entry (name, size, first block)
│   │           └── FNode.java          # Block node (linked list)
│   ├── out/                    # Compiled .class files
│   └── filesystem.dat          # Disk storage file (created at runtime)
│
├── FileClient/                 # Client project
│   ├── src/main/java/ca/concordia/
│   │   └── Main.java          # Client entry point
│   └── out/                   # Compiled .class files
│
├── run_all_tests.sh           # Automated test script
└── TEST_RESULTS.md            # Detailed test results
```

---

## 🎓 Key Concepts Tested

### 1. **Socket Programming**
- Server listens on port 12345
- Multiple clients can connect (one at a time per connection)
- Text-based protocol (commands are strings)

### 2. **File System Simulation**
- **File Allocation Table (FAT)**: Tracks which blocks belong to which files
- **Linked List**: Blocks are chained together using `next` pointers
- **Free Block Bitmap**: Tracks which blocks are available
- **Metadata**: File entries store filename, size, and first block pointer

### 3. **Concurrency**
- Uses `ReentrantLock` for thread-safe operations
- Prevents race conditions when multiple operations occur

### 4. **Persistent Storage**
- Uses `RandomAccessFile` to read/write to disk
- `filesystem.dat` stores actual file content
- Data survives across server restarts (if server properly saves)

---

## ✅ Checklist for Complete Testing

Mark these off as you test:

- [ ] Start server successfully
- [ ] Connect client successfully
- [ ] LIST empty file system
- [ ] CREATE a file
- [ ] WRITE content to file
- [ ] READ content back (verify it matches)
- [ ] DELETE a file
- [ ] Create multiple files (at least 3)
- [ ] LIST shows all files
- [ ] Try to CREATE duplicate file (should error)
- [ ] Try to CREATE file with long name (should error)
- [ ] Try to READ non-existent file (should error)
- [ ] Try to WRITE to non-existent file (should error)
- [ ] Try to DELETE non-existent file (should error)
- [ ] Create 5 files, try to create 6th (should error)
- [ ] Write large content (>128 bytes) and read it back
- [ ] Overwrite existing file content
- [ ] Delete and recreate same filename
- [ ] QUIT successfully
- [ ] Run automated test script

---

## 🎉 Summary

You now have everything needed to test your file server!

**Quick Test:**
```bash
./run_all_tests.sh
```

**Manual Test:**
1. Start server: `cd FileServer && java -cp out ca.concordia.Main`
2. In another terminal: `echo "CREATE test.txt" | nc localhost 12345`
3. Test more commands!

**All Functions Available:**
- CREATE, DELETE, READ, WRITE, LIST, QUIT

**Good luck with your assignment! 🚀**

