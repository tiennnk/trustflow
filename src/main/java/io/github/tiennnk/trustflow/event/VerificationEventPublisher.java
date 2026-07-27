package io.github.tiennnk.trustflow.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.tiennnk.trustflow.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // publish sau khi transaction commit thanh cong, tranh publish event cho thay doi bi rollback
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VerificationEvent event) {
        String routingKey = "verification." + event.action().name().toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.VERIFICATION_EXCHANGE, routingKey, event);
    }
}
