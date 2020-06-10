package com.test.services

import com.datastax.driver.core.Cluster
import com.datastax.spark.connector._
import com.fasterxml.jackson.databind.ObjectMapper
import com.test.configurations.KafkaConfigs
import com.test.domain.Domains.{Product, ProductAndNbSales}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{LongDeserializer, StringDeserializer}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{State, StreamingContext}

object KafkaConsumerService {

  val objectMapper = new ObjectMapper()
  def parse(json: String): Product = {
    objectMapper.readValue(json, classOf[Product])
  }

  def getSetConsumerProperties(bootstrapServer: String, topic: String, ssc: StreamingContext): InputDStream[org.apache.kafka.clients.consumer.ConsumerRecord[Long, String]] = {
    println(s"Initializing Properties for ${bootstrapServer}")

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> bootstrapServer,
      "key.deserializer" ->"org.apache.kafka.common.serialization.LongDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    println(s"Starting....Listen to Server ${bootstrapServer}")

    println(s"Waiting Data from topic ==>  ${topic}")

    val topicsSet = Array(topic)

    KafkaUtils.createDirectStream(ssc, PreferConsistent, Subscribe[Long,String](topicsSet,kafkaParams))
  }

  def inputDsmToProductAndKeyDsm(stream: InputDStream[org.apache.kafka.clients.consumer.ConsumerRecord[Long, String]]): org.apache.spark.streaming.dstream.DStream[(String,Product)] = {
    stream
      .map( record =>{
        val product = parse(record.value)
        (product.aname,product)
      }
      );

  }
  def inputDsmToProductDsm(stream: InputDStream[ConsumerRecord[Long, String]]): DStream[Product] = {
    stream
      .map( record => parse(record.value));
  }

  def updateState() : (String, Option[Product], State[Long]) => Option[Any] = {

    val updateState = (productName: String, product: Option[Product], stateNbSales: State[Long]) => {

      (product, stateNbSales.getOption()) match {

        case (Some(product), None) =>
          stateNbSales.update(1)
          Some(productName, stateNbSales)

        case (Some(product), Some(nbSales)) =>
          stateNbSales.update(nbSales + 1)
          Some(productName, stateNbSales)

        case (None, Some(nbSales)) => Some(productName, nbSales)

        case _ => None
      }
    }
    updateState
  }

  def createTable (kesSpace: String ,queryCreateTable: String): Unit = {
        //Creating Cluster object
        val cluster = Cluster.builder.addContactPoint(KafkaConfigs.CASSANDRA_SERVERS).build

        //Creating Session object
        val session = cluster.connect(kesSpace)

        //Executing the query
        session.execute(queryCreateTable)

        System.out.println("Table created")
  }


  def insertToCassandra(ProductAndNbSalesDSm: org.apache.spark.streaming.dstream.DStream[ProductAndNbSales], keyspace: String, table: String): Unit ={
    ProductAndNbSalesDSm.print()
    ProductAndNbSalesDSm.foreachRDD(rdd => {
      if (!rdd.isEmpty()){
        print("======================rdd==========================",rdd)
        rdd.saveToCassandra(keyspace, table)
      }
    })
  }



}
