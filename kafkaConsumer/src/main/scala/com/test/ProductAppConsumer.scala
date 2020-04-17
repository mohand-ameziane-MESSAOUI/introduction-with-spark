package com.test

import java.util.{Collections, _}

import com.fasterxml.jackson.databind.ObjectMapper
import com.test.configurations.KafkaConfigs
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

import scala.collection.JavaConverters._

object ProductAppConsumer {

  val objectMapper = new ObjectMapper()
  //objectMapper.registerModule(DefaultScalaModule)

  def parse(json:String): Product= {
    objectMapper.readValue(json, classOf[Product])
  }

  def main(args: Array[String]): Unit = {
    println("******************* PROGRAM BEGIN **********************")

//     val sparkConf = new SparkConf()
//    .setAppName("Introduction to Spark Streaming with Kafka")
//    sparkConf.setIfMissing("spark.master", "local[*]")
//    sparkConf.set("spark.streaming.backpressure.enabled", "true")

    val sparkConf = new SparkConf()

    sparkConf
        .setAppName("Histogram")
        .setMaster("local[*]")

    val spark = SparkSession.builder()
      .master("local[*]")
      .config(sparkConf)
      .getOrCreate()

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Milliseconds(500))
    ssc.checkpoint("./chekpoint")




    println(s"Starting....Listen to ${KafkaConfigs.BOOTSTRAP_SERVERS}")

    val  props = new Properties()
    props.put("bootstrap.servers", KafkaConfigs.BOOTSTRAP_SERVERS)

    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("group.id", "something")

    val consumer = new KafkaConsumer[String, String](props)

    consumer.subscribe(Collections.singletonList(KafkaConfigs.TOPIC_PRODUCT))

    while(true){
      val records=consumer.poll(100)
      for (record<-records.asScala){
        println(record)
      }
    }




    //stream.foreachRDD(elm => println(parse(elm.toString())))

    println("******************* PROGRAM END ************************")


  }
}
