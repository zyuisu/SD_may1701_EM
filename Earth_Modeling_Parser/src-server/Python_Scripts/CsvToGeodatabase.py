# Copyright (C) Kellen Johnson
# This file is part the Visual Earth Modeling System (VEMS).
# VEMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
# VEMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with VEMS. If not, see <http://www.gnu.org/licenses/>.
#
# author Kellen Johnson
#
# attributions from http://pro.arcgis.com/en/pro-app/tool-reference/conversion/excel-to-table.htm for Excel to Table
# attributions from http://pro.arcgis.com/en/pro-app/tool-reference/conversion/table-to-geodatabase.htm for Table to GDB
# attributions from http://gis.stackexchange.com/questions/155923/create-featuredataset-in-geodatabase-with-arcpy-from-a-table for creating empty GDB


import arcpy




def main(argv):

  # To Enable Debug Mode, set this to 1.
  debug = 0

  
  # The directory were Parsed CSVS are kept
  parsed_csv_dir = ""
  # The name of the incoming file (something.csv)
  input_csv_file = ""
  # the main workspace (where all folders are kept)
  arcpy_workspace = ""
  # Where Template MXDs are kept (for symbology reference)
  map_templates_dir = ""
  # Where created mxd's will be kept (finalized and ready to publish)
  map_publishing_dir = ""
  # Where temporary publishing files are kept (.ags and .sd files)
  temp_publishing_dir = ""
  # The name of the template map (where the symbology is copied from)
  template_map_name = ""
  # The name of the blank map mxd
  blank_map = ""
  # Where all of the tables or stored (.dbf files)
  tables_dir = ""
  # Where the GDBs are stored (.gdb)
  auto_gdb_dir = ""
  # Where the created layer can go (for insertion)
  created_layers_dir = ""
  # The name of the layer that will show up in the Legend. (Fancy Unicode!)
  changed_layer_name = ""

  
  if debug == 1:
    parsed_csv_dir = "C:/Users/kj20207/Desktop/Python/tables/"
    input_csv_file = "ch4y2001m4.csv"
    arcpy_workspace = "C:/Users/kj20207/Desktop/Python/"
    map_templates_dir = "C:/Users/kj20207/Desktop/Python/Map_Templates/"
    map_publishing_dir = "C:/Users/kj20207/Desktop/Python/Maps_Publishing/"
    temp_publishing_dir = "C:/Users/kj20207/Desktop/Python/temp_publishing/"
    template_map_name = "CH4TemplateMap.mxd"
    blank_map = "blank_map.mxd"
    tables_dir = "C:/Users/kj20207/Desktop/Python/tables/"
    auto_gdb_dir = "C:/Users/kj20207/Desktop/Python/tables/auto_gdb2/"
    created_layers_dir = "C:/Users/kj20207/Desktop/Python/Created_Layers/"
    changed_layer_name = "fancy unicode"
  else:
    parsed_csv_dir = argv[0]
    input_csv_file = argv[1]
    arcpy_workspace = argv[2]
    map_templates_dir = argv[3]
    map_publishing_dir = argv[4]
    temp_publishing_dir = argv[5]
    template_map_name = argv[6]
    blank_map = argv[7]
    tables_dir = argv[8]
    auto_gdb_dir = argv[9]
    created_layers_dir = argv[10]
    changed_layer_name = argv[11]



  #Define Constants
  arcpy.env.workspace = arcpy_workspace
  x = "longitude"
  y = "latitude"
  # Spatial Reference - Our Spatial Reference has to be this file or the map will not be projected onto the Globe correctly
  spref = "Coordinate Systems\Geographic Coordinate Systems\World\WGS 1984.prj"
  

  # Get the Map File to Use as a Template
  template_mxd = arcpy.mapping.MapDocument(map_templates_dir + template_map_name)
  # Also access the layer to be used as a template
  template_lyr = arcpy.mapping.ListLayers(template_mxd)[0]  
  print "Accessed Template Map for Reference: " + template_map_name

  # Open an instance of a blank Map
  mxd_new = arcpy.mapping.MapDocument(map_templates_dir + blank_map)
  # Access the empty data frame inside of the map
  df_new = arcpy.mapping.ListDataFrames(mxd_new)[0]
  print "Accessed Blank Map for Creating new Map"

  # Create Table from Input file for creating Feature Layer
  arcpy.TableToTable_conversion(parsed_csv_dir + input_csv_file, tables_dir, input_csv_file[0:-4] + ".gdb")
  print "CSV to table completed. Table created is   " + input_csv_file[0:-4] + ".dbf"

  # Create the name of the Feature Layer to be created
  outLayer = input_csv_file[0:-4] + ".lyr"
  # Create an XY Event Layer using the created table and the constant spatial reference
  arcpy.MakeXYEventLayer_management(tables_dir + input_csv_file[0:-4] + ".dbf", x, y, outLayer, spref, None)
  print "Created XY Event Layer to be placed into gdb: " + outLayer

  # Create empty GDB to house Feature Layer Data
  arcpy.CreateFileGDB_management(auto_gdb_dir, input_csv_file[0:-4] + ".gdb", "10.0")
  print "Created empty GDB for XY Layer: " + input_csv_file[0:-4] + ".gdb"

  # Create Feature Layer Data Source using XY Event Layer data and placing into gdb
  arcpy.FeatureClassToFeatureClass_conversion(outLayer, auto_gdb_dir + input_csv_file[0:-4] + ".gdb", input_csv_file[0:-4])
  print "Created Feature Class"
  # Replace Data Source of the template layer
  template_lyr.replaceDataSource(auto_gdb_dir + input_csv_file[0:-4] + ".gdb", "FILEGDB_WORKSPACE", input_csv_file[0:-4], False)
  print "Data Source Successfully Replaced"
  # Save a COPY of the template layer to be imported into the empty data frame of the blank map
  template_lyr.saveACopy(created_layers_dir + input_csv_file[0:-4] + ".lyr")
  print "Creeated a copy of Layer"
  # Acsses the Copied Layer
  addLayer = arcpy.mapping.Layer(created_layers_dir + input_csv_file[0:-4] + ".lyr")
  # Change the actual name of the inner layer (can be funky Unicode if you'd like)
  addLayer.name = changed_layer_name
  print "Referenced Layer to be added"
  # Apply symbology from Reference Layer to the Layer to be added
  arcpy.ApplySymbologyFromLayer_management(addLayer, template_lyr)
  print "Applied Symbology"
  # Add the copied layer to the empty map document into the empty Data frame
  arcpy.mapping.AddLayer(df_new, addLayer, "BOTTOM")
  print "Successfully Added Layer"
  # Set the reference scale of the data frame (won't pixelate on zoom-in at this level)
  df_new.referenceScale = "5000000"
  print "Reference Scale of Data Frame Set"
  # Save a COPY of the map (the map to now be published)
  mxd_new.saveACopy(map_publishing_dir + input_csv_file[0:-4] + ".mxd")
  print "Copy of Map Service Saved"

  # Define local variables
  # Set the publishing files directory
  wrkspc = temp_publishing_dir
  # Access the map to be published
  mapDoc = arcpy.mapping.MapDocument(map_publishing_dir + input_csv_file[0:-4] + ".mxd")

  # Provide path to server which hosts map services
  con =  'https://proj-se491.iastate.edu:6443/arcgis/admin'

  # Provide other service details
  # The name of the service (how it shows up on the server)
  service = input_csv_file[0:-4]
  # name of sddraft (required for publication)
  sddraft =  wrkspc + service + '.sddraft'
  # name of sd (required for publication)
  sd = wrkspc + service + '.sd'
  # Summary (not needed)
  summary = 'General reference map '
  # Tags, not Needed
  tags = 'Test'

  # Create connection file for Publishing
  arcpy.mapping.CreateGISServerConnectionFile ('PUBLISH_GIS_SERVICES', wrkspc, 'CONNECTION.ags', con, 'ARCGIS_SERVER', {True}, {None}, 'may1701', 'earthmodeling', {True})

  # Create service definition draft (sddraft)
  arcpy.mapping.CreateMapSDDraft(mapDoc, sddraft, service, 'FROM_CONNECTION_FILE', wrkspc + '\CONNECTION.ags', True, 'EarthModelingTest', None, None)

  # Analyze the service definition draft (required for publication services)
  analysis = arcpy.mapping.AnalyzeForSD(sddraft)

  # Print errors, warnings, and messages returned from the analysis
  print "The following information was returned during analysis of the MXD:"
  for key in ('messages', 'warnings', 'errors'):
    print '----' + key.upper() + '---'
    vars = analysis[key]
    for ((message, code), layerlist) in vars.iteritems():
      print '    ', message, ' (CODE %i)' % code
      print '       applies to:'

  # Stage and upload the service if the sddraft analysis did not contain errors
  if analysis['errors'] == {}:
      # Execute StageService. This creates the service definition.
      arcpy.StageService_server(sddraft, sd)

      # Execute UploadServiceDefinition. This uploads the service definition and publishes the service.
      arcpy.UploadServiceDefinition_server(sd, wrkspc + '\CONNECTION.ags')
      print "Service successfully published"
  else: 
      print "Service could not be published because errors were found during analysis."

  print arcpy.GetMessages()

if __name__ == "__main__":
   main(sys.argv[1:])
