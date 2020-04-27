import entities.Product;
import services.KafkaService;

import java.io.IOException;
import java.util.ArrayList;

public class ProductAppProducer {

    public static void main(String[] args) throws IOException {
        //BasicConfigurator.configure();

        //val name = Seq("Samsung", "Apple", "Condor")

        ArrayList <Product> products = new ArrayList<Product>();
        products.add(new Product(1,"Apple Iphone 11",1500,10));
        products.add(new Product(2,"Apple Iphone 11",1400,8));
        products.add(new Product(3,"Nokia 3310",100,9));
        products.add(new Product(4,"Apple Iphone 11 Pro",1300,11));
        products.add(new Product(5,"Nokia 3310",750,11));


        KafkaService kafkaService = new KafkaService();
        products.forEach(p -> {
            try {
                kafkaService.sending(p);
                Thread.sleep(4000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });


    }
}
