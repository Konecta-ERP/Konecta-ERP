package com.konecta.mail_service.consumer;

import com.konecta.mail_service.dto.EmailRequest;
import com.konecta.mail_service.service.MailService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final MailService mailService;

    public EmailConsumer(MailService emailService) {
        this.mailService = emailService;
    }

    @RabbitListener(queues = "${app.rabbitmq.otp-queue}")
    public void handleOtpRequest(@Payload EmailRequest request) {
        System.out.println("Received message from queue: " + request.getRecipient());
        try {
            mailService.sendEmail(request);
            System.out.println("Email sent successfully to: " + request.getRecipient());
        } catch (Exception e) {
            System.err.println("Failed to send email to " + request.getRecipient() + ": " + e.getMessage());
            // Re-throw exception to reject the message and prevent re-queueing
            throw new AmqpRejectAndDontRequeueException("Mail service failed to process request for " + request.getRecipient());
        }
    }
}