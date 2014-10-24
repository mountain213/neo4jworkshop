package REST;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.is;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.test.TestGraphDatabaseFactory;
import airport.Airports;



/**
 *
 * @author Aaron
 * Code snippets taken from http://neo4j.com/docs/
 */
public class RESTAPIEndpointsTest {
    
    protected GraphDatabaseService graphDb;
    
    public RESTAPIEndpointsTest() {
    }
    
//    @BeforeClass
//    public static void setUpClass() {
//    }
//    
//    @AfterClass
//    public static void tearDownClass() {
//    }
    
    @Before
    public void prepareTestDatabase()
    {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Airports.setUniqueConstraints(graphDb);
    }
    
    @After
    public void destroyTestDatabase()
    {
        graphDb.shutdown();
    }

    @Test
    public void startWithConfiguration()
    {
        // START SNIPPET: startDbWithConfig
        GraphDatabaseService db = new TestGraphDatabaseFactory()
            .newImpermanentDatabaseBuilder()
            .setConfig( GraphDatabaseSettings.nodestore_mapped_memory_size, "10M" )
            .setConfig( GraphDatabaseSettings.string_block_size, "60" )
            .setConfig( GraphDatabaseSettings.array_block_size, "300" )
            .newGraphDatabase();
        // END SNIPPET: startDbWithConfig
        db.shutdown();
    }
    

    private boolean assertIterableSize(Iterable iter, int assertSize) {
        ArrayList<Object> list = new ArrayList();
        for (Object n : iter) {
            list.add(n);
        }
        return list.size() == assertSize;
    }
    
    @Test
    public void shouldCreateAirport(){
        Airports airport = new Airports();
        
        //Create an airport with no optional properties
        assert airport.createAirport(graphDb, "AAA", null, null, null, null, null, null);
        //Cannot create two airports with the same id
        assert !airport.createAirport(graphDb, "AAA", null, null, null, null, null, null);
        assert !airport.createAirport(graphDb, "AAA", "bad", null, "bad", null, null, "bad");
        
        //Create airports with different combinations of properties
        assert airport.createAirport(graphDb, "AAB", "hi!", null, null, null, null, null);
        assert airport.createAirport(graphDb, "AAC", "hi!", "hi!", null, null, null, null);
        assert airport.createAirport(graphDb, "AAD", "hi!", "hi!", "hi!", null, null, null);
        assert airport.createAirport(graphDb, "AAE", "hi!", "hi!", "hi!", "hi!", null, null);
        assert airport.createAirport(graphDb, "AAF", "hi!", "hi!", "hi!", "hi!", "hi!", null);
        assert airport.createAirport(graphDb, "AAG", "hi!", "hi!", "hi!", "hi!", "hi!", "hi!");
    }
    
    public void shouldCreateFlight(){
        Airports airport = new Airports();
        
        //Create two airport
        assert airport.createAirport(graphDb, "AAA", null, null, null, null, null, null);
        assert airport.createAirport(graphDb, "AAB", "hi!", null, null, null, null, null);
        
        //Create a flight with no properties
        assert airport.addFlight(graphDb, "AAA", "AAB",null,null,null,null,null,null, null,null,null,null,null);
        
        //You can create as many flights as you want with the same airports
        assert airport.addFlight(graphDb, "AAA", "AAB",null,null,null,null,null,null, null,null,null,null,null);
        assert airport.addFlight(graphDb, "AAA", "AAB",null,null,null,null,null,null, null,null,null,null,null);
        assert airport.addFlight(graphDb, "AAA", "AAB",null,null,null,null,null,null, null,null,null,null,null);
        
        //They can also go the other direction
        assert airport.addFlight(graphDb, "AAB", "AAA",null,null,null,null,null,null, null,null,null,null,null);
        assert airport.addFlight(graphDb, "AAB", "AAA",null,null,null,null,null,null, null,null,null,null,null);
        assert airport.addFlight(graphDb, "AAB", "AAA",null,null,null,null,null,null, null,null,null,null,null);

        //They can also have properties
        assert airport.addFlight(graphDb, "AAB", "AAA",2014,null,null,null,null,null, null,null,null,null,null);
        assert airport.addFlight(graphDb, "AAB", "AAA",2014,2,null,null,null,null, null,null,null,null,null);
        assert airport.addFlight(graphDb, "AAB", "AAA",2014,2,50,null,null,null, null,null,null,null,null);
        assert airport.addFlight(graphDb, "AAB", "AAA",2014,2,50,50,50,500, 500,24,20,"Cool Airwayz, Inc.","747");
        
        //Flights have to be to and from valid Airports
        assert !airport.addFlight(graphDb, "AAA", "ZZZ",2014,2,50,50,50,500, 500,24,20,"Cool Airwayz, Inc.","747");
        assert !airport.addFlight(graphDb, "ZZZ", "AAB",2014,2,50,50,50,500, 500,24,20,"Cool Airwayz, Inc.","747");
        assert !airport.addFlight(graphDb, "ZZZ", "ZZZ",2014,2,50,50,50,500, 500,24,20,"Cool Airwayz, Inc.","747");
        assert !airport.addFlight(graphDb, null, "AAA",2014,2,50,50,50,500, 500,24,20,"Cool Airwayz, Inc.","747");
        assert !airport.addFlight(graphDb, "AAA", null,2014,2,50,50,50,500, 500,24,20,"Cool Airwayz, Inc.","747");
        assert !airport.addFlight(graphDb, null, null,2014,2,50,50,50,500, 500,24,20,"Cool Airwayz, Inc.","747");
    }
    
    @Test
    public void shouldCreateNode()
    {
        // START SNIPPET: unitTest
        Node n = null;
        try ( Transaction tx = graphDb.beginTx() )
        {
            n = graphDb.createNode();
            n.setProperty( "name", "Nancy" );
            tx.success();
        }

        // The node should have a valid id
        assertThat( n.getId(), is( greaterThan( -1L ) ) );

        // Retrieve a node by using the id of the created node. The id's and
        // property should match.
        try ( Transaction tx = graphDb.beginTx() )
        {
            Node foundNode = graphDb.getNodeById( n.getId() );
            assertThat( foundNode.getId(), is( n.getId() ) );
            assertThat( (String) foundNode.getProperty( "name" ), is( "Nancy" ) );
        }
        // END SNIPPET: unitTest
        
    }
    
}
