package main.java.protocols;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import main.java.Interfaces.ServerProtocol;


interface ServerProtocolFactory {
	ServerProtocol create();
}


class GameProtocolFactory implements ServerProtocolFactory {
	public ServerProtocol create() {
		return new GameServerProtocol();
	}
}

class ConnectionHandler implements Runnable {

	private BufferedReader in;
	private PrintWriter out;
	Socket clientSocket;
	ServerProtocol protocol;
	private static final Logger logger = Logger.getLogger("ConnectionHandler");

	public ConnectionHandler(Socket acceptedSocket, ServerProtocol serverProtocol) {
		
		in = null;
		out = null;
		clientSocket = acceptedSocket;
		protocol = serverProtocol;
		logger.info("Accepted connection from client!");
		logger.info("The client is from: " + acceptedSocket.getInetAddress() + ":" + acceptedSocket.getPort());
		try {
			initialize();
		} catch (IOException e) {
			logger.info("Error in initializing I/O");
			e.printStackTrace();
		}
		logger.info("I/O initialized");
		out.println("Welcome to the GameServer!");
	}

	public void run() {

		String msg;
		
		try {
			process();
		} catch (IOException e) {
			System.out.println("Error in I/O");
		}

		System.out.println("Connection closed - bye bye...");
		close();

	}

	public void process() throws IOException {
		String msg;

		while ((msg = in.readLine()) != null) {
			System.out.println("Received \"" + msg + "\" from client");

			protocol.processMessage(msg, v->{
				out.println(v);
			});

			if (protocol.isEnd(msg)) {
				break;
			}

		}
	}

	// Starts listening
	public void initialize() throws IOException {
		// Initialize I/O
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
		}

	// Closes the connection
	public void close() {
		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}

			clientSocket.close();
		} catch (IOException e) {
			System.out.println("Exception in closing I/O");
		}
	}

}

class MultipleClientProtocolServer implements Runnable {
	private ServerSocket serverSocket;
	private int listenPort;
	private ServerProtocolFactory factory;
	private static final Logger logger = Logger.getLogger("MultipleClientProtocolServer");

	public MultipleClientProtocolServer(int port, ServerProtocolFactory p) {
		serverSocket = null;
		listenPort = port;
		factory = p;
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(listenPort);
			logger.info("Listening...");
		} catch (IOException e) {
			logger.info("Cannot listen on port " + listenPort);
		}

		while (true) {
			try {
				ConnectionHandler newConnection = new ConnectionHandler(serverSocket.accept(), factory.create());
				new Thread(newConnection).start();
			} catch (IOException e) {
				logger.info("Failed to accept on port " + listenPort);
				e.printStackTrace();
			}
		}
	}

	// Closes the connection
	public void close() throws IOException {
		serverSocket.close();
	}

	public static void main(String[] args) throws IOException {
		// Get port
		int port = Integer.decode(args[0]).intValue();

		MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new GameProtocolFactory());
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			serverThread.join();
		} catch (InterruptedException e) {
			System.out.println("Server stopped");
		}

	}
}
