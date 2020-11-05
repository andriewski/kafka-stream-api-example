package by.mark;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Pipe1 {

    public static void main(String[] args) {
        Properties props = getProperties("streams-pipe");
        Properties props2 = getProperties("streams-pipe2");

        Topology topology = getTopology();
        System.out.println(topology.describe());

        KafkaStreams streams1 = new KafkaStreams(topology, props);
        KafkaStreams streams2 = new KafkaStreams(topology, props2);
        CountDownLatch latch = new CountDownLatch(2);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams1.close();
            streams2.close();
            latch.countDown();
            latch.countDown();
        }));

        try {
            streams1.start();
            streams2.start();
            latch.await();
        } catch (Exception e) {
            System.exit(1);
        }
        System.out.println("Bla-bla");
        System.exit(0);
    }

    private static Topology getTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> source = builder.stream("streams-plaintext-input");
        source.to("streams-pipe-output");

        return builder.build();
    }

    private static Properties getProperties(String appIdConfig) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appIdConfig);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return props;
    }
}
