package ca.concordia.server;
import ca.concordia.filesystem.FileSystemManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * FileServer - A multi-threaded TCP socket-based file server that handles
 * concurrent client connections using a thread pool.
 * 
 * This server is designed to handle thousands of concurrent clients efficiently.
 * It uses a fixed thread pool to manage client connections and implements
 * readers-writers synchronization to allow multiple concurrent reads while
 * ensuring exclusive write access.
 * 
 * Threading Strategy:
 * - Uses ExecutorService with a fixed thread pool (1000 threads)
 * - Each client connection is handled by a separate thread
 * - Thread pool prevents resource exhaustion from too many threads
 * 
 * Synchronization:
 * - ReadWriteLock allows multiple concurrent readers
 * - Write operations get exclusive access (no readers or other writers)
 * - Prevents race conditions and data corruption
 * 
 * @author Aryan Aggarwal (40215476)
 * @version 2.0
 */
public class FileServer {

    /** File system manager that handles all file operations */
    private FileSystemManager fsManager;
    
    /** Port number on which the server listens for connections */
    private int port;
    
    /** Thread pool for handling concurrent client connections */
    private ExecutorService threadPool;
    
    /** Maximum number of concurrent client threads */
    private static final int MAX_THREADS = 1000;
    
    /**
     * Constructs a new FileServer with the specified configuration.
     * 
     * Initializes the file system manager and creates a fixed thread pool
     * capable of handling up to MAX_THREADS (1000) concurrent client connections.
     * 
     * @param port The port number to listen on (e.g., 12345)
     * @param fileSystemName The name of the file to store the file system data
     * @param totalSize The total size of the file system in bytes
     */
    public FileServer(int port, String fileSystemName, int totalSize){
        // Initialize the FileSystemManager with the disk file and total size
        this.fsManager = new FileSystemManager(fileSystemName, totalSize);
        this.port = port;
        
        // Create a fixed thread pool to handle concurrent clients
        // This prevents resource exhaustion from too many threads
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        
        System.out.println("Thread pool initialized with " + MAX_THREADS + " threads");
    }

    /**
     * Starts the file server and begins listening for client connections.
     * 
     * The server runs in an infinite loop, accepting client connections and
     * submitting each connection to the thread pool for concurrent handling.
     * This allows thousands of clients to be served simultaneously.
     * 
     * Threading Model:
     * - Main thread accepts connections
     * - Each client is handled by a worker thread from the pool
     * - Thread pool manages resources efficiently
     * 
     * Supported commands:
     * - CREATE <filename>: Creates a new file
     * - DELETE <filename>: Deletes an existing file
     * - READ <filename>: Reads and returns file content
     * - WRITE <filename> <content>: Writes content to a file
     * - LIST: Lists all files in the system
     * - QUIT: Closes the client connection
     */
    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Server started. Listening on port " + this.port + "...");
            System.out.println("Ready to handle up to " + MAX_THREADS + " concurrent clients");

            // Main server loop - continuously accept client connections
            while (true) {
                // Wait for and accept a new client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                
                // Submit the client to the thread pool for concurrent handling
                threadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
            // Handle server socket creation or binding errors
            e.printStackTrace();
            System.err.println("Could not start server on port " + port);
        } finally {
            // Shutdown the thread pool gracefully
            shutdownThreadPool();
        }
    }
    
    /**
     * Handles a single client connection in its own thread.
     * 
     * This method processes all commands from a client until the client
     * disconnects or sends a QUIT command. Each client runs independently
     * in the thread pool, allowing concurrent request processing.
     * 
     * @param clientSocket The socket connection to the client
     */
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            // Process commands from client until connection is closed
            while ((line = reader.readLine()) != null) {
                System.out.println("[Thread " + Thread.currentThread().getId() + "] Received: " + line);
                
                // Parse the command and arguments
                String[] parts = line.split(" ");
                String command = parts[0].toUpperCase();

                try {
                    // Process the command using a switch statement for efficient command routing
                    // Each case handles a specific file operation with proper validation and error handling
                    switch (command) {
                        case "CREATE":
                            // CREATE <filename> - Creates a new empty file in the file system
                            // Validation: Ensures filename is provided
                            // Constraints: Max 5 files, filename ≤ 11 characters
                            if (parts.length < 2) {
                                writer.println("ERROR: CREATE requires a filename");
                                break;
                            }
                            // Delegate to FileSystemManager with write lock for thread safety
                            fsManager.createFile(parts[1]);
                            writer.println("SUCCESS: File '" + parts[1] + "' created.");
                            writer.flush(); // Ensure immediate response to client
                            break;
                            
                        case "DELETE":
                            // DELETE <filename> - Permanently removes a file and frees all its allocated blocks
                            // Security: Validates filename parameter to prevent malformed requests
                            // Performance: Uses write lock to ensure exclusive access during deletion
                            // Memory Management: Automatically frees blocks back to the free block list
                            if (parts.length < 2) {
                                writer.println("ERROR: DELETE requires a filename");
                                break;
                            }
                            fsManager.deleteFile(parts[1]);
                            writer.println("SUCCESS: File '" + parts[1] + "' deleted.");
                            writer.flush();
                            break;
                            
                        case "READ":
                            // READ <filename> - Retrieves and returns the complete content of a file
                            // Concurrency: Uses read lock allowing multiple simultaneous readers
                            // Performance: Efficiently traverses linked list of blocks
                            // Data Integrity: Guarantees consistent read even during concurrent operations
                            if (parts.length < 2) {
                                writer.println("ERROR: READ requires a filename");
                                break;
                            }
                            String content = fsManager.readFile(parts[1]);
                            writer.println("SUCCESS: " + content);
                            writer.flush(); // Send response immediately to minimize client wait time
                            break;
                            
                        case "WRITE":
                            // WRITE <filename> <content> - Writes content to an existing file
                            if (parts.length < 3) {
                                writer.println("ERROR: WRITE requires filename and content");
                                break;
                            }
                            // Reconstruct the content from all parts after the filename
                            // This allows multi-word content to be written
                            StringBuilder writeContent = new StringBuilder();
                            for (int i = 2; i < parts.length; i++) {
                                if (i > 2) writeContent.append(" ");
                                writeContent.append(parts[i]);
                            }
                            fsManager.writeFile(parts[1], writeContent.toString());
                            writer.println("SUCCESS: File '" + parts[1] + "' written.");
                            writer.flush();
                            break;
                            
                        case "LIST":
                            // LIST - Lists all files in the file system
                            String fileList = fsManager.listFiles();
                            writer.println("SUCCESS: " + fileList);
                            writer.flush();
                            break;
                            
                        case "QUIT":
                            // QUIT - Closes the client connection gracefully
                            writer.println("SUCCESS: Disconnecting.");
                            return;
                            
                        default:
                            // Unknown command received
                            writer.println("ERROR: Unknown command.");
                            break;
                    }
                } catch (Exception cmdException) {
                    // Handle any exceptions from file system operations
                    // Error messages are already formatted by FileSystemManager
                    writer.println(cmdException.getMessage());
                    writer.flush();
                }
            }
        } catch (Exception e) {
            // Handle any unexpected errors during client communication
            System.err.println("[Thread " + Thread.currentThread().getId() + "] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure client socket is properly closed
            try {
                clientSocket.close();
                System.out.println("[Thread " + Thread.currentThread().getId() + "] Client disconnected");
            } catch (Exception e) {
                // Ignore closing errors
            }
        }
    }
    
    /**
     * Gracefully shuts down the thread pool.
     * 
     * This method attempts to shut down the thread pool gracefully by:
     * 1. Preventing new tasks from being submitted
     * 2. Waiting for existing tasks to complete (up to 60 seconds)
     * 3. Forcing shutdown if tasks don't complete in time
     */
    private void shutdownThreadPool() {
        try {
            System.out.println("Shutting down thread pool...");
            threadPool.shutdown();
            
            // Wait up to 60 seconds for tasks to complete
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown...");
                threadPool.shutdownNow();
                
                // Wait for forced shutdown to complete
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate");
                }
            }
            System.out.println("Thread pool shut down successfully");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
