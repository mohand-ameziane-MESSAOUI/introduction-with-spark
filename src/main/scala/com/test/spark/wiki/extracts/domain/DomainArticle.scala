package com.test.spark.wiki.extracts.domain

import org.apache.spark.sql.Encoders

object DomainArticle {
  case class Person (lastName: String, firstName: String, dateB: String, city: String)

  implicit val encdPersonne = Encoders.product[Person]
}
