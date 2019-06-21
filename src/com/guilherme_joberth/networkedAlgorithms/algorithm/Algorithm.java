package com.guilherme_joberth.networkedAlgorithms.algorithm;

public interface Algorithm {

    String result();
    String getStatus();
    int getId();
    boolean getReady();

    void execute();

}
