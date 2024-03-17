package client;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 49999;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            out.println(username); // Send the username to the server

            // Start a thread to handle incoming messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                    	 if (message.startsWith("file:")) {
                             // Handle file transfer notification separately
                             String filename = "sampledownload"+Math.floor(Math.random()*100)+".txt";
                             receiveFile(filename, socket);
                         } else {
                             // Regular chat message
                             System.out.println(message);
                         }
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving message from server: " + e.getMessage());
                } catch (Exception e) {
					// TODO Auto-generated catch block
                	System.err.println("Error in downloading file: "+e.getMessage());
					e.printStackTrace();
				}
            }).start();

            // Read user input and send messages or files to the server
            String userInputStr;
            while ((userInputStr = userInput.readLine()) != null) {
                if (userInputStr.startsWith("file:")) {
                    // File transfer request
                    String filePath = userInputStr.substring(5).trim();
                    sendFile(filePath, socket);
                } else {
                    // Regular message
                    out.println(userInputStr);
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    // Method to send a file to the server
    private static void sendFile(String filePath, Socket socket) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             OutputStream outputStream = socket.getOutputStream()) {

            // Send the filename
            String filename = new File(filePath).getName();
            outputStream.write(("file: " + filename + "\n").getBytes());

            // Send the file contents
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            //outputStream.flush();
            System.out.println("File sent successfully: " + filename);
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
        }
    }
    
    private static void receiveFile(String filename, Socket socket) throws Exception {
    	byte[] contents = new byte[10000];
    	FileOutputStream fos = new FileOutputStream(filename);
    	 BufferedOutputStream bos = new BufferedOutputStream(fos); 
    	 InputStream is = socket.getInputStream(); 
    	 int bytesRead = 0;
    	 while((bytesRead=is.read(contents))!=-1)
    		 bos.write(contents, 0, bytesRead);
    	 System.out.println("Finished Writing");
    	 //bos.flush();
    	 
    	 System.out.println("File saved succesfully!");
    	 
//        try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
//             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
//
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            InputStream is = socket.getInputStream(); 
//            while ((bytesRead = is.read(buffer)) != -1) {
//                bufferedOutputStream.write(buffer, 0, bytesRead);
//            }
//            bufferedOutputStream.flush();
//            System.out.println("File received successfully: " + filename);
//        } catch (IOException e) {
//            System.err.println("Error receiving file: " + e.getMessage());
//        }
    }
    
}
