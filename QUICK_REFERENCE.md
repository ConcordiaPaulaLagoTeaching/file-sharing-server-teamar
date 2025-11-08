# 📌 Quick Reference Card - File Server

## 🚀 Start Server & Client

### Start Server (Terminal 1):
```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2/FileServer"
java -cp out ca.concordia.Main
```

### Connect Client (Terminal 2):
```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2/FileClient"
java -cp out ca.concordia.Main
```

### Quick Test (Terminal 2):
```bash
echo "LIST" | nc localhost 12345
```

---

## 📝 All Commands

| Command | Syntax | Example |
|---------|--------|---------|
| **LIST** | `LIST` | `LIST` |
| **CREATE** | `CREATE <filename>` | `CREATE myfile.txt` |
| **WRITE** | `WRITE <filename> <content>` | `WRITE myfile.txt Hello World` |
| **READ** | `READ <filename>` | `READ myfile.txt` |
| **DELETE** | `DELETE <filename>` | `DELETE myfile.txt` |
| **QUIT** | `QUIT` | `QUIT` |

---

## ⚙️ System Limits

- **Max Files:** 5
- **Max Blocks:** 10 (128 bytes each)
- **Total Storage:** 1,280 bytes
- **Max Filename Length:** 11 characters
- **Server Port:** 12345

---

## 🧪 Quick Test Sequence

Copy-paste this entire block into your terminal:
```bash
(
echo "LIST"
echo "CREATE test.txt"
echo "WRITE test.txt Hello from COEN 346"
echo "READ test.txt"
echo "LIST"
echo "DELETE test.txt"
echo "LIST"
echo "QUIT"
) | nc localhost 12345
```

---

## 🤖 Run All Automated Tests

```bash
cd "/Users/aryanaggarwal/Desktop/COEN 346/programing assignment 2"
./run_all_tests.sh
```

---

## ✅ Expected Results

### Success Response:
```
SUCCESS: <message or data>
```

### Error Response:
```
ERROR: <description>
```

---

## 🔄 Common Workflows

### Create, Write, Read:
```
CREATE myfile.txt
WRITE myfile.txt Some content here
READ myfile.txt
```

### List all files:
```
LIST
```

### Delete and verify:
```
DELETE myfile.txt
LIST
```

---

## ❌ Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `ERROR: file already exists` | CREATE duplicate | Use different name or DELETE first |
| `ERROR: filename too large` | Name > 11 chars | Use shorter name |
| `ERROR: file does not exist` | File not created | CREATE file first |
| `ERROR: maximum number of files reached` | Already have 5 files | DELETE a file first |
| `ERROR: file too large` | Content needs >10 blocks | Use shorter content |

---

## 🛠️ Troubleshooting

### Server not starting?
```bash
# Check if port is in use
lsof -i :12345

# Kill existing server
pkill -f "ca.concordia.Main"
```

### Recompile code:
```bash
# Server
cd FileServer
rm -rf out && mkdir out
javac -d out src/main/java/ca/concordia/filesystem/datastructures/*.java \
               src/main/java/ca/concordia/filesystem/*.java \
               src/main/java/ca/concordia/server/*.java \
               src/main/java/ca/concordia/*.java

# Client
cd ../FileClient
rm -rf out && mkdir out
javac -d out src/main/java/ca/concordia/*.java
```

### Fresh start:
```bash
cd FileServer
rm -f filesystem.dat  # Delete old data
java -cp out ca.concordia.Main
```

---

## 📚 More Info

- **Full Guide:** `TESTING_GUIDE.md`
- **Test Results:** `TEST_RESULTS.md`
- **Assignment PDF:** `Programming Assignment 2.pdf`

