package ru.sggr.nosql.examples.redis;

import static org.junit.Assert.fail;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author asablin
 * @version 1.0
 * @since 05.10.16
 */
public class ClientTest {

    private static RedisServer server;

    @BeforeClass
    public static void setup() throws IOException {
        server = new RedisServer(6379);
        server.start();
    }

    @AfterClass
    public static void close() {
        server.stop();
    }

    @Test
    public void clientTest() {
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();

        System.out.println("Connected to Redis");

        RedisCommands<String, String> sync = connection.sync();
        sync.set("first", "Hello");
        String value = sync.get("first");
        System.out.println(value);

        RedisAsyncCommands<String, String> async = connection.async();
        RedisFuture<String> futureValue = async.get("first");
        try {
            String asyncValue = futureValue.get();
            System.out.println(asyncValue);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail();
        }

        connection.close();
        redisClient.shutdown();
    }
}
