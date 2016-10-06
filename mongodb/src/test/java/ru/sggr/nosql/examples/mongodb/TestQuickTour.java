package ru.sggr.nosql.examples.mongodb;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author tolya
 * @since 04.10.16.
 */
public class TestQuickTour {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    @BeforeClass
    public static void setup() {
        mongoClient = new MongoClient("localhost", 27017);

        database = mongoClient.getDatabase("mydb");
    }

    @Before
    public void data() {
        MongoCollection<Document> collection = database.getCollection("test");
        /*
         * Add document:
         *      {
         *          "name" : "MongoDB",
         *          "type" : "database",
         *          "count" : 1,
         *          "info" : {
         *              x : 203,
         *              y : 102
         *          }
         *      }
         */
        Document doc1 = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102));
        /*
         * Add document:
         *      {
         *          "name" : "MongoTest",
         *          "type" : "table",
         *          "i"    : 71
         */
        Document doc2 = new Document("name", "MongoTest")
                .append("type", "table")
                .append("i", 71);
        collection.insertMany(Arrays.asList(doc1, doc2));
    }

    @AfterClass
    public static void dispose() {
        mongoClient.close();
    }

    @Test
    public void clientTest() {
        MongoCollection<Document> collection = database.getCollection("test");
        MongoCursor<Document> cursor = collection.find().iterator();
        System.out.println("All records");
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }
    }

    @Ignore
    @Test
    public void filterTest() {
        MongoCollection<Document> collection = database.getCollection("test");
        Document doc = collection.find(eq("i", 71)).first();
        System.out.println("Found records with i=71");
        System.out.println(doc.toJson());
    }
}
