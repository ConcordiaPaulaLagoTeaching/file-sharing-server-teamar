package ca.concordia;

import ca.concordia.server.FileServer;

public class Main {
    
    /** Default server port for client connections */
    private static final int DEFAULT_PORT = 12345;
    
    /** Default file system storage file name */
    private static final String DEFAULT_FILESYSTEM = "filesystem.dat";
    
    /** Default total storage size in bytes (10 blocks × 128 bytes) */
    private static final int DEFAULT_STORAGE_SIZE = 10 * 128;
    
    /**
     * Application entry point that initializes and starts the file server.
     * 
     * This method creates a new FileServer instance with default configuration
     * and starts it in blocking mode. The server will continue running until
     * manually terminated or an unrecoverable error occurs.
     * 
     * Configuration Details:
     * - Listens on port 12345 for TCP connections
     * - Creates/uses "filesystem.dat" for persistent storage
     * - Allocates 1,280 bytes total storage (10 × 128-byte blocks)
     * - Supports up to 5 files and 1,000 concurrent clients
     * 
     * @param args Command line arguments (currently unused)
     */
    public static void main(String[] args) {
        // Display welcome message with server information
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              COEN 346 - File Sharing Server                  ║");
        System.out.println("║                  Programming Assignment 2                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("   Initializing File Server...");
        System.out.println("   Port: " + DEFAULT_PORT);
        System.out.println("   Storage: " + DEFAULT_FILESYSTEM + " (" + DEFAULT_STORAGE_SIZE + " bytes)");
        System.out.println("   Max Files: 5");
        System.out.println("   Max Clients: 1,000 concurrent connections");
        System.out.println();

        try {
            // Initialize the file server with default configuration
            FileServer server = new FileServer(DEFAULT_PORT, DEFAULT_FILESYSTEM, DEFAULT_STORAGE_SIZE);
            
            // Start the server (this call blocks until server shuts down)
            System.out.println("Server initialized successfully!");
            System.out.println("Starting server...");
            System.out.println();
            server.start();
            
        } catch (Exception e) {
            // Handle any initialization or startup errors
            System.err.println("Failed to start server: " + e.getMessage());
            System.err.println("Please check that:");
            System.err.println("  - Port " + DEFAULT_PORT + " is not already in use");
            System.err.println("  - You have write permissions in the current directory");
            System.err.println("  - No firewall is blocking the port");
            e.printStackTrace();
            System.exit(1);
        }
    }
}