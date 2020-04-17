package com.test.services

import java.util.Properties

import com.test.configurations.KafkaConfigs
import org.apache.kafka.clients.consumer.KafkaConsumer

object KafkaConsumerService {


//  val topica = KafkaConfigs.TOPIC_PRODUCT
  //
  //  def consumeFromKafka(topica) = {
  //    val props = new Properties()
  //    props.put("bootstrap.servers", KafkaConfigs.BOOTSTRAP_SERVERS)
  //    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  //    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  //    props.put("auto.offset.reset", "latest")
  //    props.put("group.id", "consumer-group")
  //    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
  //    consumer.
  //      subscribe(util.Arrays.asList(topic))
  //    while (true) {
  //      val record = consumer.poll(1000).asScala
  //      for (data <- record.iterator)
  //        println(data.value())
  //    }


}
