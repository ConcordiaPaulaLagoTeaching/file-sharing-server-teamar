package ca.concordia;

import ca.concordia.filesystem.FileSystemManager;
import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FileSystemTests - JUnit tests for file system operations
 * Tests: CREATE, WRITE, READ, DELETE operations with various scenarios
 */
public class FileSystemTests {

    private FileSystemManager fsManager;
    private static final String TEST_FS_FILE = "test_filesystem.dat";

    @BeforeEach
    public void setUp() {
        // Clean up any existing test file
        new File(TEST_FS_FILE).delete();
        // Create fresh file system for each test
        fsManager = new FileSystemManager(TEST_FS_FILE, 10 * 128);
    }

    @AfterEach
    public void tearDown() {
        // Clean up test file after each test
        new File(TEST_FS_FILE).delete();
    }

    @Test
    public void testCreateFile() throws Exception {
        // Test creating a file
        fsManager.createFile("test.txt");
        
        // Verify file exists by listing
        String files = fsManager.listFiles();
        assertTrue(files.contains("test.txt"), "File should exist after creation");
    }

    @Test
    public void testWriteAndReadFile() throws Exception {
        // Create file
        fsManager.createFile("data.txt");
        
        // Write content
        String content = "Hello World";
        fsManager.writeFile("data.txt", content);
        
        // Read and verify
        String readContent = fsManager.readFile("data.txt");
        assertEquals(content, readContent, "Read content should match written content");
    }

    @Test
    public void testWriteAndReadLongFile() throws Exception {
        // Create file
        fsManager.createFile("long.txt");
        
        // Write long content (>128 bytes to span multiple blocks)
        String longContent = "This is a very long text that will definitely exceed 128 bytes when stored. " +
                           "I am adding more and more text to make sure this content spans across multiple " +
                           "blocks in the file system to test proper block allocation and linking.";
        
        fsManager.writeFile("long.txt", longContent);
        
        // Read and verify
        String readContent = fsManager.readFile("long.txt");
        assertEquals(longContent, readContent, "Long file content should be preserved");
    }

    @Test
    public void testTooLongFilename() {
        // Test that filename longer than 11 characters throws exception
        assertThrows(Exception.class, () -> {
            fsManager.createFile("verylongfilename.txt"); // 22 characters
        }, "Should throw exception for filename longer than 11 characters");
    }

    @Test
    public void testDeleteFile() throws Exception {
        // Create and verify file exists
        fsManager.createFile("delete.txt");
        assertTrue(fsManager.listFiles().contains("delete.txt"));
        
        // Delete file
        fsManager.deleteFile("delete.txt");
        
        // Verify file no longer exists
        assertFalse(fsManager.listFiles().contains("delete.txt"), 
                   "File should not exist after deletion");
    }

    @Test
    public void testFileNotExist() {
        // Test reading non-existent file
        assertThrows(Exception.class, () -> {
            fsManager.readFile("nonexistent.txt");
        }, "Should throw exception when reading non-existent file");
    }

    @Test
    public void testDuplicateFile() throws Exception {
        fsManager.createFile("dup.txt");
        
        // Try to create duplicate
        assertThrows(Exception.class, () -> {
            fsManager.createFile("dup.txt");
        }, "Should throw exception when creating duplicate file");
    }

    @Test
    public void testMaxFiles() throws Exception {
        // Create 5 files (MAXFILES = 5)
        for (int i = 1; i <= 5; i++) {
            fsManager.createFile("f" + i + ".txt");
        }
        
        // Try to create 6th file
        assertThrows(Exception.class, () -> {
            fsManager.createFile("f6.txt");
        }, "Should throw exception when exceeding maximum files");
    }
}

