package ru.sggr.nosql.examples.neo4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

/**
 * @author tolya
 * @since 04.10.16.
 */
public class SimpleTest {

    private static GraphDatabaseService graphDb;

    @BeforeClass
    public static void setup() {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File("target/db"));
    }

    @AfterClass
    public static void close() {
        graphDb.shutdown();
    }

    private enum RelTypes implements RelationshipType {
        KNOWS
    }

    @Test
    public void singleTest() {
        Node firstNode;
        Node secondNode;
        Relationship relationship;

        try (Transaction tx = graphDb.beginTx()) {

            firstNode = graphDb.createNode();
            firstNode.setProperty( "message", "Hello, " );
            secondNode = graphDb.createNode();
            secondNode.setProperty( "message", "World!" );

            relationship = firstNode.createRelationshipTo( secondNode, RelTypes.KNOWS );
            relationship.setProperty( "message", "brave Neo4j " );

            System.out.print( firstNode.getProperty( "message" ) );
            System.out.print( relationship.getProperty( "message" ) );
            System.out.print( secondNode.getProperty( "message" ) );

            firstNode.getSingleRelationship( RelTypes.KNOWS, Direction.OUTGOING ).delete();
            firstNode.delete();
            secondNode.delete();

            // Database operations go here
            tx.success();
        }

    }
}
