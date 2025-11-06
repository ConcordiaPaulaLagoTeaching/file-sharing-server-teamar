/**
 * File System Simulator for File Sharing Server
 * COEN 346 - Operating Systems
 * 
 * This class represents the simulated file system stored in a single file
 * with a single directory structure.
 */
public class FileSystem {
    // File system constants
    public static final int BLOCKSIZE = 128;    // Size of each block in bytes
    public static final int MAXFILES = 4;       // Maximum number of files
    public static final int MAXBLOCKS = 6;      // Maximum number of data blocks
    
    // File system structures
    private FEntry[] fileEntries;
    private FNode[] fileNodes;
    private byte[][] dataBlocks;
    
    /**
     * Initializes the file system with empty structures
     */
    public FileSystem() {
        fileEntries = new FEntry[MAXFILES];
        fileNodes = new FNode[MAXBLOCKS];
        dataBlocks = new byte[MAXBLOCKS][BLOCKSIZE];
        
        // Initialize all file entries
        for (int i = 0; i < MAXFILES; i++) {
            fileEntries[i] = new FEntry();
        }
        
        // Initialize all file nodes
        for (int i = 0; i < MAXBLOCKS; i++) {
            fileNodes[i] = new FNode();
            fileNodes[i].blockindex = -i;  // Negative indicates not in use
            fileNodes[i].nextblock = -1;
        }
    }
}

