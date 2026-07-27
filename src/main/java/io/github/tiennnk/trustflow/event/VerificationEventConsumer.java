package io.github.tiennnk.trustflow.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VerificationEventConsumer {

    @RabbitListener(queues = "verification.events.log")
    public void handle(VerificationEvent event) {
        log.info("Received verification event: {}", event);
    }
}
