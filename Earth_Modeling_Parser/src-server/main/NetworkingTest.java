package main;

import networking.ClientServer;
import networking.ServerInformation;
import utils.FileLocations;

public class NetworkingTest {

	public static void main(String[] args) {
		ClientServer cs = new ClientServer(ServerInformation.SERVER_PORT, FileLocations.KEYSTORE_FILE_LOCATION, "password");
		cs.start();
	}
}
