# Copyright (C) Kellen Johnson
# This file is part the Visual Earth Modeling System (VEMS).
# VEMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
# VEMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with VEMS. If not, see <http://www.gnu.org/licenses/>.

#author Kellen Johnson

# attributions from http://pro.arcgis.com/en/pro-app/tool-reference/conversion/excel-to-table.htm for Excel to Table
# attributions from http://pro.arcgis.com/en/pro-app/tool-reference/conversion/table-to-geodatabase.htm for Table to GDB
# attributions from http://gis.stackexchange.com/questions/155923/create-featuredataset-in-geodatabase-with-arcpy-from-a-table for creating empty GDB

#import necessary modules
import arcpy
import os
import sys

def main(argv):
	# set workspace (where the files to be converted are)
	#arcpy.env.workspace = "C:/Users/kj20207/Desktop/Python/tables"
	# the name of the input file to be converted. MUST BE EXCEL FILE
	#input_file = "ch4y2001m2"
	# the output directory (where the gdb's will be located) + the input_file name with extension
	#directory = "C:/Users/kj20207/Desktop/Python/tables/auto_gdb"
	workspace = '' 
	directory = ''
	input_file = ''
	tables = ''

	workspace = argv[0]
	directory = argv[1]
	input_file = argv[2]
	tables = argv[3]

	arcpy.env.workspace = workspace

	# parsed CSV - ch4y2001m0.csv (now ch4y2001m0.xls or .xlsx)
	# CSV's DO NOT WORK
	# create's a file with a .dbf extension, not sure why it's expressed as gdb in the parameter name....
	# this file will be passed along to be converted to the gdb
	# all of our csv's will be single sheets, always use name of file as sheet name
	# POSSIBLE EXCEPTIONS
	#   cannot find Excel file
	#   if the Table already exists, will crash
	#arcpy.ExcelToTable_conversion(input_file + ".xlsx", input_file + ".gdb", input_file)
	#print "Converted Excel File   " + input_file + "  to Table   " + input_file + ".dbf"

	# Table to Table attempt
	arcpy.TableToTable_conversion(input_file, tables, input_file[0:-4] + ".gdb")
	print "CSV to table completed. Table created is   " + input_file[0:-4] + ".dbf"

	# create an empty gdb file
	# Table to GDB will not create a file for us. It must already exist.
	# This will create an empty gdb for us
	arcpy.CreateFileGDB_management(directory, input_file[0:-4] + ".gdb", "10.0")
	print "Created empty gdb for " + input_file[0:-4]


	# implicitly define output folder for geodatabases and set table to convert
	arcpy.TableToGeodatabase_conversion(tables + input_file[0:-4] + ".dbf", directory + input_file[0:-4] + ".gdb")
	print "Converted Table File   " + input_file[0:-4] + ".dbf  to GeoDatabase   " + input_file[0:-4] + ".gdb"
	print "Geodatabases are stored in directory with extension '" + directory + "'"

if __name__ == "__main__":
   main(sys.argv[1:])
