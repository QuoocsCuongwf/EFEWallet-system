package com.QuoocCuongwf.EFEWallet.AuthService.config;

import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ProducerFactory;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;



import java.util.HashMap;
import java.util.Map;



@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic walletTransactionTopic() {
        // Tên topic, số lượng partition, số lượng replication
        return TopicBuilder.name("wallet-transactions")quoc_cuongwf@cuongwflaptophp  ~/workspace/java_workspace/EFEWallet   main.cuong.dev ✚  git reset
        Unstaged changes after reset:
        M       .idea/EFEWallet.iml
        M       .idea/compiler.xml
        M       .idea/misc.xml
        M       AuthService/pom.xml
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/WalletSystemApiApplication.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/config/RedisConfig.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/controller/AuthController.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/reponsitory/UserReponsitory.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/security/JwtAuthFilter.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/security/JwtService.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/service/AuthService.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/service/OtpService.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/service/UserService.java
        M       AuthService/src/main/java/com/QuoocCuongwf/EFEWallet/AuthService/util/JwtUtil.java
        D       AuthService/src/main/resources/application.properties
        M       README.md
        D       TransactionService/.env
        D       TransactionService/.gitattributes
        D       TransactionService/.gitignore
        D       TransactionService/.mvn/wrapper/maven-wrapper.properties
        D       TransactionService/Dockerfile
        D       TransactionService/README.md
        D       TransactionService/docker-compose.yml
        D       TransactionService/mvnw
        D       TransactionService/mvnw.cmd
        D       TransactionService/pom.xml
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/TransactionServiceApplication.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/config/SecurityConfig.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/config/SecurityConstants.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/controller/TransactionController.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/entity/Transaction.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/enums/IdempotencyStatus.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/enums/TransactionStatus.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/enums/TransactionType.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/exception/InsufficientBalanceException.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/exception/WalletNotFoundException.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/payload/request/TransferRequest.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/payload/response/ApiResponse.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/reponsitory/TransactionReponsitory.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/security/JwtFilter.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/service/TransactionService.java
        D       TransactionService/src/main/java/com/QuoocCuongwf/EFEWallet/TransactionService/util/JwtUtil.java
        D       TransactionService/src/main/proto/Wallet.proto
        D       TransactionService/src/main/resources/application.properties
        D       TransactionService/src/main/resources/data.sql
        D       TransactionService/src/test/java/com/QuoocCuongwf/EFEWallet/TransactionService/TransactionServiceApplicationTests.java
        D       TransactionService/src/test/resources/application-test.properties
        M       WalletService/src/main/java/com/QuoocsCuongwf/EFEWallet/WalletService/payload/KafkaMessage/TransferMessage.java
        M       docker-compose.kafka.yml
        quoc_cuongwf@cuongwflaptophp  ~/workspace/java_workspace/EFEWallet   main.cuong.dev ± 
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Địa chỉ Kafka Broker
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Key là String
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Value là Object nên phải dùng JsonSerializer
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
