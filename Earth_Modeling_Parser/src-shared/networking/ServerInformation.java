/*
 * 
 * Copyright (C) 2017 Anish Kunduru
 * 
 * This file is part the Visual Earth Modeling System (VEMS).
 * 
 * VEMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * VEMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with VEMS. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Anish Kunduru
 * 
 *         Utility class whose sole purpose is to store network configuration information among common classes. It will make it easy to make changes if the server's configuration changes.
 */

package networking;

public class ServerInformation {

	public final static String DAEMON_SERVER_ADDRESS = "clu-vems.eeob.iastate.edu";
	public final static int SERVER_PORT = 1443;

	public final static String ARCGIS_SERVER_NAME = "clu-vems.eeob.iastate.edu";
	public final static String ARCGIS_PUBLISH_URL = "https://clu-vems.eeob.iastate.edu:6443/arcgis/rest/services/EarthModelingTest/";
	public final static String ARCGIS_INNER_SUBSTRING = "/arcgis/admin/services/";
	public final static String ARCGIS_PUBLISHING_SERVICES_SUBFOLDER = "EarthModelingTest";
	public final static String ARCGIS_PUBLISH_ADMIN_FOLDER = "https://clu-vems.eeob.iastate.edu:6443/arcgis/admin";
	public final static String ARCGIS_HTTPS_TOKEN_URL = "https://clu-vems.eeob.iastate.edu:6443/arcgis/admin/generateToken";
	public final static int ARCGIS_SERVER_PORT = 6443;

	//public final static String WEB_SERVER_ADDRESS = "may1701.sd.ece.iastate.edu";
	public final static String WEB_SERVER_ADDRESS = "clu-vems.eeob.iastate.edu";
	//public final static String WEB_SERVER_JAVASCRIPT_DIRECTORY_LOCATION = "/www/VEMS/";
	public final static String WEB_SERVER_JAVASCRIPT_DIRECTORY_LOCATION = "C:\\inetpub\\wwwroot\\";
	//public final static String WEB_SERVER_HOSTKEY = "ssh-ed25519 256 7d:8e:ef:2b:9a:66:dc:98:ff:7e:5d:fc:b7:61:94:69";
	public final static String WEB_SERVER_HOSTKEY = "ssh-ed25519 256 7d:8e:ef:2b:9a:66:dc:98:ff:7e:5d:fc:b7:61:94:69";

	private ServerInformation() {
	};
}
