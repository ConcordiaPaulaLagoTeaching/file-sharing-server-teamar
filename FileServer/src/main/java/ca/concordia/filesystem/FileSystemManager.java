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

    public void createFile(String fileName) throws Exception {
        // TODO
        throw new UnsupportedOperationException("Method not implemented yet.");
    }


    // TODO: Add readFile, writeFile and other required methods,
}
