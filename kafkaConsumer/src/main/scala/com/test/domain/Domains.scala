package com.test.domain

import org.apache.spark.sql.Encoders


object Domains {
  case class Product(id: String, name: String, price: String, quantity: Double)

 implicit val encd = Encoders.product[Product]

}
