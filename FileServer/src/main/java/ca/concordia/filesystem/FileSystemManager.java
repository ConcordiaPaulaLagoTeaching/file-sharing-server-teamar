package ca.concordia.filesystem;

import ca.concordia.filesystem.datastructures.FEntry;
import ca.concordia.filesystem.datastructures.FNode;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * FileSystemManager - Core file system implementation that manages file operations
 * and block allocation with readers-writers synchronization.
 * 
 * This class simulates a simple file system with:
 * - Fixed number of file entries (like an inode table)
 * - Block-based storage with linked list allocation
 * - Persistent storage using RandomAccessFile
 * - Thread-safe operations using ReadWriteLock
 * 
 * File System Structure:
 * - Maximum 5 files (MAXFILES)
 * - 10 blocks of 128 bytes each (MAXBLOCKS * BLOCK_SIZE = 1280 bytes total)
 * - Files can span multiple blocks using a linked list structure
 * - Free block bitmap tracks available blocks
 * 
 * Synchronization Strategy (Readers-Writers):
 * - READ and LIST operations acquire read lock (multiple concurrent readers allowed)
 * - CREATE, WRITE, DELETE operations acquire write lock (exclusive access)
 * - When a writer has the lock, no readers or other writers can proceed
 * - Multiple readers can read concurrently without blocking each other
 * - This prevents race conditions while maximizing concurrency for read operations
 * 
 * @author Aryan Aggarwal (40215476)
 * @version 2.0
 */
public class FileSystemManager {

    /** Maximum number of files that can be stored in the file system */
    private final int MAXFILES = 5;
    
    /** Maximum number of blocks available for file storage */
    private final int MAXBLOCKS = 10;
    
    /** RandomAccessFile handle for persistent disk storage */
    private RandomAccessFile disk;
    
    /** 
     * ReadWriteLock for readers-writers synchronization
     * - Allows multiple concurrent readers (READ, LIST operations)
     * - Ensures exclusive access for writers (CREATE, WRITE, DELETE operations)
     */
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /** Size of each block in bytes (128 bytes per block) */
    private static final int BLOCK_SIZE = 128;

    /** Array of file entries containing metadata for each file */
    private FEntry[] fileEntries;
    
    /** Array of file nodes representing blocks in the file system */
    private FNode[] fileNodes;
    
    /** Bitmap tracking which blocks are free (true) or in use (false) */
    private boolean[] freeBlockList;

    /**
     * Constructs a new FileSystemManager and initializes the file system structures.
     * 
     * This constructor:
     * 1. Creates or opens the disk file for persistent storage
     * 2. Initializes file entry table (like inodes)
     * 3. Initializes block nodes for linked list allocation
     * 4. Sets up free block bitmap
     * 
     * @param filename The name of the file to store file system data (e.g., "filesystem.dat")
     * @param totalSize The total size of the file system in bytes (not currently enforced)
     * @throws RuntimeException if the file system cannot be initialized
     */
    public FileSystemManager(String filename, int totalSize) {
        try {
            // Create or open the RandomAccessFile for persistent storage
            File fsFile = new File(filename);
            this.disk = new RandomAccessFile(fsFile, "rw");
            
            // Initialize the file entries array (file metadata table)
            // Initially all entries are null (no files exist)
            this.fileEntries = new FEntry[MAXFILES];
            
            // Initialize file nodes (blocks) for storage
            // Each FNode represents one block in the file system
            this.fileNodes = new FNode[MAXBLOCKS];
            for (int i = 0; i < MAXBLOCKS; i++) {
                // Negative blockIndex indicates the block is not in use
                fileNodes[i] = new FNode(-i);
            }
            
            // Initialize the free block bitmap
            // true = block is free, false = block is in use
            this.freeBlockList = new boolean[MAXBLOCKS];
            for (int i = 0; i < MAXBLOCKS; i++) {
                freeBlockList[i] = true; // All blocks start as free
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize file system: " + e.getMessage());
        }
    }

    /**
     * Creates a new empty file with the given filename.
     * 
     * The file is initially created with no blocks allocated (size = 0).
     * Blocks will be allocated when content is written using writeFile().
     * 
     * @param fileName Name of the file to create (maximum 11 characters)
     * @throws Exception if:
     *         - filename exceeds 11 characters
     *         - file already exists
     *         - maximum number of files (5) has been reached
     */
    public void createFile(String fileName) throws Exception {
        rwLock.writeLock().lock();  // Acquire write lock (exclusive access)
        try {
            // Validate filename length (11 characters max)
            if (fileName.length() > 11) {
                throw new Exception("ERROR: filename too large");
            }
            
            // Check for duplicate files
            if (findFileByName(fileName) != -1) {
                throw new Exception("ERROR: file already exists");
            }
            
            // Find an available slot in the file entry table
            int entryIndex = findAvailableFileEntry();
            if (entryIndex == -1) {
                throw new Exception("ERROR: maximum number of files reached");
            }
            
            // Create new file entry: filename, size=0, firstBlock=-1 (no blocks yet)
            fileEntries[entryIndex] = new FEntry(fileName, (short) 0, (short) -1);
            
        } finally {
            rwLock.writeLock().unlock();  // Release write lock
        }
    }

    /**
     * Deletes a file with the given filename
     * @param fileName name of the file to delete
     * @throws Exception if file does not exist
     */
    public void deleteFile(String fileName) throws Exception {
        rwLock.writeLock().lock();  // Acquire write lock (exclusive access)
        try {
            // Find the file
            int entryIndex = findFileByName(fileName);
            if (entryIndex == -1) {
                throw new Exception("ERROR: file " + fileName + " does not exist");
            }
            
            FEntry entry = fileEntries[entryIndex];
            
            // Free all blocks used by this file
            if (entry.getFirstBlock() != -1) {
                int currentBlock = entry.getFirstBlock();
                while (currentBlock != -1) {
                    FNode node = fileNodes[currentBlock];
                    int nextBlock = node.getNext();
                    
                    // Mark block as free
                    freeBlockList[currentBlock] = true;
                    node.setBlockIndex(-currentBlock);
                    node.setNext(-1);
                    
                    currentBlock = nextBlock;
                }
            }
            
            // Remove file entry
            fileEntries[entryIndex] = null;
            
        } finally {
            rwLock.writeLock().unlock();  // Release write lock
        }
    }

    /**
     * Lists all files in the file system.
     * 
     * This is a READ operation that uses the read lock, allowing
     * multiple clients to list files concurrently.
     * 
     * @return String containing all filenames separated by newlines
     */
    public String listFiles() {
        rwLock.readLock().lock();  // Acquire read lock (shared access)
        try {
            StringBuilder fileList = new StringBuilder();
            for (int i = 0; i < MAXFILES; i++) {
                if (fileEntries[i] != null) {
                    fileList.append(fileEntries[i].getFilename()).append("\n");
                }
            }
            
            if (fileList.length() == 0) {
                return "No files in the system";
            }
            
            return fileList.toString();
        } finally {
            rwLock.readLock().unlock();  // Release read lock
        }
    }

    /**
     * Writes content to an existing file, overwriting any previous content.
     * 
     * This method:
     * 1. Frees all blocks currently used by the file
     * 2. Calculates how many blocks are needed for the new content
     * 3. Allocates new blocks and links them together
     * 4. Writes the content to disk across multiple blocks if needed
     * 5. Updates the file entry with new size and first block pointer
     * 
     * @param fileName Name of the file to write to
     * @param content The text content to write to the file
     * @throws Exception if:
     *         - file does not exist
     *         - content is too large (not enough free blocks)
     */
    public void writeFile(String fileName, String content) throws Exception {
        rwLock.writeLock().lock();  // Acquire write lock (exclusive access)
        try {
            // Find the file entry
            int entryIndex = findFileByName(fileName);
            if (entryIndex == -1) {
                throw new Exception("ERROR: file " + fileName + " does not exist");
            }
            
            FEntry entry = fileEntries[entryIndex];
            byte[] contentBytes = content.getBytes();
            int contentSize = contentBytes.length;
            
            // Calculate how many blocks we need
            // Each block is 128 bytes, so we divide and round up
            int blocksNeeded = (contentSize + BLOCK_SIZE - 1) / BLOCK_SIZE;
            
            // Free all existing blocks used by this file (if any)
            if (entry.getFirstBlock() != -1) {
                int currentBlock = entry.getFirstBlock();
                while (currentBlock != -1) {
                    FNode node = fileNodes[currentBlock];
                    int nextBlock = node.getNext();
                    
                    // Mark block as free
                    freeBlockList[currentBlock] = true;
                    node.setBlockIndex(-currentBlock); // Negative indicates free
                    node.setNext(-1);
                    
                    currentBlock = nextBlock;
                }
                entry.setFilesize((short) 0);
            }
            
            // Check if we have enough free blocks for the new content
            int availableBlocks = 0;
            for (int i = 0; i < MAXBLOCKS; i++) {
                if (freeBlockList[i]) availableBlocks++;
            }
            
            if (availableBlocks < blocksNeeded) {
                throw new Exception("ERROR: file too large");
            }
            
            // Allocate blocks and write content to disk
            int previousBlock = -1;
            int firstBlock = -1;
            int bytesWritten = 0;
            
            for (int i = 0; i < blocksNeeded; i++) {
                // Find the next available block
                int blockIndex = findAvailableBlock();
                if (blockIndex == -1) {
                    throw new Exception("ERROR: file too large");
                }
                
                // Mark this block as in use
                freeBlockList[blockIndex] = false;
                fileNodes[blockIndex].setBlockIndex(blockIndex);
                
                // Link blocks together (form a linked list)
                if (i == 0) {
                    firstBlock = blockIndex; // Remember the first block
                } else {
                    fileNodes[previousBlock].setNext(blockIndex); // Link previous block to this one
                }
                
                // Prepare data for this block
                int bytesToWrite = Math.min(BLOCK_SIZE, contentSize - bytesWritten);
                byte[] blockData = new byte[BLOCK_SIZE];
                System.arraycopy(contentBytes, bytesWritten, blockData, 0, bytesToWrite);
                
                // Write data to disk at the block's position
                // Block position = blockIndex * BLOCK_SIZE
                disk.seek(blockIndex * BLOCK_SIZE);
                disk.write(blockData);
                bytesWritten += bytesToWrite;
                
                previousBlock = blockIndex;
            }
            
            // Update the file entry with new size and first block pointer
            if (firstBlock != -1) {
                FEntry newEntry = new FEntry(fileName, (short) contentSize, (short) firstBlock);
                fileEntries[entryIndex] = newEntry;
            }
            
        } finally {
            rwLock.writeLock().unlock();  // Release write lock
        }
    }

    /**
     * Reads and returns the content of a file.
     * 
     * This is a READ operation that uses the read lock, allowing
     * multiple clients to read files concurrently without blocking each other.
     * 
     * This method:
     * 1. Finds the file in the file entry table
     * 2. Follows the linked list of blocks
     * 3. Reads data from each block on disk
     * 4. Assembles the complete file content
     * 5. Converts bytes to a string and returns it
     * 
     * @param fileName Name of the file to read
     * @return String containing the complete file content
     * @throws Exception if file does not exist
     */
    public String readFile(String fileName) throws Exception {
        rwLock.readLock().lock();  // Acquire read lock (shared access)
        try {
            // Find the file entry
            int entryIndex = findFileByName(fileName);
            if (entryIndex == -1) {
                throw new Exception("ERROR: file " + fileName + " does not exist");
            }
            
            FEntry entry = fileEntries[entryIndex];
            
            // Handle empty files
            if (entry.getFirstBlock() == -1 || entry.getFilesize() == 0) {
                return "";
            }
            
            // Read content from all blocks in the linked list
            int totalBytes = entry.getFilesize();
            byte[] fileContent = new byte[totalBytes]; // Buffer to hold complete file
            int currentBlock = entry.getFirstBlock();
            int bytesRead = 0;
            
            // Traverse the linked list of blocks
            while (currentBlock != -1 && bytesRead < totalBytes) {
                FNode node = fileNodes[currentBlock];
                
                // Determine how many bytes to read from this block
                int bytesToRead = Math.min(BLOCK_SIZE, totalBytes - bytesRead);
                byte[] blockData = new byte[BLOCK_SIZE];
                
                // Read block from disk
                disk.seek(currentBlock * BLOCK_SIZE);
                disk.read(blockData);
                
                // Copy the relevant bytes into our file content buffer
                System.arraycopy(blockData, 0, fileContent, bytesRead, bytesToRead);
                bytesRead += bytesToRead;
                
                // Move to the next block in the chain
                currentBlock = node.getNext();
            }
            
            // Convert bytes to string and return
            return new String(fileContent);
        } finally {
            rwLock.readLock().unlock();  // Release read lock
        }
    }
    
    /**
     * Finds the first available file entry slot
     * @return index of available file entry, or -1 if none available
     */
    private int findAvailableFileEntry() {
        for (int i = 0; i < MAXFILES; i++) {
            if (fileEntries[i] == null) {
                return i;
            }
        }
        return -1; // No available file entry
    }
    
    /**
     * Finds a file entry by filename
     * @param filename the name of the file to find
     * @return index of the file entry, or -1 if not found
     */
    private int findFileByName(String filename) {
        for (int i = 0; i < MAXFILES; i++) {
            if (fileEntries[i] != null && fileEntries[i].getFilename().equals(filename)) {
                return i;
            }
        }
        return -1; // File not found
    }
    
    /**
     * Finds the first available file node (free block)
     * @return index of available file node, or -1 if none available
     */
    private int findAvailableBlock() {
        for (int i = 0; i < MAXBLOCKS; i++) {
            if (freeBlockList[i]) {
                return i;
            }
        }
        return -1; // No available blocks
    }
}
