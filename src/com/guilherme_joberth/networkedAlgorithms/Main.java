package com.guilherme_joberth.networkedAlgorithms;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.GeneticAlgorithm;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.RepeatedRestriction;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.Restriction;
import com.guilherme_joberth.networkedAlgorithms.network.AbstractNode;
import com.guilherme_joberth.networkedAlgorithms.network.MasterNode;
import com.guilherme_joberth.networkedAlgorithms.network.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Main program = new Main();
        program.run();

    }

    List<AbstractNode> created;

    MasterNode masterNode;

    Main(){

        this.created = new LinkedList<>();
    }

    void run(){


        int ids = 1;

        while (true){
            System.out.println("0 - Quit");
            System.out.println("1 - Master Node");
            System.out.println("2 - Node");

            int option = getInt();

            if (option == 0){
                break;
            }else if (option == 1){

                if (masterNode == null) {
                    createMasterNode(ids);
                    ids++;
                }

                while (true){

                    System.out.println("0 - Back");
                    System.out.println("1 - New Genetic Algorithm");

                    option = getInt();

                    if(option == 0) break;
                    else if (option == 1){

                        List<Restriction> restrictions = new LinkedList<>();
                        restrictions.add(new RepeatedRestriction());

                        Algorithm alg = new GeneticAlgorithm(restrictions, ids, 150);
                        System.out.println("[MAIN] Creating algorithm #" + ids);
                        ids++;

                        Thread t = new Thread(() -> masterNode.startAlgorithm(alg));
                        t.start();
                    }


                }

            } else if (option == 2){
                createNode(ids);
                ids++;
            }

        }

        if (created != null){

            for ( AbstractNode n : created) {
                n.end();
            }
        }

    }

    public static int getInt(){

        try{

            return Integer.parseInt(getString());

        }catch (NumberFormatException e){

            e.printStackTrace();

            return getInt();
        }
    }

    static String getString(){

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        try {
            input = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return input;
    }

    private void createMasterNode(int id){

        System.out.println("Input Port: ");
        int inPort = getInt();


        System.out.println("Output Port: ");
        int outPort = getInt();

        MasterNode master = new MasterNode(inPort, outPort, id);
        created.add(master);
        masterNode = master;

        Thread t = new Thread(master);
        t.start();

    }

    private void createNode(int id){

        System.out.println("Master IP: ");
        String address = getString();

        System.out.println("Master Port Number: ");
        int masterPort = getInt();

        System.out.println("Local port: ");
        int localPort = getInt();


        System.out.println("Output port: ");
        int outPort = getInt();

        Node n = new Node(address, masterPort, localPort, outPort, id);
        created.add(n);

        Thread t = new Thread(n);
        t.start();
    }

}
