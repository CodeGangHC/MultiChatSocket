package multi_chatting_socket;

import java.io.*;
import java.net.*;
import java.util.*;

public class chatServer {
    HashMap clients;
    chatServer(){
        clients = new HashMap();
        Collections.synchronizedMap(clients);
    }

    public void start() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(8888);
            System.out.println("starting server.");
            while(true) {
                socket = serverSocket.accept();
                System.out.println(socket.getInetAddress() + ":" + socket.getPort() + " connected.");
                ServerReceiver thread = new ServerReceiver(socket);
                thread.start(); // run
            }
        } catch (Exception e) {e.printStackTrace();}
    } // start

    void sendToAll(String msg) { // broadcasting
        Iterator iterator = clients.keySet().iterator();
        while(iterator.hasNext()) {
            try {
                DataOutputStream out = (DataOutputStream) clients.get(iterator.next());
                out.writeUTF(msg);
            } catch(IOException e) {e.printStackTrace();}
        }
    } // sendToAll

    public static void main(String[] args) {
        new _MultiChatServer().start();
    }
    // inner class
    class ServerReceiver extends Thread {
        Socket socket; DataInputStream in; DataOutputStream out;
        ServerReceiver(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch(Exception e) {e.printStackTrace();}
        }
        public void run() {
            String name = "";
            try {
                name = in.readUTF();
                if (clients.get(name) != null) { // if name already exists
                    out.writeUTF("Name already exists : " + name);
                    out.writeUTF("Please reconnect with different name.");
                    System.out.println(socket.getInetAddress() + ":" + socket.getPort() + " disconnected.");
                    in.close();
                    out.close();
                    socket.close();
                    socket = null;
                }
                else { // if name doesn't exist
                    sendToAll((name + " joined."));
                    clients.put(name, out);
                    while(in != null) {
                        sendToAll(in.readUTF());
                    }
                }
            } catch(Exception e) {e.printStackTrace();}
            finally {
                if (socket != null) {
                    sendToAll(name + " left the chat.");
                    clients.remove(name);
                    System.out.println(socket.getInetAddress() + ":" + socket.getPort() + " disconnected.");
                } // if
            } // finally
        } // run
    }
}
