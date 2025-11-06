/**
 * File Node (FNode) structure
 * Contains information about data blocks in a file
 */
public class FNode {
    // Index of the data block storing the file data
    // The magnitude is always the index of the fnode in its array
    // Negative if the data block is not in use
    public short blockindex;
    
    // Index into the array of fnodes for the next block
    // -1 indicates there is no next block
    public short nextblock;
    
    /**
     * Creates an empty file node
     */
    public FNode() {
        this.blockindex = -1;
        this.nextblock = -1;
    }
    
    /**
     * Checks if this fnode is in use
     */
    public boolean isInUse() {
        return blockindex >= 0;
    }
}

