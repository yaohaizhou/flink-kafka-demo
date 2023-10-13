package com.example;

// import org.apache.flink.api.common.typeinfo.Types;
// import org.apache.flink.api.java.ExecutionEnvironment;
// import org.apache.flink.api.java.operators.AggregateOperator;
// import org.apache.flink.api.java.operators.DataSource;

// import org.apache.flink.api.java.operators.FlatMapOperator; 
// import org.apache.flink.api.java.operators.UnsortedGrouping; 
// import org.apache.flink.api.java.tuple.Tuple2; 
// import org.apache.flink.util.Collector;  
// import org.apache.flink.api.common.functions.FlatMapFunction;

// public class BatchWordCount {      
//     public static void main(String[] args) throws Exception {          
//         // 1. 创建执行环境         
//         ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();                  
//         // 2. 从文件读取数据  按行读取(存储的元素就是每行的文本)         
//         DataSource<String> lineDS = env.readTextFile("input/words.txt");                  
//         // 3. 转换数据格式         
//         FlatMapOperator<String, Tuple2<String, Long>> wordAndOne = lineDS.flatMap(new FlatMapFunction<String, Tuple2<String, Long>>() 
//         {              
//             @Override             
//             public void flatMap(String line, Collector<Tuple2<String, Long>> out) throws Exception {                  
//                 String[] words = line.split(" ");                  
//                 for (String word : words) {                     
//                     out.collect(Tuple2.of(word,1L));                 
//                 }             
//             }         
//         });          
//         // 4. 按照 word 进行分组         
//         UnsortedGrouping<Tuple2<String, Long>> wordAndOneUG = wordAndOne.groupBy(0);                  
//         // 5. 分组内聚合统计         
//         AggregateOperator<Tuple2<String, Long>> sum = wordAndOneUG.sum(1);          
//         // 6. 打印结果         
//         sum.print();     
//     } 
// }

import org.apache.flink.api.common.typeinfo.Types; 
import org.apache.flink.api.java.tuple.Tuple2; 
import org.apache.flink.streaming.api.datastream.DataStreamSource; 
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator; 
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment; 
import org.apache.flink.util.Collector;  
import java.util.Arrays;  
import org.apache.flink.api.common.functions.FlatMapFunction;

public class StreamWordCount {      
    public static void main(String[] args) throws Exception {              
        // 1. 创建流式执行环境         
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 读取文件         
        DataStreamSource<String> lineStream = env.readTextFile("input/words.txt");                  
        // 3. 转换、分组、求和,得到统计结果         
        SingleOutputStreamOperator<Tuple2<String, Long>> sum = lineStream.flatMap(
            new FlatMapFunction<String, Tuple2<String, Long>>() {             
                @Override             
                public void flatMap(String line, Collector<Tuple2<String, Long>> out) throws Exception {
                                      
                    String[] words = line.split(" ");                  
                    for (String word : words) {                     
                        out.collect(Tuple2.of(word, 1L));                 
                    }             
                }         
            })
            .keyBy(data -> data.f0)            
            .sum(1);          
        // 4. 打印         
        sum.print();                  
        // 5. 执行         
        env.execute();     
    } 
}