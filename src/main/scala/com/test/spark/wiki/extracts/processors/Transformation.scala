package com.test.spark.wiki.extracts.processors
import com.test.spark.wiki.extracts.domain.DomainArticle.Person
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object Transformation {



  def scvToDF(path: String)(implicit spark: SparkSession) :DataFrame = {
    spark.read
      .format("csv")
      .option("delimiter", ";")
      .option("header", "true")
      .load(path)
  }

  def personPerCityWithDataFrame(personDF: DataFrame, city: String) : DataFrame = {
    personDF
      .filter(person=>person.getString(3) == city)
  }

  def countPersonPerCity(personDF: DataFrame) : DataFrame = {
      personDF
        .groupBy("ville")
        .count()
  }

  def DatasetToDataFrame(personDF: DataFrame) : Dataset[Person] = {
    personDF
      .map(person => Person(person.getString(0),person.getString(1),person.getString(2),person.getString(3)))

  }

   def personPerCityWithDataset(personDS: Dataset[Person], city: String) : Dataset[Person] = {
     personDS
      .filter(person => person.city == city)
  }

  def personPerCityWithSparkSQL(personDS: Dataset[Person], city: String)(implicit spark: SparkSession) : DataFrame = {
    personDS.createOrReplaceTempView("person")

    spark.sql(
      """
        |select *
        |from person
        |where city = "paris"
        |
        |""".stripMargin)

  }

  def countPersonPerCityWithSparkSQL(personDS: Dataset[Person])(implicit spark: SparkSession) : DataFrame = {
    personDS.createOrReplaceTempView("person")

    spark.sql(
      """
        |select city, count(*) as count
        |from person
        |group by city
        |
        |""".stripMargin)

  }


}
