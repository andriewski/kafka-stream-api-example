package by.mark;

import java.io.IOException;

import static java.lang.Runtime.getRuntime;

public class KafkaUtils {

    public static void deleteTopic(String topicName) throws IOException {
        String basePath = System.getProperty("basePath");

        getRuntime()
                .exec(basePath + "/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic " + topicName);
    }

    public static void createTopic(String topicName, boolean compact) throws IOException {
        String basePath = System.getProperty("basePath");

        getRuntime().exec(
                basePath + "/bin/kafka-topics.sh --create" +
                        " --bootstrap-server localhost:9092" +
                        " --replication-factor 1" +
                        " --partitions 1" +
                        " --topic " + topicName +
                        (compact ? " --config cleanup.policy=compact" : "")
        );
    }
}


