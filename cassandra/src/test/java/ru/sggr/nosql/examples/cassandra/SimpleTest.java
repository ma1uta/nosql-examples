package ru.sggr.nosql.examples.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author tolya
 * @since 04.10.16.
 */
public class SimpleTest {

    @Before
    public void before() throws TTransportException, IOException, InterruptedException, ConfigurationException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("simple-cassandra.yaml");
    }

    @Test
    public void simpleClient() {

        // Connect to the cluster and keyspace "demo"

        Cluster cluster = Cluster.builder().withClusterName("TestCluster").addContactPoint("127.0.0.1").build();
        Session newSession = cluster.connect();
        newSession.execute("CREATE KEYSPACE IF NOT EXISTS  demo WITH replication = {"
                + " 'class': 'SimpleStrategy', "
                + " 'replication_factor': '3' "
                + "};" );
        newSession.close();

        Session session = cluster.connect("demo");

        session.execute("create table demo.users (lastname text, firstname text, age int, city text, email text, primary key (lastname))");
        // Insert one record into the users table
        session.execute(
                "INSERT INTO users (lastname, firstname, age, city, email) VALUES ('Jones', 'Bob', 35, 'Austin', 'bob@example.com')");

        // Use select to get the user we just entered
        ResultSet results = session.execute("SELECT * FROM users WHERE lastname='Jones'");
        for (Row row : results) {
            System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
        }

        // Update the same user with a new age
        session.execute("update users set age = 36 where lastname = 'Jones'");

        // Select and show the change
        results = session.execute("select * from users where lastname='Jones'");
        for (Row row : results) {
            System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
        }

        // Delete the user from the users table
        session.execute("DELETE FROM users WHERE lastname = 'Jones'");

        // Show that the user is gone
        results = session.execute("SELECT * FROM users");
        for (Row row : results) {
            System.out
                    .format("%s %d %s %s %s\n", row.getString("lastname"), row.getInt("age"), row.getString("city"), row.getString("email"),
                            row.getString("firstname"));
        }

        // Clean up the connection by closing it
        cluster.close();
    }
}
