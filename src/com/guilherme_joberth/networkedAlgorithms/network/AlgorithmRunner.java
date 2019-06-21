package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;

public class AlgorithmRunner implements Runnable {


    private Algorithm algorithm;
    boolean running = false;

    Node callback;

    AlgorithmRunner(Algorithm alg, Node callback){

        this.algorithm = alg;
        this.callback = callback;

    }

    @Override
    public void run() {

        this.algorithm.execute();
        this.callback.runnerCallback(this);

    }

    public synchronized Algorithm getAlgorithm() {
        return algorithm;
    }
}
