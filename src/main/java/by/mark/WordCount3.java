package by.mark;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class WordCount3 {

    public static final String INPUT_TOPIC = "streams-plaintext-input";
    public static final String OUTPUT_TOPIC = "streams-word-count-output";
    public static final String APP_ID_CONFIG = "streams-word-count";

    public static void main(String[] args) throws Exception {
        System.setProperty("basePath", "/home/user/Programs/kafka_2.13-2.6.0");

        KafkaUtils.deleteTopic(INPUT_TOPIC);
        KafkaUtils.deleteTopic(OUTPUT_TOPIC);

        KafkaUtils.createTopic(INPUT_TOPIC, false);
        KafkaUtils.createTopic(OUTPUT_TOPIC, true);

        Properties props = getProperties(APP_ID_CONFIG);

        Topology topology = getTopology();
        System.out.println(topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, props);
        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close();
            latch.countDown();
        }));

        try {
            streams.start();
            latch.await();
        } catch (Exception e) {
            System.exit(1);
        }
        System.out.println("Bla-bla");
        System.exit(0);
    }

    private static Topology getTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream(INPUT_TOPIC);

        final KTable<String, Long> counts = source
                .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split(" ")))
                .groupBy((key, value) -> value)
                .count(Materialized.as("count-store"));

        counts.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));

        return builder.build();
    }

    @SuppressWarnings("SameParameterValue")
    private static Properties getProperties(String appIdConfig) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appIdConfig);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        return props;
    }
}

/*
bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic streams-plaintext-input

bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
 --topic streams-word-count-output \
 --from-beginning \
 --formatter kafka.tools.DefaultMessageFormatter \
 --property print.key=true \
 --property print.value=true \
 --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
 --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
 */
