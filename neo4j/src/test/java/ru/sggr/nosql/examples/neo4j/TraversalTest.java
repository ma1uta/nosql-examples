package ru.sggr.nosql.examples.neo4j;


import static java.lang.System.out;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

import java.io.File;

/**
 * @author tolya
 * @since 04.10.16.
 */
public class TraversalTest {

    private static GraphDatabaseService db;
    private static TraversalDescription friendsTraversal;

    private static final File DB_PATH = new File("target/neo4j-traversal-example");

    @BeforeClass
    public static void setup() {
        db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        friendsTraversal = db.traversalDescription()
                .depthFirst()
                .relationships(Rels.KNOWS)
                .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
    }

    @Test
    public void traversalTest() {
        run(createData());
    }

    private static Node createData() {
        String query = "CREATE (joe {name: 'Joe'}), (sara {name: 'Sara'}), "
                + "(lisa {name: 'Lisa'}), (peter {name: 'PETER'}), (dirk {name: 'Dirk'}), "
                + "(lars {name: 'Lars'}), (ed {name: 'Ed'}),"
                + "(joe)-[:KNOWS]->(sara), (lisa)-[:LIKES]->(joe), "
                + "(peter)-[:KNOWS]->(sara), (dirk)-[:KNOWS]->(peter), "
                + "(lars)-[:KNOWS]->(drk), (ed)-[:KNOWS]->(lars), "
                + "(lisa)-[:KNOWS]->(lars) "
                + "RETURN joe";
        Result result = db.execute(query);
        Object joe = result.columnAs("joe").next();
        if (joe instanceof Node) {
            return (Node) joe;
        } else {
            throw new RuntimeException("Joe isn't a node!");
        }
    }

    private static void run(Node joe) {
        try (Transaction ignored = db.beginTx()) {
            out.println(knowsLikesTraverser(joe));
            out.println(traverseBaseTraverser(joe));
            out.println(depth3(joe));
            out.println(depth4(joe));
            out.println(nodes(joe));
            out.println(relationships(joe));
        }
    }

    public static String knowsLikesTraverser(Node node) {
        String output = "";
        // START SNIPPET: knowslikestraverser
        for (Path position : db.traversalDescription()
                .depthFirst()
                .relationships(Rels.KNOWS)
                .relationships(Rels.LIKES, Direction.INCOMING)
                .evaluator(Evaluators.toDepth(5))
                .traverse(node)) {
            output += position + "\n";
        }
        // END SNIPPET: knowslikestraverser
        return output;
    }

    public static String traverseBaseTraverser(Node node) {
        String output = "";
        // START SNIPPET: traversebasetraverser
        for (Path path : friendsTraversal.traverse(node)) {
            output += path + "\n";
        }
        // END SNIPPET: traversebasetraverser
        return output;
    }

    public static String depth3(Node node) {
        String output = "";
        // START SNIPPET: depth3
        for (Path path : friendsTraversal
                .evaluator(Evaluators.toDepth(3))
                .traverse(node)) {
            output += path + "\n";
        }
        // END SNIPPET: depth3
        return output;
    }

    public static String depth4(Node node) {
        String output = "";
        // START SNIPPET: depth4
        for (Path path : friendsTraversal
                .evaluator(Evaluators.fromDepth(2))
                .evaluator(Evaluators.toDepth(4))
                .traverse(node)) {
            output += path + "\n";
        }
        // END SNIPPET: depth4
        return output;
    }

    public static String nodes(Node node) {
        String output = "";
        // START SNIPPET: nodes
        for (Node currentNode : friendsTraversal
                .traverse(node)
                .nodes()) {
            output += currentNode.getProperty("name") + "\n";
        }
        // END SNIPPET: nodes
        return output;
    }

    public static String relationships(Node node) {
        String output = "";
        // START SNIPPET: relationships
        for (Relationship relationship : friendsTraversal
                .traverse(node)
                .relationships()) {
            output += relationship.getType().name() + "\n";
        }
        // END SNIPPET: relationships
        return output;
    }

    // START SNIPPET: sourceRels
    private enum Rels implements RelationshipType {
        LIKES, KNOWS
    }
    // END SNIPPET: sourceRels
}
