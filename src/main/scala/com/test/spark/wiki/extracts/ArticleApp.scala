package com.test.spark.wiki.extracts
import com.test.spark.wiki.extracts.processors.Transformation
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}

object ArticleApp {

  def main(args: Array[String]): Unit = {
    implicit val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .getOrCreate()

    val personDF = Transformation.scvToDF("pathfile.csv")
    val personPerCityWithDataFrameDF = Transformation.personPerCityWithDataFrame(personDF, "paris")
    val countPersonPerCityDF = Transformation.countPersonPerCity(personDF)
    val DatasetToDataFrameDS = Transformation.DatasetToDataFrame(personDF)
    val personPerCityWithDatasetDS = Transformation.personPerCityWithDataset(DatasetToDataFrameDS,"paris")
    val personPerCityWithSparkSQLDF = Transformation.personPerCityWithSparkSQL(DatasetToDataFrameDS,"paris")
    val countPersonPerCityWithSparkSQLDF = Transformation.countPersonPerCityWithSparkSQL(DatasetToDataFrameDS)

    personDF.show()
    personPerCityWithDataFrameDF.show()
    countPersonPerCityDF.show()
    DatasetToDataFrameDS.show()
    personPerCityWithDatasetDS.show()
    personPerCityWithSparkSQLDF.show()
    countPersonPerCityWithSparkSQLDF.show()
  }

}
