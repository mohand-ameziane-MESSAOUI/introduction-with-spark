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

  def personPerCityWithDataFrame(personneDF: DataFrame, city: String) : DataFrame = {
    personneDF
      .filter(person=>person.getString(3) == city)
  }

  def countPersonPerCity(personneDF: DataFrame) : DataFrame = {
      personneDF
      .groupBy("ville")
      .count()
  }

  def DatasetToDataFrame(personneDF: DataFrame) : Dataset[Person] = {
    personneDF
      .map(person => Person(person.getString(0),person.getString(1),person.getString(2),person.getString(3)))

  }

   def personPerCityWithDataset(personneDS: Dataset[Person], city: String) : Dataset[Person] = {
     personneDS
      .filter(person => person.city == city)
  }

  def personPerCityWithSparkSQL(personneDS: Dataset[Person], city: String)(implicit spark: SparkSession) : DataFrame = {
    personneDS.createOrReplaceTempView("person")

    spark.sql(
      """
        |select *
        |from person
        |where city = "paris"
        |
        |""".stripMargin)

  }

  def countPersonPerCityWithSparkSQL(personneDS: Dataset[Person])(implicit spark: SparkSession) : DataFrame = {
    personneDS.createOrReplaceTempView("person")

    spark.sql(
      """
        |select city, count(*) as count
        |from person
        |group by city
        |
        |""".stripMargin)

  }


}
