package com.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.util.Properties;

public class ImageDirectoryToKafkaProducer {
    public static void main(String[] args) {
        // Configure Kafka producer properties
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // Create a Kafka producer
        Producer<String, String> producer = new KafkaProducer<>(properties);

        // Specify the directory containing your images
        String imageDirectoryPath = "/home/zippaper/Kafka/test/java/images";

        // Get a list of image files in the directory
        File directory = new File(imageDirectoryPath);
        File[] imageFiles = directory.listFiles((dir, name) -> name.endsWith(".jpeg")); // You can change the file extension as needed

        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                String imagePath = imageFile.getAbsolutePath();

                // Send the image path to the Kafka topic
                ProducerRecord<String, String> record = new ProducerRecord<>("flink-kafka", imagePath);
                producer.send(record);
                System.out.println("Sent: " + imagePath);
            }
        }

        // Close the Kafka producer
        producer.close();
    }
}