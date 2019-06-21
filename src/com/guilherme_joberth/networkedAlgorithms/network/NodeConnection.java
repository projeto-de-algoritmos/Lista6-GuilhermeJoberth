package com.guilherme_joberth.networkedAlgorithms.network;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeConnection implements Serializable, Comparable {

    private InetAddress ip;
    private int port;

    NodeConnection(String address, int port) throws UnknownHostException {

        this.ip = InetAddress.getByName(address);
        this.port = port;

    }

    public String getIp() {

        return this.ip.getHostAddress();
    }

    public int getPort() {
        return port;
    }

    public String toString(){
        return getIp() + ":" + getPort();
    }

    @Override
    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }
}
