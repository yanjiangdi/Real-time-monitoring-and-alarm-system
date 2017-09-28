package hw2
import org.apache.spark.sql.SQLContext
import kafka.serializer.StringDecoder
import net.sf.json.JSONObject
import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkConf
import java.util.HashMap
import org.apache.spark.sql.SQLContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import java.lang.String
import org.apache.spark.rdd.RDD

import org.apache.spark.{SparkConf, SparkContext}
import java.sql.{DriverManager, Connection} 
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

object Time_alarm {
  
  case class info(id:String,t:String,infor:String)
  def myFun(iterator: Iterator[String]): Unit = {

      var conn: Connection= null 
      var ps:java.sql.PreparedStatement=null 
      val sql="insert into info(id,t,infor) values (?,?,?)"
      conn=DriverManager.getConnection("jdbc:mysql://172.17.11.174:3306/hive?useUnicode=true
        &characterEncoding=UTF-8","root","root") 
      ps = conn.prepareStatement(sql) 

      var m=Map[String,Int]()
      iterator.foreach(data => { 
           var count=0
           val w=data.split(",")(1).toString()
           val time=data.split(",")(2).toString()
           val tem=data.split(",")(13).toString()
           if(!m.contains(w){
             m+=(w->1)}
           else{
             m+=(w->(m.apply(w)+1))}
          if(m.apply(w)>=5){
           ps.setString(1, w) 
           ps.setString(2,getNowTime()) 
           ps.setString(3, "发电机温度超标,已达："+tem+"度") 
           ps.executeUpdate() //执行了Sql语句 }
        } ) 

  }
  
 def getNowTime(): String = {
    //实例化一个Date对象并且获取时间戳
    val time = new Date().getTime
    //设置时间格式
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    //将时间格式套用在获取的时间戳上
   format.format(time)
}
 
  def main(args: Array[String]) {
    
    val sparkConf = new SparkConf().setAppName("NetworkWordCount").setMaster("local[2]").set("spark.driver.allowMultipleContexts","true")
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
 
    import sqlContext.implicits._
    val topics = Set("test")
    //本地虚拟机ZK地址
    val brokers = "yjd:9092,ylh:9092,zeb:9092"
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> brokers,
      "serializer.class" -> "kafka.serializer.StringEncoder")

    // Create a direct stream
    val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    val events=kafkaStream.filter(x=>
        x._2.split(",")(4).toString()!=" " && x._2.split(",")(4).toString().toDouble.toInt!=(-902) && x._2.split(",")(4).toString().toDouble>=3 
        && x._2.split(",")(4).toString().toDouble<=12 && x._2.split(",")(22).toString()!=" " && x._2.split(",")(22).toString().toDouble.toInt!=(-902) 
        && x._2.split(",")(22).toString().toDouble>=(-0.5*1500) && x._2.split(",")(22).toString().toDouble<=(2*1500)&& x._2.split(",")(13).toString().toDouble>80
      )
      
    val ev=events.map(x=>x._2)
     val wc_r=ev.window(Seconds(30),Seconds(5))
     wc_r.foreachRDD(rdd=>
     
       rdd.foreachPartition (myFun)
      
       
   
   )
 
    
    ssc.start()
    ssc.awaitTermination()

  }

}