package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractNode implements Runnable {


    Set<NodeConnection> connections;

    protected boolean execute = true;
    public final int BUFFER_SIZE = 1024;

    ServerSocket inputSocket;

    String id;

    synchronized void processIntent(String intent, ObjectInputStream inputStream, Socket client) throws IOException, ClassNotFoundException {


        log(id, "Processing intent: " + intent);

        if(intent.contains(Operations.MESSAGE)){

            String message = (String) inputStream.readObject();

            log(id, "Message: " + message);

            this.processMessage(message, client);

        }

        if(intent.contains(Operations.OBJECT)){

            String operation = (String) inputStream.readObject();
            Object obj = (Object) inputStream.readObject();

            log(id, "Object with operation: " + operation);

            this.processObject(operation, obj, client);

        }
    }

    abstract void processObject(String operation, Object obj, Socket client);

    abstract void processMessage(String message, Socket client);

    abstract boolean isMaster();


    protected Socket connectNode(NodeConnection c) throws IOException {

        log(id, "Connecting to Node: " + c.toString());
        return new Socket(c.getIp(), c.getPort());
    }

    public synchronized void sendAlgorithm(NodeConnection c, Algorithm alg) throws IOException {


        log(id, "Sending algorithm to node: " + c.toString());

        String intent = Operations.OBJECT;
        String message = Operations.EXECUTE_GENERATION;

        Socket s = connectNode(c);
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

        out.writeObject(intent);
        out.flush();

        out.writeObject(message);
        out.flush();

        out.writeObject(alg);
        out.flush();

        out.close();
        s.close();
    }

    synchronized void registerNode(String address){

        address = address.replaceAll(Operations.REGISTER, "");
        String[] parts = address.split(":");

        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);

        NodeConnection connection = null;

        try {

            connection = new NodeConnection(ip, port);

            log(id, "Node added: " + connection.toString());

            // before adding self, lets send back our connections
            Socket s = connectNode(connection);

            log(id, "Sharing connections");

            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

            String intent = Operations.OBJECT;
            String message = Operations.CONNECTION_SHARE;

            out.writeObject(intent);
            out.flush();

            out.writeObject(message);
            out.flush();

            out.writeObject(connections);

        } catch (UnknownHostException e) {


            log(id, "Unknow host");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.connections.add(connection);

    }

    synchronized void removeNode(String address){

        address = address.replaceAll(Operations.REMOVE, "");
        String[] parts = address.split(":");

        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);

        try {
            NodeConnection comparator = new NodeConnection(ip, port);

            Iterator<NodeConnection> itr = connections.iterator();
            while(itr.hasNext()) {

                NodeConnection c = itr.next();
                if(c.getPort() == comparator.getPort() && c.getIp().equals(comparator.getIp())){
                    connections.remove(c);

                    log(id, "Node removed: " + c.toString());
                }

            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    boolean askBusy(NodeConnection c) throws IOException {


        log(id, "Asking busy status of node: " + c.toString());

        String intent = Operations.MESSAGE;
        String message = Operations.CHECK_BUSY;

        Socket s = connectNode(c);
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

        out.writeObject(intent);
        out.flush();

        out.writeObject(message);
        out.flush();


        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        boolean result = in.readBoolean();

        in.close();
        out.close();
        s.close();

        return result;

    }

    synchronized void passAlgorithm(Algorithm algorithm){


        log(id, "Trying to send algorithm");

        boolean sent = false;

        Iterator<NodeConnection> itr = connections.iterator();
        while(itr.hasNext()) {

            NodeConnection c = itr.next();

            try {

                boolean isBusy = askBusy(c);

                if (isBusy){
                    log(id, "Node " + c.toString() + " is busy");
                    continue;
                }

                log(id, "Node " + c.toString() + " is free");
                sendAlgorithm(c, algorithm);
                sent = true;
                break;

            }catch (IOException e){
                connections.remove(c);
                e.printStackTrace();
            }
        }


        if (!sent && !isMaster()){ // not sent and node

            log(id, "Couldn't send algorithm to any connection, returning to Master Node");
            Node n = (Node) this;
            n.sendAlgorithmToMaster(algorithm);

        }else if (!sent){ // not sent and master

            log(id, "All nodes busy, waiting 5 seconds to send again");
            try {Thread.currentThread().sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
            passAlgorithm(algorithm);
        }
    }


    public synchronized void end(){

        this.execute = false;

        try {

            log(id, "Closing Socket on: " + inputSocket.getInetAddress().getHostAddress() + ":" + inputSocket.getLocalPort());
            this.inputSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void log(String id, String message){

        System.out.println("[" + id + "]" + message);

    }

    @Override
    public void run() {

        while(execute){

            try {

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket dp = new DatagramPacket(buffer, 1024);


                log(id, "Awaiting connections...");
                Socket client = this.inputSocket.accept();

                log(id, "socket conected from: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());

                ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
                String intent = (String) inputStream.readObject();

                this.processIntent(intent, inputStream, client);

                inputStream.close();
                client.close();

            } catch (IOException e) {

                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


    }
}
