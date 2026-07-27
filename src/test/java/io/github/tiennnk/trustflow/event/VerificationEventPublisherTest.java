package io.github.tiennnk.trustflow.event;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import io.github.tiennnk.trustflow.config.RabbitMQConfig;
import io.github.tiennnk.trustflow.entity.AuditAction;

@ExtendWith(MockitoExtension.class)
class VerificationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private VerificationEventPublisher verificationEventPublisher;

    @Test
    void handle_approvedEvent_sendsToExchangeWithRoutingKey() {
        VerificationEvent event = new VerificationEvent(UUID.randomUUID(), UUID.randomUUID(), AuditAction.APPROVED, null);

        verificationEventPublisher.handle(event);

        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.VERIFICATION_EXCHANGE, "verification.approved", event);
    }
}
