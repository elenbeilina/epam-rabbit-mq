package com.aqualen.epamrabbitmq.service;

import com.aqualen.epamrabbitmq.properties.RabbitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageConsumers {

  private final RabbitProperties rabbitProperties;
  private final MessageService messageService;
  @Value("${spring.cloud.stream.bindings.queue1Consumer-in-0.group}")
  private String queue1Name;

  @Bean
  public Consumer<String> queue1Consumer() {
    return (message) -> log.info("Received message: {} from the queue: {}", message,
        rabbitProperties.getExchangeName() + "." + queue1Name);
  }

  @Bean
  public Consumer<Message<String>> queue2Consumer() {
    return (message) -> {
      AtomicInteger deliveryAttempt = message.getHeaders().get("deliveryAttempt", AtomicInteger.class);
      if (deliveryAttempt.get() > rabbitProperties.getRetries()) {
        // giving up - don't send to DLX, instead send to failed exchange
        // TODO think about copying headers with exception details
        messageService.saveMessage(message.getPayload());
      }
      throwExceptionWhileProcessing(message.getPayload());
    };
  }

  @Bean
  public Consumer<String> failedConsumer() {
    return messageService::saveMessage;
  }


  private String throwExceptionWhileProcessing(String message) {
    throw new ImmediateRequeueAmqpException(
        String.format("Exception happened, while processing the message: %s!", message
        ));
  }
}
