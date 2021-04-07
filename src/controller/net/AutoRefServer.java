package controller.net;

import controller.SystemClock;
import controller.action.ActionBoard;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class AutoRefServer implements Runnable
{
    Socket connectionSocket;
    BlockingQueue<String> commandQueue;

    BlockingQueue<String> returnCommunicationQueue;

    private int port;

    public AutoRefServer(BlockingQueue<String> commandQueue, BlockingQueue<String> returnCommunicationQueue, int port){
        try{
            this.commandQueue = commandQueue;
            this.returnCommunicationQueue = returnCommunicationQueue;
            this.port = port;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        ServerSocket simulator_socket = null;
        while (true) {
            boolean isConnected;
            try {
                if (simulator_socket != null && connectionSocket != null && simulator_socket.isBound()) {
                    connectionSocket.close();
                    simulator_socket.close();
                }
                simulator_socket = new ServerSocket(port);
                System.out.println("Start listening to AutoReferee connection on port " + port);
                this.connectionSocket = simulator_socket.accept();
                isConnected = true;
                System.out.println("AutoReferee connected");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

                while (isConnected) {
                    while (returnCommunicationQueue.size() > 0) {
                        String returnCommand = returnCommunicationQueue.poll();
                        writer.write(returnCommand);
                        writer.flush();
                    }

                    String data1 = reader.readLine();
                    data1 = data1.trim();
                    String[] values = data1.split(":");

                    //Clock update is handled immediately, everything else is handled by a command queue
                    if (values[1].equals("CLOCK")) {
                        SystemClock.getInstance().setTime(Integer.parseInt(values[2]));
                        this.returnCommunicationQueue.add(values[0] + ":OK\n");
                    } else {
                        this.commandQueue.add(data1);
                    }


                }
                connectionSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
                isConnected = false;
                try {
                    connectionSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
