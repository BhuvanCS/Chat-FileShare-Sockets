package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 49999;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new thread to handle client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Error in the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Broadcast message to all connected clients
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Remove disconnected client
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String username = in.readLine();
            System.out.println(username + " has joined.");

            // Notify other clients about the new connection
            Server.broadcastMessage(username + " has joined.", this);

            // Read messages from client and handle them
            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("file: ")) {
                    // File transfer request
                    String filename = input.substring(6).trim();
                    receiveFile(filename);
                    Server.broadcastMessage("file:"+ username + ": " + input, this);
                    //Server.broadcastMessage("file:" input, this);
                } else {
                    // Regular message
                    Server.broadcastMessage(username + ": " + input, this);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } catch (Exception e) {
			// TODO Auto-generated catch block
        	System.out.println("Couldnt recieve file: "+ e.getMessage());
		} finally {
//            try {
//                //socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            // Remove this client from the list
            Server.removeClient(this);
            System.out.println("Client disconnected: " + socket);
        }
    }

    // Send a message to this client
    public void sendMessage(String message) {
        out.println(message);
    }

    // Method to receive a file from the client
    private void receiveFile(String filename) throws Exception {
    	File file = new File(filename);
    	 FileInputStream fis = new FileInputStream(file); 
    	 BufferedInputStream bis = new BufferedInputStream(fis); 
    	 OutputStream os = socket.getOutputStream(); 
    	 
    	 byte[] contents;
    	 long fileLength = file.length();
    	 long current = 0;
    	 long start = System.nanoTime(); 
    	 
    	 while(current!=fileLength){
    		 int size = 10000;
    		 if(fileLength - current >= size)
    			 current += size;
    		 else{
    			 size = (int)(fileLength - current);
    			 current = fileLength;
    			 }
    		 contents = new byte[size];
    		 bis.read(contents, 0, size);
    		 os.write(contents);
    		 System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
    		 }
    	 //os.flush();
    	 
//        try (BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
//             FileOutputStream fos = new FileOutputStream(filename)) {
//
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = bis.read(buffer)) != -1) {
//                fos.write(buffer, 0, bytesRead);
//            }
//            System.out.println("File received: " + filename);
//        } catch (IOException e) {
//            System.err.println("Error receiving file: " + e.getMessage());
//        }
    }
}
