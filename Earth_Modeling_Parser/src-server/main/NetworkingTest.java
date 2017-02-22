package main;

import java.io.IOException;

import networking.ClientServer;
import networking.ServerInformation;

public class NetworkingTest {

	public static void main(String[] args) throws IOException {
		ClientServer cs = new ClientServer(ServerInformation.SERVER_PORT);
		cs.start();
	}
}
