# File Server Test Results

**Date:** November 8, 2025  
**Assignment:** COEN 346 - Programming Assignment 2  
**Student:** Aryan Aggarwal (40215476)

---

## Test Summary

**Total Tests:** 7  
**Tests Passed:** ✅ 7/7 (100%)  
**Tests Failed:** ❌ 0/7

---

## Detailed Test Results

### ✅ TEST 1: Basic CREATE, WRITE, READ, DELETE
**Status:** PASSED  
**Operations Tested:**
- `LIST` - Returns "No files in the system" ✓
- `CREATE file1.txt` - File created successfully ✓
- `WRITE file1.txt Hello World from COEN 346` - Content written ✓
- `READ file1.txt` - Returns "Hello World from COEN 346" ✓
- `DELETE file1.txt` - File deleted successfully ✓
- `LIST` - Returns "No files in the system" ✓

**Result:** All basic operations work correctly.

---

### ✅ TEST 2: Multiple Files Management
**Status:** PASSED  
**Operations Tested:**
- Created 3 files (file1.txt, file2.txt, file3.txt) ✓
- `LIST` shows all 3 files ✓
- Wrote different content to each file ✓
- `READ` returns correct content for each file:
  - file1.txt → "Content A" ✓
  - file2.txt → "Content B" ✓
  - file3.txt → "Content C" ✓

**Result:** Multi-file management works correctly.

---

### ✅ TEST 3: Error Handling
**Status:** PASSED  
**Errors Tested:**
- `CREATE test.txt` twice → "ERROR: file already exists" ✓
- `READ nonexistent.txt` → "ERROR: file nonexistent.txt does not exist" ✓
- `DELETE nonexistent.txt` → "ERROR: file nonexistent.txt does not exist" ✓
- `WRITE nonexistent.txt` → "ERROR: file nonexistent.txt does not exist" ✓
- `CREATE verylongname.txt` → "ERROR: filename too large" ✓

**Result:** All error cases handled correctly with proper error messages.

---

### ✅ TEST 4: Maximum Files Limit
**Status:** PASSED  
**Constraint:** Maximum 5 files (MAXFILES = 5)  
**Operations Tested:**
- Created 5 files successfully (f1.txt through f5.txt) ✓
- `LIST` shows all 5 files ✓
- Attempt to create 6th file → "ERROR: maximum number of files reached" ✓

**Result:** File limit constraint enforced correctly.

---

### ✅ TEST 5: Multi-block File (>128 bytes)
**Status:** PASSED  
**File Size:** 187 bytes (requires 2 blocks, each block = 128 bytes)  
**Operations Tested:**
- `CREATE large.txt` ✓
- `WRITE` with 187-byte content ✓
- `READ` returns complete original content ✓

**Result:** Multi-block file handling works correctly. Content is properly split across blocks and reassembled on read.

---

### ✅ TEST 6: Overwrite Existing File
**Status:** PASSED  
**Operations Tested:**
- `CREATE file.txt` ✓
- `WRITE file.txt Original content` ✓
- `READ file.txt` → "Original content" ✓
- `WRITE file.txt New content` (overwrite) ✓
- `READ file.txt` → "New content" ✓

**Result:** File overwrite works correctly. Old blocks are freed and new content is written.

---

### ✅ TEST 7: Delete and Recreate File
**Status:** PASSED  
**Operations Tested:**
- `CREATE temp.txt` ✓
- `WRITE temp.txt First version` ✓
- `READ temp.txt` → "First version" ✓
- `DELETE temp.txt` ✓
- `CREATE temp.txt` (same name) ✓
- `WRITE temp.txt Second version` ✓
- `READ temp.txt` → "Second version" ✓

**Result:** Delete properly frees file entry allowing recreation with same name. Blocks are properly recycled.

---

## Implementation Details Verified

### ✅ File System Features
- **Maximum Files:** 5 (enforced)
- **Maximum Blocks:** 10 (128 bytes each)
- **Total Disk Size:** 1,280 bytes
- **Filename Length:** Max 11 characters (enforced)
- **Block Allocation:** Dynamic, linked-list structure
- **Thread Safety:** ReentrantLock used for all operations

### ✅ Commands Implemented
1. `CREATE <filename>` - Creates empty file
2. `DELETE <filename>` - Deletes file and frees blocks
3. `READ <filename>` - Reads and returns file content
4. `WRITE <filename> <content>` - Writes content to file
5. `LIST` - Lists all files in system
6. `QUIT` - Disconnects client

### ✅ Data Structures
- **FEntry:** File entry with filename, size, and first block pointer
- **FNode:** Block node with block index and next pointer
- **Free Block List:** Bitmap tracking available blocks
- **RandomAccessFile:** Persistent storage on disk

### ✅ Error Handling
- Duplicate file creation
- Non-existent file operations
- Filename length validation
- Maximum file limit
- File too large (insufficient blocks)
- Proper error messages for all cases

---

## Persistence Verification

✅ **Disk I/O Operations:**
- `writeFile()` uses `disk.seek()` and `disk.write()` to persist data
- `readFile()` uses `disk.seek()` and `disk.read()` to retrieve data
- Data is stored in `filesystem.dat` file
- Content survives across write/read operations

---

## Conclusion

**All test cases passed successfully.** The file server implementation:
- ✅ Handles all required commands correctly
- ✅ Properly manages file system constraints (max files, max blocks)
- ✅ Implements robust error handling
- ✅ Correctly handles multi-block files
- ✅ Persists data to disk using RandomAccessFile
- ✅ Uses proper synchronization with locks
- ✅ Validates all inputs appropriately

**The assignment is ready for submission and grading.**

---

## How to Run Tests

```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2"
./run_all_tests.sh
```

Or run server and client separately:

**Terminal 1 (Server):**
```bash
cd FileServer
java -cp out ca.concordia.Main
```

**Terminal 2 (Client):**
```bash
cd FileClient
java -cp out ca.concordia.Main
# Then type commands interactively
```

Or use netcat for testing:
```bash
echo "CREATE test.txt" | nc localhost 12345
```

