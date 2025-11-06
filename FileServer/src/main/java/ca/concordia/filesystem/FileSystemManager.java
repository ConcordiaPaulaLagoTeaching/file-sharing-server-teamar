package ca.concordia.filesystem;

import ca.concordia.filesystem.datastructures.FEntry;
import ca.concordia.filesystem.datastructures.FNode;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

public class FileSystemManager {

    private final int MAXFILES = 5;
    private final int MAXBLOCKS = 10;
    private RandomAccessFile disk;
    private final ReentrantLock globalLock = new ReentrantLock();

    private static final int BLOCK_SIZE = 128; // Block size in bytes

    private FEntry[] fileEntries; // Array of file entries
    private FNode[] fileNodes; // Array of file nodes
    private boolean[] freeBlockList; // Bitmap for free blocks

    public FileSystemManager(String filename, int totalSize) {
        try {
            // Create or open the file system file
            File fsFile = new File(filename);
            this.disk = new RandomAccessFile(fsFile, "rw");
            
            // Initialize file entries array
            this.fileEntries = new FEntry[MAXFILES];
            
            // Initialize file nodes array
            this.fileNodes = new FNode[MAXBLOCKS];
            for (int i = 0; i < MAXBLOCKS; i++) {
                fileNodes[i] = new FNode(-i); // Negative indicates not in use
            }
            
            // Initialize free block list
            this.freeBlockList = new boolean[MAXBLOCKS];
            for (int i = 0; i < MAXBLOCKS; i++) {
                freeBlockList[i] = true; // All blocks initially free
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize file system: " + e.getMessage());
        }
    }

    /**
     * Creates a new empty file with the given filename
     * @param fileName name of the file to create (max 11 characters)
     * @throws Exception if filename is too long, file already exists, or no space available
     */
    public void createFile(String fileName) throws Exception {
        globalLock.lock();
        try {
            // Check filename length
            if (fileName.length() > 11) {
                throw new Exception("ERROR: filename too large");
            }
            
            // Check if file already exists
            if (findFileByName(fileName) != -1) {
                throw new Exception("ERROR: file already exists");
            }
            
            // Find available file entry
            int entryIndex = findAvailableFileEntry();
            if (entryIndex == -1) {
                throw new Exception("ERROR: maximum number of files reached");
            }
            
            // Create new file entry with no blocks allocated initially (empty file)
            fileEntries[entryIndex] = new FEntry(fileName, (short) 0, (short) -1);
            
        } finally {
            globalLock.unlock();
        }
    }

    /**
     * Deletes a file with the given filename
     * @param fileName name of the file to delete
     * @throws Exception if file does not exist
     */
    public void deleteFile(String fileName) throws Exception {
        globalLock.lock();
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
            globalLock.unlock();
        }
    }

    // TODO: Add readFile, writeFile and other required methods,
    
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
