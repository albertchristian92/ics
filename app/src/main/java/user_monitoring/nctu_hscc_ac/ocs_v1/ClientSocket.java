package user_monitoring.nctu_hscc_ac.ocs_v1;

/**
 * Created by AC on 4/13/2018.
 */

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocket extends AsyncTask<String, Void, String> {
    private String serverIP;
    private int serverPort;
    private Socket socket;

    public static boolean connected = false;

    public ClientSocket(String server, int port) {
        serverIP = server;
        serverPort = port;

    }

    public void sendDataString(final String data) {

        if (connected) {
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {

//                        printWriter = new PrintWriter(socket.getOutputStream()); // set the output stream
//                        printWriter.write(data); // send the message through socket
//                        printWriter.flush();

                        DataOutputStream dout=new DataOutputStream(socket.getOutputStream());

                        dout.writeUTF(data);
//                        OutputStream os = socket.getOutputStream();
//                        os.write(data.getBytes());
                        System.out.println("data "+data+"have been sent");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void disconnect(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    if(connected){
                        socket.close();
                        connected = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void checkSocketConnection(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    if(!connected){
                        socket = new Socket(serverIP, serverPort);
                        connected = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @Override
    protected String doInBackground(String... params) {
        try {
            System.out.println("Attempting to connect to " + serverIP + ":" + serverPort);
            socket = new Socket(serverIP, serverPort);
            connected = true;

        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected IO exception: " + ioe.getMessage());
        } catch (Exception fe) {
            System.out.println("Unexpected fatal exception: " + fe);
        }
        // TODO Auto-generated method stub
        return null;
    }

    public Socket getSocket(){
        return this.socket;
    }
}