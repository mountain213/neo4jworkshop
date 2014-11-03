package airports;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;


/**
 *
 * @author Aaron
 * Code snippets taken from http://neo4j.com/docs/
 */

@Description( "An extension to the Neo4j Server for getting all nodes or relationships" )
public class Airports extends ServerPlugin
{
    private static final String DB_PATH = "/usr/local/neo4j/data/graph.db";
    GraphDatabaseService graphDb;
    
    @Name( "create_new_airport")
    @Description( "Adds a new Airport with optional name and location data" )
    @PluginTarget( GraphDatabaseService.class )
    public boolean createAirport( @Source GraphDatabaseService graphDb,
            @Description( "String; Airport call letters (typically 3 characters).  'Primary Key' for nodes." )
                @Parameter( name = "call_letters" ) String call_letters,
            @Description( "String; Airport name" )
                @Parameter( name = "airport_name" , optional = true ) String airport_name,
            @Description( "String; Airport city" )
                @Parameter( name = "airport_city" , optional = true ) String airport_city,
            @Description( "String; Airport country" )
                @Parameter( name = "airport_country" , optional = true ) String airport_country,
            @Description( "Double; Airport latitude (i.e. 40.639751)" )
                @Parameter( name = "airport_lat" , optional = true ) String airport_lat,
            @Description( "Double; Airport longitude (i.e. -73.778925)" )
                @Parameter( name = "airport_lon" , optional = true ) String airport_lon,
            @Description( "String; Airport region (i.e. America/New_York)" )
                @Parameter( name = "airport_region" , optional = true ) String airport_region
            )
    {
        // Figure out which properties to include
        HashMap<String,String> properties = new HashMap<>();
        if (null != call_letters) {
            properties.put("call_letters", call_letters) ;
        } else {return false;}
        if (null != airport_name) properties.put("airport_name", airport_name) ;
        if (null != airport_city) properties.put("airport_city", airport_city) ;
        if (null != airport_country) properties.put("airport_country", airport_country) ;
        if (null != airport_lat) properties.put("airport_lat", airport_lat) ;
        if (null != airport_lon) properties.put("airport_long", airport_lon) ;
        if (null != airport_region) properties.put("airport_region", airport_region) ;
        
        Node n = null;
        // Start a transaction
        try ( Transaction tx = graphDb.beginTx() )
        {
            // Create the node
            n = graphDb.createNode();
            
            // Add properties to node
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                n.setProperty(key, value);
            }
            
            // Add label to node
            n.addLabel(AirportLabelNames.Airport);
            
            // Everything worked!
            tx.success();
            // Return 'true' = everything worked
            return true;
        } catch (Exception ex) {
            // Return 'false' = not everything worked, transaction rolled back
            return false;
        }
    }
    
    //Tested by shouldCreateAndFindCredentialNode
    @Name( "get_airport_node")
    @Description( "Node; Get an airport node given call_letters" )
    @PluginTarget( GraphDatabaseService.class )
    public Node cypherGetAirportNode( @Source GraphDatabaseService graphDb,
            @Description( "The call letters of the Airport node" )
                @Parameter( name = "call_letters" ) String call_letters)
    {
        // Write the Cypher query to find the airport
        final String cypherQuery = "match (res:Airport {call_letters:{param_call_letters}}) return res";
        
        // Generate parameters to send to cypher
        HashMap<String, Object> params = new HashMap<>();
        params.put( "param_call_letters", call_letters );

        // Generate the engine that will execute the cypher command
        // NOTE: These are expensive to spin up - you'll get much better 
        // performance if you execute multiple queries against the same
        // engine instead of spinning up one per query
        ExecutionEngine engine = new ExecutionEngine( graphDb );
        ExecutionResult result;
        
        // Since we want to reuse this code elsewhere (because of the performance
        // boost from reusing an execution engine) we use another function to get results
        return getSingleNodeWithParams(graphDb, engine, cypherQuery, params);
    }

    /**
     * 
     * @return a single node, or null on error
     * @throws CypherException 
     */
    private Node getSingleNodeWithParams(GraphDatabaseService graphDb, ExecutionEngine engine, final String cypherQuery, HashMap<String, Object> params) throws CypherException {
        ExecutionResult result;
        // Notice that all this work needs to be wrapped in a transaction,
        // even though we really don't care about the transaction
        try ( Transaction ignored = graphDb.beginTx() )
        {
            result = engine.execute( cypherQuery,params);

            // Grab the results column called 'res' (from our cypher query)
            // If you name your results column something else, you need to change this
            // or use a different function!
            Iterator<Node> n_column = result.columnAs( "res" );
            
            // Iterate through the 'rows' of results (in the 'res' column)
            if (n_column.hasNext()){
                Node returnNode = n_column.next();
                //Check if there is a second node 
                //(there shouldn't be - 'primary key' should be unique)
                if (n_column.hasNext()){
                    while (n_column.hasNext()){
                        n_column.next();
                    }
                    System.out.println("Too many results");
                    return null; //This is my angry response - these should have been unique
                } else {
                    return returnNode; //This is my happy response - there was exactly one
                }
            } else {
                System.out.println("No results");
                return null; //This is my angry response - there should have been one
            }
        }
    }
    
    @Name( "add_new_flight")
    @Description( "Adds a flight between two Airports, with optional flight data" )
    @PluginTarget( GraphDatabaseService.class )
    public boolean addFlight( @Source GraphDatabaseService graphDb,
            @Description( "String; Origin airport call letters (typically 3 characters).  'Primary Key' for nodes." )
                @Parameter( name = "call_letters_origin" ) String call_letters_origin,
            @Description( "String; Destination airport call letters (typically 3 characters).  'Primary Key' for nodes." )
                @Parameter( name = "call_letters_destination" ) String call_letters_destination,
            @Description( "Int; Year in which flight took place (i.e. 2014)" )
                @Parameter( name = "year" ,optional=true) Integer year,
            @Description( "Int; Numeric month in which flight took place (i.e. 2 for February)" )
                @Parameter( name = "month",optional=true ) Integer month,
            @Description( "Int; Total number of passenger seats on the plane" )
                @Parameter( name = "seats" , optional = true ) Integer seats,
            @Description( "Int; Number of passenger seats filled on the plane" )
                @Parameter( name = "passengers" , optional = true ) Integer passengers,
            @Description( "Int; Weight of cargo freight on the plane" )
                @Parameter( name = "freight" , optional = true ) Integer freight,
            @Description( "Int; Weight of mail on the plane" )
                @Parameter( name = "mail" , optional = true ) Integer mail,
            @Description( "Int; Distance of flight, rounded to nearest integer" )
                @Parameter( name = "distance" , optional = true ) Integer distance,
            @Description( "Int; Total amount of time elapsed, from ramp to ramp" )
                @Parameter( name = "ramp_to_ramp" , optional = true ) Integer ramp_to_ramp,
            @Description( "Int; Total amount of time elapsed while plane was in the air" )
                @Parameter( name = "air_time" , optional = true ) Integer air_time,
            @Description( "String; Name of the carrier associated with this flight" )
                @Parameter( name = "carrier" , optional = true ) String carrier,
            @Description( "String; The type of aircraft (i.e. 747)" )
                @Parameter( name = "aircraft_type" , optional = true ) String aircraft_type
            
            )
    {
        // Confirm call letters are non-null
        if (null==call_letters_origin || null==call_letters_destination){
            return false;
        }
        
        // Figure out which properties to include
        HashMap<String,String> properties = new HashMap<>();
        if (null != year) properties.put("year", year.toString()) ;
        if (null != month) properties.put("month", month.toString()) ;
        if (null != seats) properties.put("seats", seats.toString()) ;
        if (null != passengers) properties.put("passengers", passengers.toString()) ;
        if (null != freight) properties.put("freight", freight.toString()) ;
        if (null != mail) properties.put("mail", mail.toString()) ;
        if (null != distance) properties.put("distance", distance.toString()) ;
        if (null != ramp_to_ramp) properties.put("ramp_to_ramp", ramp_to_ramp.toString()) ;
        if (null != air_time) properties.put("air_time", air_time.toString()) ;
        if (null != carrier) properties.put("carrier", carrier) ;
        if (null != aircraft_type) properties.put("aircraft_type", aircraft_type) ;
        
        Node origin;
        Node destination;
        
        ExecutionEngine engine = new ExecutionEngine( graphDb );
        
        final boolean performanceMatters = false;

        // If performance is really important, we can repeat a bit of code
        // to allow us to reuse our ExecutionEngine for all of our queries
        // (Because the endpoints create a new one each time we hit them)
        if (!performanceMatters){
        // Get origin and destination nodes.
            origin = cypherGetAirportNode(graphDb, call_letters_origin);
            destination = cypherGetAirportNode(graphDb, call_letters_destination);
        } else { 
            // Cypher query to find an airport
            final String cypherQuery = "match (res:Airport {call_letters:{param_call_letters}}) return res";

            // Generate parameters to send to cypher
            HashMap<String, Object> paramsOrigin = new HashMap<>();
            paramsOrigin.put( "param_call_letters", call_letters_origin );

            HashMap<String, Object> paramsDestination = new HashMap<>();
            paramsOrigin.put( "param_call_letters", call_letters_destination );

            // We could actually use our endpoint
            origin = getSingleNodeWithParams(graphDb, engine, cypherQuery, paramsOrigin);
            destination = getSingleNodeWithParams(graphDb, engine, cypherQuery, paramsDestination);
        }
        
        // Confirm cypher actually found both nodes
        if (null==origin || null==destination){
            return false;
        }
        
        // Start a transaction
        try ( Transaction tx = graphDb.beginTx() )
        {
            // Create the relationship
            Relationship rel = origin.createRelationshipTo(
                    destination, 
                    Airports.FlightTypes.Flight
            );
            
            // Add properties to relationship
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                rel.setProperty(key, value);
            }
            
            // Everything worked!
            tx.success();
            // Return 'true' = everything worked
            return true;
        } catch (Exception ex) {
            // Return 'false' = not everything worked, transaction rolled back
            return false;
        }
    }
    
    
    
    // START SNIPPET: createReltype
    public static enum FlightTypes implements RelationshipType
    {
        Flight;
    }
    
    public static enum AirportLabelNames implements Label {
        Airport;
    }
    
    public static void main( final String[] args )
    {
        System.out.println("Starting Airports");
        Airports hello = new Airports();
        hello.createDb();
        hello.shutDown();
    }
    
    public static void setUniqueConstraints(GraphDatabaseService graphDb){
        try ( Transaction tx = graphDb.beginTx() )
        {
            for (Airports.AirportLabelNames labRes : Airports.AirportLabelNames.values()) {
                graphDb.schema()
                    .constraintFor( labRes )
                    .assertPropertyIsUnique( "call_letters" )
                    .create();
            }
            tx.success();
        }
    }
    
    void createDb()
    {
//        System.out.println("Dropping Existing DB");
        deleteFileOrDirectory( new File( DB_PATH ) );
        // START SNIPPET: startDb
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        registerShutdownHook( graphDb );
        // END SNIPPET: startDb

        
        //Set unique constraints
        setUniqueConstraints(graphDb);
        
    }

    void shutDown()
    {
        
        System.out.println();
        System.out.println( "Shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }

    // START SNIPPET: shutdownHook
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
    // END SNIPPET: shutdownHook

    private static void deleteFileOrDirectory( File file )
    {
        if ( file.exists() )
        {
            if ( file.isDirectory() )
            {
                for ( File child : file.listFiles() )
                {
                    deleteFileOrDirectory( child );
                }
            }
            file.delete();
        }
    }
    
    
    
    
}
//SYS_FIELD_NAME	 FIELD_DESC
//YEAR	Year
//QUARTER	Quarter
//MONTH	Month
//ORIGIN_AIRPORT_ID	Origin Airport, Airport ID. An identification number assigned by US DOT to identify a unique airport.  Use this field for airport analysis across a range of years because an airport can change its airport code and airport codes can be reused.
//ORIGIN_AIRPORT_SEQ_ID	Origin Airport, Airport Sequence ID. An identification number assigned by US DOT to identify a unique airport at a given point of time.  Airport attributes, such as airport name or coordinates, may change over time.
//ORIGIN_CITY_MARKET_ID	Origin Airport, City Market ID. City Market ID is an identification number assigned by US DOT to identify a city market.  Use this field to consolidate airports serving the same city market.
//ORIGIN	Origin Airport
//ORIGIN_CITY_NAME	Origin City
//ORIGIN_STATE_ABR	Origin State Code
//ORIGIN_STATE_FIPS	Origin State FIPS (U.S. Federal Information Processing Standard Codes)
//ORIGIN_STATE_NM	Origin Airport, State Name
//ORIGIN_WAC	Origin Airport, World Area Code
//DEST_AIRPORT_ID	Destination Airport, Airport ID. An identification number assigned by US DOT to identify a unique airport.  Use this field for airport analysis across a range of years because an airport can change its airport code and airport codes can be reused.
//DEST_AIRPORT_SEQ_ID	Destination Airport, Airport Sequence ID. An identification number assigned by US DOT to identify a unique airport at a given point of time.  Airport attributes, such as airport name or coordinates, may change over time.
//DEST_CITY_MARKET_ID	Destination Airport, City Market ID. City Market ID is an identification number assigned by US DOT to identify a city market.  Use this field to consolidate airports serving the same city market.
//DEST	Destination Airport
//DEST_CITY_NAME	Destination City
//DEST_STATE_ABR	Destination State Code
//DEST_STATE_FIPS	Destination State FIPS (U.S. Federal Information Processing Standard Codes)
//DEST_STATE_NM	Destination Airport, State Name
//DEST_WAC	Destination Airport, World Area Code
//UNIQUE_CARRIER	Unique Carrier Code. When the same code has been used by multiple carriers, a numeric suffix is used for earlier users, for example, PA, PA(1), PA(2). Use this field for analysis across a range of years.
//AIRLINE_ID	An identification number assigned by US DOT to identify a unique airline (carrier). A unique airline (carrier) is defined as one holding and reporting under the same DOT certificate regardless of its Code, Name, or holding company/corporation.
//UNIQUE_CARRIER_NAME	Unique Carrier Name. When the same name has been used by multiple carriers, a numeric suffix is used for earlier users, for example, Air Caribbean, Air Caribbean (1).
//UNIQUE_CARRIER_ENTITY	Unique Entity for a Carrier's Operation Region.
//REGION	Carrier's Operation Region. Carriers Report Data by Operation Region
//CARRIER	Code assigned by IATA and commonly used to identify a carrier. As the same code may have been assigned to different carriers over time, the code is not always unique. For analysis, use the Unique Carrier Code.
//CARRIER_NAME	Carrier Name
//CARRIER_GROUP	Carrier Group Code.  Used in Legacy Analysis
//CARRIER_GROUP_NEW	Carrier Group New
//DISTANCE_GROUP	Distance Intervals, every 500 Miles, for Flight Segment
//CLASS	Service Class
//AIRCRAFT_GROUP	Aircraft Group
//AIRCRAFT_TYPE	Aircraft Type
//AIRCRAFT_CONFIG	Aircraft Configuration
//DEPARTURES_SCHEDULED	Departures Scheduled
//DEPARTURES_PERFORMED	Departures Performed
//PAYLOAD	Available Payload (pounds)
//SEATS	Available Seats
//PASSENGERS	Non-Stop Segment Passengers Transported
//FREIGHT	Non-Stop Segment Freight Transported (pounds)
//MAIL	Non-Stop Segment Mail Transported (pounds)
//DISTANCE	Distance between airports (miles)
//RAMP_TO_RAMP	Ramp to Ramp Time (minutes)
//AIR_TIME	Airborne Time (minutes)
