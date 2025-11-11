package ca.concordia.filesystem.datastructures;

/**
 * FEntry - File Entry structure that stores metadata for a single file.
 * 
 * This class represents an entry in the file system's file table (similar to an inode).
 * Each file entry contains:
 * - filename: Name of the file (max 11 characters)
 * - filesize: Size of the file content in bytes
 * - firstBlock: Index of the first block in the file's block chain (-1 if empty)
 * 
 * The file system uses a linked list structure where firstBlock points to
 * the first FNode, which then links to subsequent blocks.
 * 
 * @author Aryan Aggarwal (40215476)
 * @version 1.0
 */
public class FEntry {

    /** Name of the file (maximum 11 characters) */
    private String filename;
    
    /** Size of the file in bytes */
    private short filesize;
    
    /** Index of the first block, or -1 if file is empty */
    private short firstBlock;

    /**
     * Constructs a new file entry with the specified parameters.
     * 
     * @param filename Name of the file (must be 11 characters or less)
     * @param filesize Size of the file in bytes
     * @param firstblock Index of the first block, or -1 if file has no content
     * @throws IllegalArgumentException if filename exceeds 11 characters
     */
    public FEntry(String filename, short filesize, short firstblock) throws IllegalArgumentException{
        // Validate filename length constraint
        if (filename.length() > 11) {
            throw new IllegalArgumentException("Filename cannot be longer than 11 characters.");
        }
        this.filename = filename;
        this.filesize = filesize;
        this.firstBlock = firstblock;
    }

    // Getters and Setters
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        if (filename.length() > 11) {
            throw new IllegalArgumentException("Filename cannot be longer than 11 characters.");
        }
        this.filename = filename;
    }

    public short getFilesize() {
        return filesize;
    }

    public void setFilesize(short filesize) {
        if (filesize < 0) {
            throw new IllegalArgumentException("Filesize cannot be negative.");
        }
        this.filesize = filesize;
    }

    public short getFirstBlock() {
        return firstBlock;
    }
}
