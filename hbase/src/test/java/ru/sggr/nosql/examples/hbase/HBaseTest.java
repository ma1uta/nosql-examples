package ru.sggr.nosql.examples.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * @author asablin
 * @version 1.0
 * @since 05.10.16
 */
public class HBaseTest {
    public static final byte[] SALE_CF = Bytes.toBytes("sale");
    private static final String TABLE_NAME = "test";
    private HBaseTestingUtility testingUtility = new HBaseTestingUtility();

    @Before
    public void before() throws Exception {
        File workingDirectory = new File("./target/hbase");
        Configuration conf = new Configuration();
        System.setProperty("test.build.data", workingDirectory.getAbsolutePath());
        conf.set("test.build.data", new File(workingDirectory, "zookeeper").getAbsolutePath());
        conf.set("fs. default.name", "file:///");
        conf.set("zookeeper.session.timeout", "180000");
        conf.set("hbase.zookeeper.peerport", "2888");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            conf.set(HConstants.HBASE_DIR, new File(workingDirectory, "hbase").toURI().toURL().toString());
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        Configuration hbaseConf = HBaseConfiguration.create(conf);
        testingUtility = new HBaseTestingUtility(hbaseConf);
        try {
            MiniZooKeeperCluster zkCluster = new MiniZooKeeperCluster(conf);
            zkCluster.setDefaultClientPort(2181);
            zkCluster.setTickTime(18000);
            zkCluster.startup(workingDirectory);
            testingUtility.setZkCluster(zkCluster);
            testingUtility.startMiniCluster();
            testingUtility.getHBaseCluster().startMaster();
            testingUtility.createTable(Bytes.toBytes(TABLE_NAME), SALE_CF);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @After
    public void after() throws Exception {
        testingUtility.shutdownMiniCluster();
        testingUtility = null;
    }

    @Test
    public void connectTest() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor[] tables = admin.listTables();
        System.out.printf("Found %d table(s)\n", tables.length);
        for (HTableDescriptor table : tables) {
            System.out.printf("%s\n", table.getNameAsString());
        }
    }

    @Test
    public void simpleTest() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor tableDescriptor = new HTableDescriptor("my-table");
        tableDescriptor.addFamily(new HColumnDescriptor("colfam1"));
        tableDescriptor.addFamily(new HColumnDescriptor("colfam2"));
        tableDescriptor.addFamily(new HColumnDescriptor("colfam3"));
        admin.createTable(tableDescriptor);
        boolean tableAvailable = admin.isTableAvailable("my-table");
        System.out.println("tableAvailable = " + tableAvailable);

        HTable table = new HTable(conf, "my-table");
        Put put = new Put(Bytes.toBytes("row1"));
        put.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1"));
        put.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual2"), Bytes.toBytes("value2"));
        put.add(Bytes.toBytes("colfam2"), Bytes.toBytes("qual1"), Bytes.toBytes("value3"));
        put.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("value1-a"));
        put.add(Bytes.toBytes("colfam3"), Bytes.toBytes("qual1"), Bytes.toBytes("value4"));
        table.put(put);
        table.flushCommits();
        table.close();

        HTable getTable = new HTable(conf, "my-table");
        Get get = new Get(Bytes.toBytes("row1"));
        get.setMaxVersions(3);
        get.addFamily(Bytes.toBytes("colfam1"));
        get.addColumn(Bytes.toBytes("colfam2"), Bytes.toBytes("qual1"));
        Result result = getTable.get(get);
        String row = Bytes.toString(result.getRow());

        // Get a specific value
        String specificValue = Bytes.toString(result.getValue(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1")));
        System.out.println("latest colfam1:qual1 is: " + specificValue);

        // Traverse entire returned row
        System.out.println(row);
        NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = result.getMap();
        for (Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> navigableMapEntry : map.entrySet()) {
            String family = Bytes.toString(navigableMapEntry.getKey());
            System.out.println("\t" + family);
            NavigableMap<byte[], NavigableMap<Long, byte[]>> familyContents = navigableMapEntry.getValue();
            for (Map.Entry<byte[], NavigableMap<Long, byte[]>> mapEntry : familyContents.entrySet()) {
                String qualifier = Bytes.toString(mapEntry.getKey());
                System.out.println("\t\t" + qualifier);
                NavigableMap<Long, byte[]> qualifierContents = mapEntry.getValue();
                for (Map.Entry<Long, byte[]> entry : qualifierContents.entrySet()) {
                    Long timestamp = entry.getKey();
                    String value = Bytes.toString(entry.getValue());
                    System.out.printf("\t\t\t%s, %d\n", value, timestamp);
                }
            }
        }
        getTable.close();

        System.out.println("Scan:");

        HTable scanTable = new HTable(conf, "my-table");
        Scan scan = new Scan();
        try (ResultScanner resultScanner = scanTable.getScanner(scan)) {
            Result next = resultScanner.next();
            while (next != null) {
                System.out.println(Bytes.toString(next.getRow()));
                NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> resultMap = next.getMap();
                for (Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> navigableMapEntry : resultMap.entrySet()) {
                    String family = Bytes.toString(navigableMapEntry.getKey());
                    System.out.println("\t" + family);
                    NavigableMap<byte[], NavigableMap<Long, byte[]>> familyContents = navigableMapEntry.getValue();
                    for (Map.Entry<byte[], NavigableMap<Long, byte[]>> mapEntry : familyContents.entrySet()) {
                        String qualifier = Bytes.toString(mapEntry.getKey());
                        System.out.println("\t\t" + qualifier);
                        NavigableMap<Long, byte[]> qualifierContents = mapEntry.getValue();
                        for (Map.Entry<Long, byte[]> entry : qualifierContents.entrySet()) {
                            Long timestamp = entry.getKey();
                            String value = Bytes.toString(entry.getValue());
                            System.out.printf("\t\t\t%s, %d\n", value, timestamp);
                        }
                    }
                }
                next = resultScanner.next();
            }
        }
    }
}
