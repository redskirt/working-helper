package com.sasaki.spark.template

import com.sasaki.{packages => p}
import com.sasaki.spark.enums.LaunchMode
import com.sasaki.spark.enums.SparkType._
import com.sasaki.spark.SparkHandler
import com.sasaki.spark.DataumMockHandler

/**
 * @Author Sasaki
 * @Mail wei.liu@suanhua.org
 * @Timestamp 2018-01-08 上午8:55:23
 * @Description
 */
object FakeDatumSparkTemplate extends DataumMockHandler with SparkHandler {

  type M = LaunchMode.Value

  val conf = buildConfWithoutMaster(DEFAULT_SETTINGS)

  lazy val spark = buildAutomaticOnYarnSparkSession(conf, _mode_, false)
  
  @deprecated("Spark-1.* Version.")
  lazy val sc = spark.sparkContext

  import logger._
  import spark._
  import spark.implicits._

  implicit var _mode_ : M = _
  
  import com.sasaki.packages.constant.original._
  override def mock[T: CT] = Nil //List("1", "33", "23")
  
//  def create[T]: List[T] = List()

  def main(args: Array[String]): Unit = {
    args match {
      case Array() =>
        info("> --------------------------------- Spark will start by local model. ---------------------------------------------")
        _mode_ = LaunchMode.DEVELOP
      case Array(_1) =>
        info("> --------------------------------- Spark will start by cluster model. ---------------------------------------------")
        _mode_ = LaunchMode.DEPLOY
      case _ =>
        error("Init args exception " + (args.foreach(o => print(o + " "))) + ", expect input: ... ")
        throw new IllegalArgumentException(s"Init args exception $args")
    }

    /**
     * _spark_ 必须在 _mode_ 之后初始化，避免提前触发 lazy spark 初始化
     */
        implicit val _spark_ = spark
    invokeSessionHandler { () =>
      {
        if (LaunchMode.isDevelop(_mode_))
          super.createMockRDD
        else {
        	// 测试样例仅本地通过
        	spark.read.textFile(s"${p.reflect.classpath}deploy").rdd
        }
      }//.flatMap(_.split(p.constant.$s)).map((_, 1)).reduceByKey(_ + _) foreach println
    }
  }
}