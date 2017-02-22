package main;

import networking.ClientServer;
import networking.ServerInformation;

public class NetworkingTest {

	public static void main(String[] args) {
		ClientServer cs = new ClientServer(ServerInformation.SERVER_PORT);
		cs.start();
	}
}
