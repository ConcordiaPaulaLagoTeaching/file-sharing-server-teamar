package ca.concordia.filesystem.datastructures;

/**
 * FNode - File Node structure representing a single block in the file system.
 * 
 * This class implements a node in a linked list used for file storage.
 * Each FNode represents one 128-byte block and contains:
 * - blockIndex: The index/position of this block (negative if not in use)
 * - next: Index of the next block in the chain (-1 if this is the last block)
 * 
 * Blocks are chained together to form files. For example, a file might use
 * blocks 3 -> 7 -> 2, where:
 * - FNode[3] has next=7
 * - FNode[7] has next=2
 * - FNode[2] has next=-1 (end of file)
 * 
 * @author Aryan Aggarwal (40215476)
 * @version 1.0
 */
public class FNode {

    /** Index of this block (negative if block is free/not in use) */
    private int blockIndex;
    
    /** Index of the next block in the chain, or -1 if this is the last block */
    private int next;

    /**
     * Constructs a new FNode with the specified block index.
     * 
     * @param blockIndex The block index (negative indicates block is not in use)
     */
    public FNode(int blockIndex) {
        this.blockIndex = blockIndex;
        this.next = -1; // Initially not linked to any other block
    }

    // Getters and Setters
    
    /**
     * Gets the block index.
     * @return The block index (negative if not in use)
     */
    public int getBlockIndex() {
        return blockIndex;
    }

    /**
     * Sets the block index.
     * @param blockIndex The new block index
     */
    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    /**
     * Gets the index of the next block in the chain.
     * @return The next block index, or -1 if this is the last block
     */
    public int getNext() {
        return next;
    }

    /**
     * Sets the index of the next block in the chain.
     * @param next The next block index (-1 for end of chain)
     */
    public void setNext(int next) {
        this.next = next;
    }
    
    /**
     * Checks if this block is currently in use.
     * @return true if the block index is non-negative (in use), false otherwise
     */
    public boolean isInUse() {
        return blockIndex >= 0;
    }
}
