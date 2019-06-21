package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;

import java.io.IOException;
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

        this.id = "MASTER#" + id;


        try {

            this.inputSocket = new ServerSocket(port);
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
    void processMessage(String message, Socket client) {

        if (message.contains(Operations.REGISTER)){

           this.registerNode(message);

        }else if (message.contains(Operations.REMOVE)){

            this.removeNode(message);
        }
    }

    @Override
    void processObject(String operation, Object obj, Socket client) {

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

        passAlgorithm(alg);

    }

    public void printResult(Algorithm algorithm){

        log(id, "Algorithm " + algorithm.getId() + " finished!");

        System.out.println("----------------------------------\nResult: ");
        System.out.println(algorithm.result());
        System.out.println("----------------------------------");

    }

}
