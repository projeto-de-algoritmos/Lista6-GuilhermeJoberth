package com.guilherme_joberth.networkedAlgorithms.network;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;
import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jdk.nashorn.internal.codegen.CompilerConstants;
import org.omg.CORBA.INTERNAL;
import sun.awt.Mutex;
import sun.awt.image.ImageWatched;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

import javax.swing.JTextArea;

public abstract class AbstractNode implements Runnable {

	Set<NodeConnection> connections;

	private boolean execute = true;
	JTextArea textArea;

	ServerSocket inputSocket;

	int outputPort;

	String id;

	Mutex connectionLock = new Mutex();

	synchronized void processIntent(String intent, ObjectInputStream inputStream, Socket client)
			throws IOException, ClassNotFoundException {

		log(id, "Processing intent: " + intent);

		if (intent.contains(Operations.MESSAGE)) {

			String message = (String) inputStream.readObject();

			log(id, "Message: " + message);

			this.processMessage(message, client, inputStream);

		}

		if (intent.contains(Operations.OBJECT)) {

			String operation = (String) inputStream.readObject();
			Object obj = inputStream.readObject();

			log(id, "Object with operation: " + operation);

			this.processObject(operation, obj, client, inputStream);

		}
	}

	abstract void processObject(String operation, Object obj, Socket client, ObjectInputStream inputStream);

	abstract JTextArea getTextArea();
	
	void processMessage(String message, Socket client, ObjectInputStream inputStream) {

		if (message.contains(Operations.REGISTER)) {

			this.registerNode(client, message);

		} else if (message.contains(Operations.REMOVE)) {

			this.removeNode(message);
		}
	}

	abstract boolean isMaster();

	protected Socket connectNode(NodeConnection c) {

		log(id, "Connecting to Node: " + c.toString());

		Socket s = null;

		int tries = 0;
		while (s == null && tries < 3) {
			try {

				s = new Socket();
				s.setReuseAddress(true);

				s.bind(new InetSocketAddress(this.inputSocket.getInetAddress(), this.outputPort));
				s.connect(c.getSocketAddress());

			} catch (BindException e) {
				tries++;
				log(id, "Coludn't connect to " + c.toString() + "," + " bind failed, port " + this.outputPort
						+ "in use, trying again (" + tries + ")");

				if (tries == 3) {
					e.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return s;
	}

	public synchronized void sendAlgorithm(NodeConnection c, Algorithm alg) throws IOException {

		log(id, "Sending algorithm to node: " + c.toString());

		String intent = Operations.OBJECT;
		String message = Operations.EXECUTE_GENERATION;

		Socket s = connectNode(c);
		send(s, intent, message, alg);
	}

	synchronized void send(Socket s, String intent, String message, @Nullable Object o) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

		out.writeObject(intent);
		out.flush();

		out.writeObject(message);
		out.flush();

		if (o != null) {
			out.writeObject(o);
			out.flush();
		}

		out.close();
		s.close();
	}

	synchronized void answear(ObjectOutputStream out, String message) throws IOException {

		out.writeObject(message);
		out.flush();
	}

	synchronized void shareConnections(NodeConnection connection) {

		try {
			send(connectNode(connection), Operations.OBJECT, Operations.CONNECTION_SHARE, this.connections);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	synchronized void registerNode(Socket client, String address) {

		address = address.replaceAll(Operations.REGISTER, "");
		String[] parts = address.split(":");

		log(id, "Registering new connection to: " + client.toString());

		String ip = client.getInetAddress().getHostAddress().toString();
		int port = Integer.parseInt(parts[1]);

		NodeConnection connection = null;

		try {

			connection = new NodeConnection(ip, port);
			this.connections.add(connection);

			log(id, "Node added: " + connection.toString());

			printCurrentNodes();

			shareConnections(connection);

		} catch (UnknownHostException e) {

			log(id, "Unknow host");
			e.printStackTrace();
		}
	}

	synchronized void removeNode(String address) {

		address = address.replaceAll(Operations.REMOVE, "");
		String[] parts = address.split(":");

		String ip = parts[0];
		int port = Integer.parseInt(parts[1]);

		try {
			NodeConnection comparator = new NodeConnection(ip, port);

			Iterator<NodeConnection> itr = connections.iterator();
			while (itr.hasNext()) {

				NodeConnection c = itr.next();
				if (c.getPort() == comparator.getPort() && c.getIp().equals(comparator.getIp())) {
					connections.remove(c);

					log(id, "Node removed: " + c.toString());
				}

			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	synchronized void passAlgorithm(Algorithm algorithm) {

		log(id, "Trying to send algorithm");

		boolean sent = false;

		List<NodeConnection> shuffled = new LinkedList<>(connections);
		Collections.shuffle(shuffled);

		Iterator<NodeConnection> itr = shuffled.iterator();
		while (itr.hasNext()) {

			NodeConnection c = itr.next();

			try {

				log(id, "Sending algorithm to node: " + c.toString());

				String intent = Operations.OBJECT;
				String message = Operations.EXECUTE_GENERATION;

				send(connectNode(c), intent, message, algorithm);
				sent = true;
				break;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!sent && !isMaster()) { // not sent and node

			log(id, "Couldn't send algorithm to any connection, returning to Master Node");
			Node n = (Node) this;
			n.sendAlgorithmToMaster(algorithm);

		} else if (!sent) { // not sent and master

			log(id, "All nodes busy, waiting 5 seconds to send again");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			passAlgorithm(algorithm);
		}
	}

	public synchronized void end() {

		this.execute = false;

		try {
			log(id, "Closing Socket on: " + inputSocket.getInetAddress().getHostAddress() + ":"
					+ inputSocket.getLocalPort());
			this.inputSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void log(String id, String message) {

		System.out.println("[" + id + "]" + message);
		this.getTextArea().append("[" + id + "]" + message + "\n");
	}
	
	synchronized void printCurrentNodes() {
		String str = "";

		for (NodeConnection connection : connections) {
			str += "\n" + connection.toString();
		}
		log(id, "Current connections (" + connections.size() + "):" + str + "\n");

	}
	

	@Override
	public void run() {

		final AbstractNode self = this;

		while (execute) {

			try {

				log(id, "Awaiting connections...");
				connectionLock.lock();
				Socket client = this.inputSocket.accept();

				log(id, "socket conected from: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());

				ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
				String intent = (String) inputStream.readObject();

				self.processIntent(intent, inputStream, client);

				log(id, "Closing socket: " + client.toString());
				client.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				connectionLock.unlock();
			}
		}
	}
}
