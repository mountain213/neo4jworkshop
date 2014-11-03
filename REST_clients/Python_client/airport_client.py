from neo4jrestclient.client import GraphDatabase

## Connect to the database - replace localhost with 
## the location of your server if it isn't running locally
gdb = GraphDatabase("http://localhost:7474/db/data/")

print "These are the extensions you can work with:"
print gdb.extensions.Airports

origin = "PQR"
destination = "LMN"

## Create two airports
## (The create_new_airport endpoint returns a boolean
## representing the success or failure of the call)
if gdb.extensions.Airports.create_new_airport(call_letters=origin):
	print "Created Airport {}".format(origin)
else:
	print "ISSUE: Airport {} was not successfully created - perhaps those call letters are already in use".format(origin)

if gdb.extensions.Airports.create_new_airport(call_letters=destination):
	print "Created Airport {}".format(destination)
else:
	print "ISSUE: Airport {} was not successfully created - perhaps those call letters are already in use".format(destination)

## Add a flight between the two airports
## (The add_new_flight endpoint returns a boolean
## representing the success or failure of the call)
if gdb.extensions.Airports.add_new_flight(call_letters_origin=origin,call_letters_destination=destination):
	print "Added a flight from {} to destination {}".format(origin,destination)
else:
	print "ISSUE: No flight was added from {} to {}".format(origin,destination)

## You can also return node and relationship objects from Neo4j
try:
	origin_node = gdb.extensions.Airports.get_airport_node(call_letters=origin)
	print "I've captured the node for {}".format(origin)
	print origin_node
except:
	"Node couldn't be captured"
