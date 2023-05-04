package com.own.grpc.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "grpc.cared")
public class GrpcCaredConfig {
    private List<String> grpcServerNames;
}
