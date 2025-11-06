# File Sharing Server - COEN 346 Programming Assignment 2

## Project Description
A file-sharing server simulator that enables multiple users to access and share files from a central location. The server implements a simulated file system with support for concurrent reads and exclusive writes.

## Team Members
- Team AR

## Project Structure
```
src/
  ├── FEntry.java       - File entry metadata structure
  ├── FNode.java        - File node structure for data blocks
  └── FileSystem.java   - Main file system implementation
```

## File System Configuration
- **Block Size**: 128 bytes
- **Maximum Files**: 4
- **Maximum Blocks**: 6

## Implementation Progress
- [x] Basic data structures (FEntry, FNode)
- [ ] File system operations (create, delete, write, read, list)
- [ ] Server/client architecture
- [ ] Multithreading support
- [ ] Synchronization for concurrent access

## Building and Running
*Instructions to be added as implementation progresses*

## References
Based on CSC209 programming assignment by Prof. Kianoosh Abassi and Andreas Bergen, University of Toronto.

