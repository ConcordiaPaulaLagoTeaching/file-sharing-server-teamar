package ca.concordia.server;
import ca.concordia.filesystem.FileSystemManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FileServer - A TCP socket-based file server that handles client connections
 * and processes file system commands.
 * 
 * This server listens on a specified port and accepts multiple client connections
 * sequentially. It supports commands for creating, reading, writing, deleting,
 * and listing files in a simulated file system.
 * 
 * @author Aryan Aggarwal (40215476)
 * @version 1.0
 */
public class FileServer {

    /** File system manager that handles all file operations */
    private FileSystemManager fsManager;
    
    /** Port number on which the server listens for connections */
    private int port;
    
    /**
     * Constructs a new FileServer with the specified configuration.
     * 
     * @param port The port number to listen on (e.g., 12345)
     * @param fileSystemName The name of the file to store the file system data
     * @param totalSize The total size of the file system in bytes
     */
    public FileServer(int port, String fileSystemName, int totalSize){
        // Initialize the FileSystemManager with the disk file and total size
        this.fsManager = new FileSystemManager(fileSystemName, totalSize);
        this.port = port;
    }

    /**
     * Starts the file server and begins listening for client connections.
     * 
     * The server runs in an infinite loop, accepting client connections and
     * processing commands sequentially. Each client connection is handled
     * synchronously before accepting the next connection.
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

            // Main server loop - continuously accept and handle client connections
            while (true) {
                // Wait for and accept a new client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("Handling client: " + clientSocket);
                
                // Set up input/output streams for communication with client
                try (
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    String line;
                    // Process commands from client until connection is closed
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Received from client: " + line);
                        
                        // Parse the command and arguments
                        String[] parts = line.split(" ");
                        String command = parts[0].toUpperCase();

                        try {
                            // Process the command using a switch statement
                            switch (command) {
                                case "CREATE":
                                    // CREATE <filename> - Creates a new empty file
                                    if (parts.length < 2) {
                                        writer.println("ERROR: CREATE requires a filename");
                                        break;
                                    }
                                    fsManager.createFile(parts[1]);
                                    writer.println("SUCCESS: File '" + parts[1] + "' created.");
                                    writer.flush();
                                    break;
                                    
                                case "DELETE":
                                    // DELETE <filename> - Deletes an existing file and frees its blocks
                                    if (parts.length < 2) {
                                        writer.println("ERROR: DELETE requires a filename");
                                        break;
                                    }
                                    fsManager.deleteFile(parts[1]);
                                    writer.println("SUCCESS: File '" + parts[1] + "' deleted.");
                                    writer.flush();
                                    break;
                                    
                                case "READ":
                                    // READ <filename> - Reads and returns the content of a file
                                    if (parts.length < 2) {
                                        writer.println("ERROR: READ requires a filename");
                                        break;
                                    }
                                    String content = fsManager.readFile(parts[1]);
                                    writer.println("SUCCESS: " + content);
                                    writer.flush();
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
                    e.printStackTrace();
                } finally {
                    // Ensure client socket is properly closed
                    try {
                        clientSocket.close();
                    } catch (Exception e) {
                        // Ignore closing errors
                    }
                }
            }
        } catch (Exception e) {
            // Handle server socket creation or binding errors
            e.printStackTrace();
            System.err.println("Could not start server on port " + port);
        }
    }

}
