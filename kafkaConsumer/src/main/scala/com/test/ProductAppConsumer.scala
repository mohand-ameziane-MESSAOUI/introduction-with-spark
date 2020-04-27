package com.test

import com.test.configurations.KafkaConfigs
import com.test.domain.Domains.Product
import com.test.services.KafkaConsumerService
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Encoders, SparkSession}
import org.apache.spark.streaming.{Milliseconds, State, StateSpec, StreamingContext, dstream}

object ProductAppConsumer {

  def main(args: Array[String]): Unit = {

    implicit val encd = Encoders.product[Product]

    println("******************* PROGRAM BEGIN **********************")

    val sparkConf = new SparkConf()
      .setAppName("Kafka_Streaming_Application")
      .setIfMissing("spark.master", "local[*]")
      .set("spark.streaming.backpressure.enabled", "true")
      .set("spark.cassandra.connection.host", KafkaConfigs.CASSANDRA_SERVERS)

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Milliseconds(5000))
    ssc.checkpoint("./chekpoint")

    val stream = KafkaConsumerService.getSetConsumerProperties(KafkaConfigs.BOOTSTRAP_SERVERS, KafkaConfigs.TOPIC_PRODUCT, ssc)
    val keyspaceCassandra = "kafkaspark"

    /**************************************************** stateless ***********************************************************/
    val productDSm = KafkaConsumerService.inputDsmToProductDsm(stream)

    val tableProduct = "productless"
    val queryProductless = "CREATE TABLE IF NOT EXISTS productless (aid int  PRIMARY KEY, " + "aname text, " + "aprice double, " + "aquantity double );"

    KafkaConsumerService.createTable(keyspaceCassandra,queryProductless)
    KafkaConsumerService.insertToCassandra(productDSm, keyspaceCassandra, tableProduct)

    productDSm.print()

    /*************************************************** statelfull *************************************************************/
    val producAndKeytDSm = KafkaConsumerService.inputDsmToProductAndKeyDsm(stream)

    val tableProductFull = "productfull"
    val queryProductfull = "CREATE TABLE IF NOT EXISTS productfull (aid int , " + "aname text PRIMARY KEY, " + "aprice double, " + "aquantity double );"

    val updateState = KafkaConsumerService.updateState()
    val spec = StateSpec.function(updateState)
    val mappedStatefulStream = producAndKeytDSm.mapWithState(spec)

    KafkaConsumerService.createTable(keyspaceCassandra,queryProductfull)

    val prod = mappedStatefulStream.stateSnapshots().map(x => x._2)
    KafkaConsumerService.insertToCassandra(prod, keyspaceCassandra, tableProductFull)

    mappedStatefulStream.print()

    ssc.start() // start the streaming context
    ssc.awaitTermination() // block while the context is running (until it's stopped by the timer)
    ssc.stop() // this additional stop seems to be required

  }
}
