package org.sh.spark

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.sh.{packages => p}
import org.sh.kit.ReflectHandler
import org.sh.spark.enums.LaunchMode

/**
 * @Author Sasaki
 * @Mail wei.liu@suanhua.org
 * @Timestamp 2017-08-29 上午11:39:28
 * @Description 提供Spark初始化、模板调用
 */
trait SparkHandler extends ReflectHandler with LazyLogging {
  import p.constant._
  import p.independent._
  import org.sh.spark.enums._
  import org.sh.spark.enums.SparkType._
  import Master._
  
  private val SPARK_MASTER = "spark.master"
  protected type Mode = LaunchMode.Value
  
  protected val DEFAULT_SETTINGS = Map(
    ("spark.serializer"           -> "org.apache.spark.serializer.KryoSerializer"),
    ("spark.executor.memory"      -> "2G"),
    ("spark.driver.memory"        -> "1G"),
    ("spark.driver.cores"         -> "2"),
    ("spark.driver.maxResultSize" -> "10G"),
    ("spark.total.executor.cores" -> "2")
  )

  /**
   * 兼容 LaunchMode DEVELOP/DEPLOY 同时存在时构造SparkConf。
   * @note 使用该方法后必须手动设置Master
   */
  def buildConfWithoutMaster(
    appName: String,
    settings: Map[String, String]): Conf =
    new Conf().setAppName(appName).setAll(settings)

  /**
   * @see def buildConfWithoutMaster(appName: String, settings: Map[String, String]): SparkConf
   */
  def buildConfWithoutMaster(settings: Map[String, String]): Conf =
    buildConfWithoutMaster(s"spark-job_$getSuccessorName", settings)
  
  /**
   * 仅 LaunchMode DEVELOP 时构造SparkConf，生产项目慎用！
   */
  def buildLocalConf(
    appName: String,
    settings: Map[String, String] = DEFAULT_SETTINGS) =
      buildConfWithoutMaster(appName, settings)
        .setMaster(Master.LOCAL_*.toString)

  /**
   * 兼容 LaunchMode DEVELOP/DEPLOY 同时存在时构造SparkSession。
   * @note 该方法以制造异常方式强制检查Master
   */
  def buildSparkSession(conf: Conf, enableHive: Boolean = false) =
    invokeNonEmpty(conf.get(SPARK_MASTER, $e)) { () => 
      val builder = org.apache.spark.sql.SparkSession.builder().config(conf)
      if (enableHive) builder.enableHiveSupport()
      builder.getOrCreate()
    }

  /**
   * @see def buildSparkSession(conf: SparkConf, enableHive: Boolean): SparkSession
   */
  def buildAutomaticOnYarnSparkSession(conf: Conf, mode: Mode, enableHive: Boolean = false) =
    buildSparkSession({
      if (LaunchMode.isDevelop(mode))
        conf.setMaster(Master.LOCAL_*.toString)
      else
        conf.setMaster(Master.YARN.toString)
    }, enableHive)
  
  
  @deprecated("兼容 Spark-1.* 版本。")
  def buildSparkContext(conf: Conf) = new org.apache.spark.SparkContext(conf)
  
  /**
   * 快速构造Spark，仅用于本地调试，生产项目慎用！
   */
  def buildLocalSparkSession(enableHive: Boolean = false) = {
    // 调试启用临时目录
    System.setProperty("hadoop.home.dir", s"${p.reflect.classpath}hadoop-common-2.2.0-bin-master")
    buildSparkSession(buildLocalConf("spark-local", DEFAULT_SETTINGS), enableHive)
  }
      
  def initStreamingHandler = ???

  def invokeSparkHandler[T <: { def stop(): Unit }](spark_* : T)(f_x: () => Unit) = 
    try f_x() finally spark_*.stop
     
  def invokeSessionHandler(f_x: () => Unit)(implicit spark: Spark) = 
    try f_x() finally spark.stop

  def invokeMultipleSessionHandler(f_x: () => Unit)(implicit spark: Spark, mode: Mode) =
    try {
      f_x() 
    } finally spark.stop
    
  @deprecated("兼容 Spark-1.* 版本。")
  def invokeContextHandler(f_x: () => Unit)(implicit sc: SC) = 
    try f_x() finally sc.stop
    
  def invokeStreamingHandler(f_x: () => Unit)(implicit ssc: Streaming) =
    try {
      f_x()
      ssc.start()
      ssc.awaitTermination()
    } //
    finally ssc.stop()
}
