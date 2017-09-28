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
object clean_save_data {
  
  case class kafka_Word(word:String)
  
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

    val kafkaStream = KafkaUtils.createDirectStream
    [String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    val events = kafkaStream.map(line => {
     line._2
    })    
    events.foreachRDD(rdd => {
       
      
       rdd.foreach(pair => {

          //Hbase配置 
          val tableName1 = "Normal_yjd"
          val tableName2="Abnormal_yjd"
          val hbaseConf = HBaseConfiguration.create()
          hbaseConf.set("hbase.zookeeper.quorum", "yjd:9092,zeb:9092,ylh:9092")
          hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
          hbaseConf.set("hbase.defaults.for.version.skip", "true")
          
          
          val str = pair.toString().split(",")
          
          if(str(4).toString()!=" " && str(4).toString().toDouble.toInt!=(-902) && 
          	str(4).toString.toDouble>=3 && str(4).toString.toDouble<=12 && 
          	str(22).toString()!=" " && str(22).toString().toDouble.toInt!=(-902) && 
          	str(22).toString.toDouble>=(-0.5*1500) && str(22).toString().toDouble<=(2*1500))
          {
            val w=(str(2)+"_"+str(1)).toString()
            val value = pair.toString()
            //组装数据
            val put = new Put(Bytes.toBytes(w))
            put.add("Result".getBytes, "value".getBytes, Bytes.toBytes(value))
            //put.add("Result".getBytes, "count".getBytes, Bytes.toBytes(co))
           
            val StatTable = new HTable(hbaseConf, TableName.valueOf(tableName1))
            StatTable.setAutoFlush(false, false)
            //写入数据缓存
            StatTable.setWriteBufferSize(20*1024*1024)
            StatTable.put(put)
            //提交
            StatTable.flushCommits()
          }
          else
          {
            val w=(str(2)+"_"+str(1)).toString()
            val value = pair.toString()
            //组装数据
            val put = new Put(Bytes.toBytes(w))
            put.add("Result".getBytes, "value".getBytes, Bytes.toBytes(value))
            //put.add("Result".getBytes, "count".getBytes, Bytes.toBytes(co))
           
            val StatTable = new HTable(hbaseConf, TableName.valueOf(tableName2))
            StatTable.setAutoFlush(false, false)
            //写入数据缓存
            StatTable.setWriteBufferSize(20*1024*1024)
            StatTable.put(put)
            //提交
            StatTable.flushCommits()
          }
            println(pair+" "+str.length)
     

      })
    })
    ssc.start()
    ssc.awaitTermination()

  }

}