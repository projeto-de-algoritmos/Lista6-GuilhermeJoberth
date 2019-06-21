package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;

public class NodeFinderRunner implements Runnable {

    private MasterNode masterNode;
    private Algorithm algorithm;

    NodeFinderRunner(MasterNode master, Algorithm algorithm){

        this.algorithm = algorithm;
        this.masterNode = master;

    }

    @Override
    public void run() {

        try{

            masterNode.passAlgorithm(algorithm);

            algorithm = null;

        }catch (StackOverflowError e){
            e.printStackTrace();

        }

        masterNode.finderCallback(this, algorithm);

    }
}
