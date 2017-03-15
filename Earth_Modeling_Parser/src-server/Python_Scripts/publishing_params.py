# For Http calls
import httplib, urllib, json
# For system tools
import sys
# For reading passwords without echoing
import getpass
# Sources: http://server.arcgis.com/en/server/latest/administer/linux/example-edit-service-properties.htm


# This code was used from the ESRI website, with modifications made for connection parameters
# Defines the entry point into the script
def main(argv):

    # the name of the service to have parameters changed
    service_to_change = argv[0]
    # the server user name
    server_user = argv[1]
    # the server password
    server_pass = argv[2]

	successful = "VEMS SUCCESS: "
	
    # Ask for server name
    serverName = 'proj-se491.iastate.edu'
    serverPort = 6443

    print r"Enter the service name in the format <folder>/<name>.<type>."
    #service = raw_input(r"For example USA/Chicago.MapServer: ")
    service = "EarthModelingTest/" + service_to_change + ".MapServer"
    minInstancesNum = 0
    maxInstancesNum = 8
    maxUsageTime = 15
    maxWaitTime = 30
    maxIdleTime = 60
    capabilities = u"Map"
    
    # Get a token
    token = getToken(server_user, server_pass, serverName, serverPort)
    if token == "":
        print "Could not generate a token with the username and password provided."    
        return
    
    print successful + "Received Token From Server"
	serviceURL = "/arcgis/admin/services/" + service
    
    # This request only needs the token and the response formatting parameter 
    params = urllib.urlencode({'token': token, 'f': 'json'})
    
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
    
    # Connect to service to get its current JSON definition    
    httpConn = httplib.HTTPSConnection(serverName, serverPort)
    httpConn.request("POST", serviceURL, params, headers)
    
    # Read response
    response = httpConn.getresponse()
    if (response.status != 200):
        httpConn.close()
        print "Could not read service information."
        return
    else:
        data = response.read()
        
        # Check that data returned is not an error object
        if not assertJsonSuccess(data):          
            print "Error when reading service information. " + str(data)
        else:
            print "Service information read successfully. Now changing properties..."

        # Deserialize response into Python object
        dataObj = json.loads(data)
        httpConn.close()

        # Edit desired properties of the service
        dataObj["minInstancesPerNode"] = minInstancesNum
        dataObj["maxInstancesPerNode"] = maxInstancesNum
        dataObj["maxUsageTime"] = maxUsageTime
        dataObj["maxWaitTime"] = maxWaitTime
        dataObj["maxIdleTime"] = maxIdleTime
        dataObj["capabilities"] = capabilities
        
        #print dataObj

        # Serialize back into JSON
        updatedSvcJson = json.dumps(dataObj)

        # Call the edit operation on the service. Pass in modified JSON.
        editSvcURL = "/arcgis/admin/services/" + service + "/edit"
        params = urllib.urlencode({'token': token, 'f': 'json', 'service': updatedSvcJson})
        httpConn.request("POST", editSvcURL, params, headers)
        
        # Read service edit response
        editResponse = httpConn.getresponse()
        if (editResponse.status != 200):
            httpConn.close()
            print "Error while executing edit."
            return
        else:
            editData = editResponse.read()
            
            # Check that data returned is not an error object
            if not assertJsonSuccess(editData):
                print "Error returned while editing service" + str(editData)        
            else:
                print "Service edited successfully."

        httpConn.close()  

        return

# A function to generate a token given username, password and the adminURL.
# This code was used from the ESRI website, with small modifications for connection parameters
def getToken(username, password, serverName, serverPort):
    # Token URL is typically http://server[:port]/arcgis/admin/generateToken
    tokenURL = "https://proj-se491.iastate.edu:6443/arcgis/admin/generateToken"
    
    params = urllib.urlencode({'username': username, 'password': password, 'client': 'requestip', 'f': 'json'})
    
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
    
    # Connect to URL and post parameters
    httpConn = httplib.HTTPSConnection(serverName, serverPort)
    httpConn.request("POST", tokenURL, params, headers)
    
    # Read response
    response = httpConn.getresponse()
    print response
    if (response.status != 200):
        httpConn.close()
        print "Error while fetching tokens from admin URL. Please check the URL and try again."
        return
    else:
        data = response.read()
        httpConn.close()
        
        # Check that data returned is not an error object
        if not assertJsonSuccess(data):            
            return
        
        # Extract the token from it
        token = json.loads(data)        
        return token['token']            
        

# A function that checks that the input JSON object 
#  is not an error object.
# This code was used from the ESRI website    
def assertJsonSuccess(data):
    obj = json.loads(data)
    if 'status' in obj and obj['status'] == "error":
        print "Error: JSON object returns an error. " + str(obj)
        return False
    else:
        return True
    
        
# Script start 
if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
