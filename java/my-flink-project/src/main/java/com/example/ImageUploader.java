package com.example;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class ImageUploader {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Define Kafka consumer properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092"); // Change to your Kafka broker address
        properties.setProperty("group.id", "flink-kafka-group");

        // Create a DataStream from the Kafka topic
        DataStream<String> imagePaths = env.addSource(
            new FlinkKafkaConsumer<>("flink-kafka", new SimpleStringSchema(), properties)
        );

        // Use the map function to upload each image to the FastAPI endpoint
        DataStream<Tuple2<String, Integer>> result = imagePaths.map(new ImageUploadFunction("http://localhost:8000/classify/"));

        // Print the results or perform further processing
        result.print();

        env.execute("Image Uploader");
    }

    // MapFunction to upload an image to a FastAPI endpoint and receive a response
    public static class ImageUploadFunction implements MapFunction<String, Tuple2<String, Integer>> {
        private final String apiUrl;

        public ImageUploadFunction(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        @Override
        public Tuple2<String, Integer> map(String imagePath) throws Exception {
            try {
                // Create a URL object
                URL url = new URL(apiUrl);

                // Create an HTTP connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method to POST
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Create a boundary string for multipart/form-data
                String boundary = "---------------------------" + System.currentTimeMillis();

                // Set request headers
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream outputStream = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
                    FileInputStream fileInputStream = new FileInputStream(imagePath)) {

                    // Write the file part
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + new File(imagePath).getName() + "\"").append("\r\n");
                    writer.append("Content-Type: " + HttpURLConnection.guessContentTypeFromName(imagePath)).append("\r\n");
                    writer.append("\r\n");
                    writer.flush();

                    // Write the file data
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.flush();

                    // Write the closing boundary
                    writer.append("\r\n").append("--" + boundary + "--").append("\r\n");
                }

                // Get the response from the server (optional)
                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                System.out.println("Response Code: " + responseCode);
                System.out.println("Response Message: " + responseMessage);
                
                // Read and print the response text (if any)
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    while (scanner.hasNextLine()) {
                        System.out.println(scanner.nextLine());
                    }
                }

                connection.disconnect();

                return new Tuple2<>(imagePath, responseCode);
            } catch (IOException e) {
                e.printStackTrace();
                return new Tuple2<>(imagePath, -1); // Error code for a failed request
            }
        }
    }
}
