package com.example.grpcclient;

import com.example.grpcclient.AlertClientGrpc;
import com.example.grpcclient.AlertResponse;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertClientService extends AlertClientGrpc.AlertClientImplBase {

    private RabbitTemplate rabbitTemplate;

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishAlert(AlertResponse response, StreamObserver<Empty> responseObserver) {
        if (response.getIsAlert()) {
            // Публикация сообщения в RabbitMQ
            String alertMessage = String.format(
                    "ALERT: %s - %s",
                    response.getAlertType(),
                    response.getMessage()
            );

            rabbitTemplate.convertAndSend("alertExchange", "alerts.routingKey", alertMessage);
        }

        // Завершение вызова
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
