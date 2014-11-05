import math
import sys
from neo4jrestclient.client import GraphDatabase

DB_URL = "localhost"
GRAPHDB = GraphDatabase("http://{}:7474/db/data/".format(DB_URL))

## This is not an effective way to bulk-add data.
## For entertainment purposes only.
def add_gdf_to_database(graphdb,infile):
	node_results = {}
	edge_results = {}
	for b in [True,False]:
		node_results[b] = 0
		edge_results[b] = 0
	
	with open (infile,'r') as flin:
		nodes = True
		for line in flin:
			if 'nodedef' in line:
				continue
			if 'edgedef' in line:
				nodes = False
				print "Nodes: Success: {}  Failure: {}".format(node_results[True],node_results[False])
				continue
			if nodes:
				add_node(graphdb,line,node_results)
			else:
				add_edge(graphdb,line,edge_results)

	print "Edges: Success: {}  Failure: {}".format(edge_results[True],edge_results[False])

def add_node(graphdb,line,node_results):
	fields = line.strip().split(",")
	call_letters, airport_name, city, country, lat, lon, region, extra = fields
	success = True == graphdb.extensions.Airports.create_new_airport(call_letters=call_letters,airport_name=airport_name,airport_city=city,airport_country=country,airport_lat=lat,airport_lon=lon,airport_region=region)
	node_results[success] +=1


def add_edge(graphdb,line,edge_results):
	fields = line.strip().split(",")
	if len(fields) != 13:
		print fields, len(fields)
	else:
		call_letters_origin, call_letters_destination, seats, passengers, freight, mail, distance, ramp_to_ramp, air_time, carrier_name, year, month, aircraft_type = fields
		success = True == graphdb.extensions.Airports.add_new_flight(call_letters_origin=call_letters_origin,call_letters_destination=call_letters_destination,seats=seats,passengers=passengers,freight=freight,mail=mail,distance=distance,ramp_to_ramp=ramp_to_ramp,air_time=air_time,carrier_name=carrier_name,year=year,month=month,aircraft_type=aircraft_type)
		edge_results[success] +=1


if __name__ == '__main__':
	gdf = "airports_and_flights.gdf"
	add_gdf_to_database(GRAPHDB,gdf)
