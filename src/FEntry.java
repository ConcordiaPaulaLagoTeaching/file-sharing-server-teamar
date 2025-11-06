/**
 * File Entry (FEntry) structure
 * Represents metadata for a file in the file system
 */
public class FEntry {
    public static final int MAX_FILENAME_LENGTH = 11;
    
    // File name (maximum 11 characters)
    public char[] filename;
    
    // Actual size of the file (file might not use all allocated space)
    public short size;
    
    // Index into the array of fnodes (first data block of the file)
    public short firstblock;
    
    /**
     * Creates an empty file entry
     */
    public FEntry() {
        this.filename = new char[MAX_FILENAME_LENGTH + 1]; // +1 for null terminator
        this.size = 0;
        this.firstblock = -1; // -1 indicates file entry is not in use
    }
    
    /**
     * Checks if this file entry is in use
     */
    public boolean isInUse() {
        return firstblock != -1 || filename[0] != '\0';
    }
    
    /**
     * Gets the filename as a String
     */
    public String getFilename() {
        int length = 0;
        while (length < filename.length && filename[length] != '\0') {
            length++;
        }
        return new String(filename, 0, length);
    }
    
    /**
     * Sets the filename from a String
     */
    public void setFilename(String name) throws Exception {
        if (name.length() > MAX_FILENAME_LENGTH) {
            throw new Exception("ERROR: filename too large");
        }
        
        // Clear the array
        for (int i = 0; i < filename.length; i++) {
            filename[i] = '\0';
        }
        
        // Copy the name
        for (int i = 0; i < name.length(); i++) {
            filename[i] = name.charAt(i);
        }
    }
}

