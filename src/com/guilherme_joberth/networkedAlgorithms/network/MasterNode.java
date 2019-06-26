package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class MasterNode extends AbstractNode {

    private List<NodeFinderRunner> finderRunners;

    public MasterNode(int localPort, int outPort, int id){

        this.connections = new TreeSet<>();
        this.finderRunners = new LinkedList<>();

        this.outputPort = outPort;

        try {

            this.inputSocket = new ServerSocket(localPort);
            this.inputSocket.setReuseAddress(true);

            this.id = "NODE#" + id + ":" + localPort;
            log(this.id, "Creating MasterNode Socket on: " + inputSocket.getInetAddress().getHostAddress() + ":" + localPort);

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
    void processMessage(String message, Socket client, ObjectInputStream inputStream){
    
        if(message.contains(Operations.GET_PUBLIC_ADDRESS)){

            log(id, "On socket " + client.toString() + " received request of public address");

            String[] parts = message.split(":");
            int port = Integer.parseInt(parts[1]);
            String address = client.getInetAddress().getHostAddress();

            try{
            
                NodeConnection connection = new NodeConnection(address, port);

                log(id, "Sending " + connection.toString() + " as answear");
                
                answear(new ObjectOutputStream(client.getOutputStream()), connection.getIp());

            }catch(UnknownHostException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

        }else{

            super.processMessage(message, client, inputStream);

        }
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
