package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.*;

public class MasterNode extends AbstractNode {

    String ip;

    private boolean execute = true;
    private final int BUFFER_SIZE = 1024;

    private List<NodeFinderRunner> finderRunners;

    public MasterNode(int port, int id){

        this.connections = new TreeSet<>();
        this.finderRunners = new LinkedList<>();

        try {

            this.inputSocket = new ServerSocket(port);


            this.id = "NODE#" + id + ":" + port;
            log(this.id, "Creating MasterNode Socket on: " + inputSocket.getInetAddress().getHostAddress() + ":" + port);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    boolean isMaster() {
        return true;
    }

    @Override
    void processObject(String operation, Object obj, Socket client, ObjectInputStream inputStream) {

        if(operation.contains(Operations.EXECUTE_GENERATION)){

            // find a new connection to send this algorithm
            Algorithm alg = (Algorithm) obj;

            if(alg.getReady()){

                printResult(alg);

            }else{

                NodeFinderRunner f = new NodeFinderRunner(this, alg);
                finderRunners.add(f);

                Thread t = new Thread(f);
                t.start();
            }
        }else if (operation.contains(Operations.CONNECTION_SHARE)){
            // do nothing
        }
    }

    synchronized void finderCallback(NodeFinderRunner f, Algorithm algorithm){

        finderRunners.remove(f);

        if (algorithm == null){

            log(id, "Sent algorithm");
        }else{

            log(id, "couldn't sent algorithm anywhere!!! - lost");
        }

    }

    public synchronized void startAlgorithm(Algorithm alg){

        log(id, "Starting algorithm#" + alg.getId());
        passAlgorithm(alg);

    }

    public void printResult(Algorithm algorithm){


        String message = "Algorithm " + algorithm.getId() + " finished!";
        log(id, message);

        message += "\n" + algorithm.result();

        String f_message = message;
        Thread t = new Thread(() ->
                JOptionPane.showMessageDialog(null, f_message, "Result", JOptionPane.INFORMATION_MESSAGE)
        );
        t.start();

    }

}
