import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

public class KafkaProducerTest {

    private static final String INPUT_TOPIC = "bugs";
    private static final String OUTPUT_TOPIC = "bugs-fts5";

    // adopted from
    // https://github.com/confluentinc/kafka-streams-examples/blob/7.0.1-post/src/main/java/io/confluent/examples/streams/WordCountLambdaExample.java
    public static void main(String[] args) {

        final Properties streamsConfiguration = getStreamsConfiguration();
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> testCaseStream = builder.stream(INPUT_TOPIC);
		final KTable<String, String> fts5TestCases = testCaseStream.filter((k, v) -> v.contains("fts5")).toTable();
		fts5TestCases.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    static Properties getStreamsConfiguration() {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name. The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "fts5");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "fts5-client");
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // Records should be flushed every 10 seconds. This is less than the default
        // in order to keep this example interactive.
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        return streamsConfiguration;
    }

    static void createFts5Stream(final StreamsBuilder builder) {
        final KStream<String, String> testCaseStream = builder.stream(INPUT_TOPIC);
        final KTable<String, String> fts5TestCases = testCaseStream.filter((k, v) -> v.contains("fts5")).toTable();
        fts5TestCases.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }

}
