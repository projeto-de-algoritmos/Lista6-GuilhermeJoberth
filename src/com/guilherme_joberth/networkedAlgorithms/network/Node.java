package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class Node extends AbstractNode  {

    private InetAddress masterNodeIP;
    private int masterNodePort;

    private int localPort;

    private Thread t_runner;
    private boolean busy = false;


    public Node(String masterNodeAddress, int masterNodePort, int id){

        this.connections = new TreeSet<NodeConnection>();

        this.id = "NODE#" + id;

        try {

            this.inputSocket = new ServerSocket(0);

            this.localPort = inputSocket.getLocalPort();
            log(this.id, "Creating Node Socket on " + inputSocket.getInetAddress().getHostAddress() + ":" + this.localPort);


            this.masterNodeIP = InetAddress.getByName(masterNodeAddress);
            this.masterNodePort = masterNodePort;

            this.registerOnMasterNode();

        } catch (SocketException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private Socket connectMaster() throws IOException {
        log(id, "Connecting to master node");
        return new Socket(this.masterNodeIP, this.masterNodePort);
    }

    @Override
    boolean isMaster() {
        return false;
    }

    @Override
    public synchronized void end() {

        log(id, "ending node");

        String intent = Operations.MESSAGE;
        String message = Operations.REMOVE + this.inputSocket.getInetAddress().getHostAddress() + ':' + this.localPort;

        Socket s = null;
        try {

            s = connectMaster();
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

            out.writeObject(intent);
            out.writeObject(message);

        } catch (IOException e) {

            e.printStackTrace();
            log(id, "Couldn't remove connection from master");
        }

        super.end();
    }

    private void registerOnMasterNode() throws IOException {


        log(id, "Registering on master node");

        String intent = Operations.MESSAGE;
        String message = Operations.REGISTER + this.inputSocket.getInetAddress().getHostAddress() + ':' + this.inputSocket.getLocalPort();

        Socket s = connectMaster();
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

        out.writeObject(intent);
        out.flush();

        out.writeObject(message);
        out.flush();

        out.close();

        s.close();
    }

    @Override
    void processMessage(String message, Socket client) {

        if (message.contains(Operations.CHECK_BUSY)){
            answerBusy(client);
        }
    }

    private void answerBusy(Socket client){

        log(id, "Trying to answer busy check");

        try {

            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            out.writeBoolean(this.busy);
            out.close();

        }catch (IOException e){

            e.printStackTrace();
        }
    }

    void sendAlgorithmToMaster(Algorithm alg){

        log(id, "Sending algorithm " + alg.getId() + " directly to master");

        try{
            NodeConnection masterConnection = new NodeConnection(masterNodeIP.getHostAddress(), masterNodePort);

            sendAlgorithm(masterConnection, alg);
        }catch (IOException e){

            log(id, "Couldn't send algorithm " + alg.getId() + " to master");
            e.printStackTrace();

        }

    }

    void processObject(String operation, Object o, Socket client) {

        if (operation.contains(Operations.EXECUTE_GENERATION) && !this.busy){

            this.busy = true;

            Algorithm algorithm = (Algorithm) o;
            log(id, "Starting processing of algorithm " + algorithm.getId() + " in another thread");


            this.t_runner = new Thread(new AlgorithmRunner(algorithm, this));
            t_runner.start();
        }else if (operation.contains(Operations.CONNECTION_SHARE)){

            Set<NodeConnection> received = (Set<NodeConnection>) o;

            log(id, "Received " + received.size() + " shared connections");


            try {
                NodeConnection selfConnection = new NodeConnection(this.inputSocket.getInetAddress().getHostAddress(), localPort);

                int added = 0;

                Iterator<NodeConnection> itr = received.iterator();
                while(itr.hasNext()){

                    NodeConnection c = itr.next();

                    if(!connections.contains(c) && selfConnection.compareTo(c) != 0){
                        connections.add(c);
                        added++;

                        registerNode(c.toString());
                    }
                }

                log(id, "Added new " + added + " connections");

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }

    }

    synchronized void runnerCallback(AlgorithmRunner runner){

            Algorithm algorithm = runner.getAlgorithm();

            log(id, "Generation #" + algorithm.getStatus() + " processed");


            this.busy = false;

            if(algorithm.getReady()){
                sendAlgorithmToMaster(algorithm);
            }else{
                passAlgorithm(algorithm);
            }
    }

}
