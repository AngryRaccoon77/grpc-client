package com.example.grpcclient;

import com.example.grpcclient.AlertBackendGrpc;
import com.example.grpcclient.AlertRequest;
import com.example.grpcclient.DeviceEvent;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class GrpcClientApplication {
    static final String queueName = "deviceStatusQueue2";
    private AlertClientService alertClientService;



    @Autowired
    public void setAlertClientService(AlertClientService alertClientService) {
        this.alertClientService = alertClientService;
    }
    @RabbitListener(queues = queueName)
    public void listen(String message) {
        System.out.println(" [x] Received Grpc '" + message + "'");
        try {
            DeviceEvent deviceEvent = parseMessage(message);

            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                    .usePlaintext()
                    .build();
            AlertBackendGrpc.AlertBackendBlockingStub backendClient = AlertBackendGrpc.newBlockingStub(channel);

            AlertRequest request = AlertRequest.newBuilder().setEvent(deviceEvent).build();
            var response = backendClient.processEvent(request);

            System.out.printf("Backend Response: isAlert=%b, alertType=%s, message=%s%n",
                    response.getIsAlert(),
                    response.getAlertType(),
                    response.getMessage());

            final String deviceName = deviceEvent.getName(); // Get device name from the event
            final AlertResponse modifiedResponse = AlertResponse.newBuilder(response)
                    .setMessage(String.format("Device '%s': %s", deviceName, response.getMessage()))
                    .build();

            alertClientService.publishAlert(modifiedResponse, new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error publishing alert for device '" + deviceName + "': " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Alert successfully published to RabbitMQ for device: " + deviceName);
                }
            });
            channel.shutdown();
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    private DeviceEvent parseMessage(String message) {
        Pattern pattern = Pattern.compile("Device\\{id=(.*?), name='(.*?)', status=(.*?), services=.*?type=(.*?), data='(.*?)'}");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return DeviceEvent.newBuilder()
                    .setId(matcher.group(1))
                    .setName(matcher.group(2))
                    .setStatus(Boolean.parseBoolean(matcher.group(3)))
                    .setType(matcher.group(4))
                    .setData(matcher.group(5))
                    .build();
        } else {
            throw new IllegalArgumentException("Message format is invalid: " + message);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GrpcClientApplication.class, args);
    }
}
