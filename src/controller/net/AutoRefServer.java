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

    public AutoRefServer(Socket connectionSocket, BlockingQueue<String> commandQueue){
        try{
            System.out.println("Client Got Connected  " );
            this.commandQueue = commandQueue;
            this.connectionSocket = connectionSocket;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

            // TODO - Always listen until connection is terminated
            for (int i = 0; i < 100; i++){
                String data1 = reader.readLine().trim();
                String[] values = data1.split(":");

                switch (values[0]){
                    case "CLOCK":
                        SystemClock.getInstance().setTime(Integer.parseInt(values[1]));
                        break;
                    case "STATE":
                        switch(values[1]){
                            case "READY":
                                System.out.println("Before Ready");
                                ActionBoard.ready.actionPerformed(null);
                                System.out.println("After Ready");
                                break;
                            case "SET":
                                ActionBoard.set.actionPerformed(null);
                                break;
                            case "PLAY":
                                ActionBoard.play.actionPerformed(null);
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }

                System.out.println("Got Command: " + data1);
//                this.commandQueue.add(data1);
                System.out.println("In queue.");
                writer.write("Ok\n");
                writer.flush();


            }

            connectionSocket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}