package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;
import sun.awt.Mutex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class Node extends AbstractNode  {

    private InetAddress masterNodeIP;
    private int masterNodePort;

    private int localPort;

    private Thread t_runner;
    private boolean busy = false;

    Mutex mutex = new Mutex();


    public Node(String masterNodeAddress, int masterNodePort, int id){

        this.connections = new TreeSet<NodeConnection>();



        try {

            this.inputSocket = new ServerSocket(0);

            this.localPort = inputSocket.getLocalPort();

            this.id = "NODE#" + id + ":" + localPort;
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

        try {

            Socket s = connectMaster();
            send(s, intent, message, null);

        } catch (IOException e) {

            e.printStackTrace();
            log(id, "Couldn't remove connection from master");
        }

        super.end();
    }

    private void registerOnMasterNode() throws IOException {


        log(id, "Registering on master node");

        String intent = Operations.MESSAGE;
        String message = Operations.REGISTER + this.inputSocket.getInetAddress().getHostAddress() + ':' + localPort;

        Socket s = connectMaster();
        send(s, intent, message, null);
    }

    private void registerOnNode(NodeConnection c) throws IOException {

        log(id, "Registering on node" + c.toString());

        String intent = Operations.MESSAGE;
        String message = Operations.REGISTER + this.inputSocket.getInetAddress().getHostAddress() + ':' + localPort;

        Socket s = connectNode(c);
        send(s, intent, message, null);
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

    void executeAlgorithm(Algorithm algorithm){

        log(id, "Starting processing of algorithm " + algorithm.getId() + " in another thread");

        this.t_runner = new Thread(new AlgorithmRunner(algorithm, this));
        t_runner.start();
    }

    void processObject(String operation, Object o, Socket client, ObjectInputStream inputStream) {

        mutex.lock();
            boolean b = this.busy;
        mutex.unlock();

        if (operation.contains(Operations.EXECUTE_GENERATION)){

            if(busy){

                passAlgorithm((Algorithm) o);
            }else{

                executeAlgorithm((Algorithm) o);
            }


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

                        registerOnNode(c);
                    }
                }

                log(id, "Added new " + added + " connections");
                printCurrentNodes();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    synchronized void runnerCallback(AlgorithmRunner runner){

        Algorithm algorithm = runner.getAlgorithm();

        log(id, "Generation #" + algorithm.getStatus() + " processed");


        if(algorithm.getReady()){
            sendAlgorithmToMaster(algorithm);
        }else{
            passAlgorithm(algorithm);
        }

        mutex.lock();
            this.busy = false;
        mutex.unlock();
    }

}
