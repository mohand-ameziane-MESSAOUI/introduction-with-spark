package com.test.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.spark.sql.Encoders


object Domains {
  case class Product(@JsonProperty("id") aid: Int,@JsonProperty("name") aname: String, @JsonProperty("price") aprice: Double, @JsonProperty("quantity") aquantity: Double)



}
