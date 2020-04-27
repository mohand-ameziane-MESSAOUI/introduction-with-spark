package com.test.services

import com.datastax.driver.core.Cluster
import com.datastax.spark.connector._
import com.fasterxml.jackson.databind.ObjectMapper
import com.test.configurations.KafkaConfigs
import com.test.domain.Domains.Product
import org.apache.kafka.common.serialization.{LongDeserializer, StringDeserializer}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{State, StreamingContext}

object KafkaConsumerService {

  val objectMapper = new ObjectMapper()
  // objectMapper.registerModule(DefaultScalaModule)

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

    org.apache.spark.streaming.kafka010.KafkaUtils.createDirectStream(ssc, PreferConsistent, Subscribe[Long,String](topicsSet,kafkaParams))
  }

  def inputDsmToProductAndKeyDsm(stream: InputDStream[org.apache.kafka.clients.consumer.ConsumerRecord[Long, String]]): org.apache.spark.streaming.dstream.DStream[(String,Product)] = {
    stream
      .map( record =>{
        val product = parse(record.value)
        (product.aname,product)
      }
      );

  }

  def inputDsmToProductDsm(stream: InputDStream[org.apache.kafka.clients.consumer.ConsumerRecord[Long, String]]): org.apache.spark.streaming.dstream.DStream[Product] = {
    stream
      .map( record =>{
        parse(record.value)
      }
      );

  }

  def updateState() : (String, Option[Product], State[Product]) => Any = {

    val updateState = (productName: String, product: Option[Product], stateProduct: State[Product]) => {

      val defaultProduct = Product(0,productName,Double.MaxValue,0)

      val prevMinPrice: Option[Product] = stateProduct.getOption()

      val productMin : Product = product.getOrElse(defaultProduct)

      (prevMinPrice, productMin) match {

        case (None,minPrice) => {
          println(s">>> -----------------1---------------- <<<")
          println(s"==> State =   $stateProduct ")
          println(s"Current Product = $product ")
          println()
          println("------  Product that has Max quantity -------")
          println(s" $minPrice ")
          stateProduct.update(minPrice)
          Some(minPrice.aname,product,minPrice)

        }

        case (Some(prevMinPrice),currentProd) => {
          if(prevMinPrice.aprice > currentProd.aprice) {
            println(s">>> ----------------2----------------- <<<")
            println(s"==> State = $stateProduct ")
            println(s"Current Product = $product ")
            println()
            println("------  Product that have Max quantity -------")
            println(s" $currentProd ")
            stateProduct.update(currentProd)
            Some(currentProd.aname, product, currentProd)
          }

        }
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


  def insertToCassandra(productDSm: org.apache.spark.streaming.dstream.DStream[Product], keyspace: String, table: String): Unit ={
    productDSm.foreachRDD(rdd => {
      if (!rdd.isEmpty()){
        rdd.saveToCassandra(keyspace, table)
      }
    })
  }



}
