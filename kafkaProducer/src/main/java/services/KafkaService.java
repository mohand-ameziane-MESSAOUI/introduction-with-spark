package services;

import configurations.KafkaConfig;
import entities.Product;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class KafkaService {

    private static Producer<Long, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KafkaConfig.BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    private Producer<Long, String> producer = createProducer();

    protected static ObjectMapper objectMapper = new ObjectMapper();

    public void sending(Product p) throws IOException {
        ProducerRecord<Long, String> record = new ProducerRecord<>(KafkaConfig.TOPIC_PRODUCT,objectMapper.writeValueAsString(p));
        //producer.send(record);
        //System.out.println("send product ==> " + producer.send(record).get());
        try {
            RecordMetadata metadata = producer.send(record).get();
            System.out.println("partition ======> " + metadata.toString());
        }
        catch (ExecutionException | InterruptedException e) {
            System.out.println("Error in sending record");
            System.out.println("exeption : "+e);
        }

    }


}
