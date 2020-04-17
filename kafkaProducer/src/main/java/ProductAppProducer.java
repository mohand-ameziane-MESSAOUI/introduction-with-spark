import configurations.KafkaConfig;
import entities.Product;
import org.apache.log4j.BasicConfigurator;
import services.KafkaService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ProductAppProducer {

    public static void main(String[] args) throws IOException {
        //BasicConfigurator.configure();

        Product product = new Product("10","Samsung","1200",10);

        KafkaService kafkaService = new KafkaService();

        kafkaService.send(product);
    }
}
