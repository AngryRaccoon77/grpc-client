package com.example.grpcclient;

import com.example.grpcclient.AlertBackendGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcBackendConfig {

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder
                .forAddress("localhost", 9090) // Адрес gRPC бэкенда
                .usePlaintext()
                .build();
    }

    @Bean
    public AlertBackendGrpc.AlertBackendBlockingStub alertBackendStub(ManagedChannel channel) {
        return AlertBackendGrpc.newBlockingStub(channel);
    }
}
