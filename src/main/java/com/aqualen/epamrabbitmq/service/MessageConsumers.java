package com.aqualen.epamrabbitmq.service;

import com.aqualen.epamrabbitmq.properties.RabbitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    return (message) -> {
      log.info("Received message: {} from the queue: {}", message,
          rabbitProperties.getExchangeName() + "." + queue1Name);
    };
  }

  @Bean
  public Consumer<Message<String>> queue2Consumer() {
    return (message) -> {
      List<Map<String, Object>> deathHeaders = (List<Map<String, Object>>) message.getHeaders()
          .get("x-death", List.class);
      Map<String, Object> deathHeader = CollectionUtils.isEmpty(deathHeaders) ? null : deathHeaders.get(0);
      if (Objects.nonNull(deathHeader) &&
          (Long) deathHeader.get("count") > rabbitProperties.getRetries()) {
        messageService.sendMessageToFailedQueue(message.getPayload(), null);
        throw new ImmediateAcknowledgeAmqpException(
            String.format("Failed after %s reties.", rabbitProperties.getRetries())
        );
      }
      throwExceptionWhileProcessing(message.getPayload());

    };
  }

  @Bean
  public Consumer<String> failedConsumer() {
    return messageService::saveMessage;
  }

  @Bean
  public Consumer<String> dlqConsumer() {
    return message -> log.error("Message: {} went to dlq!", message);
  }

  private String throwExceptionWhileProcessing(String message) {
    throw new ImmediateRequeueAmqpException(
        String.format("Exception happened, while processing the message: %s!", message
        ));
  }

  @Bean
  /*
  When you want to use requeue mechanism - in props you need to declare:
  spring.cloud.stream.rabbit.bindings.queue2Consumer-in-0.consumer.dlqDeadLetterExchange=
  but with dlqDeadLetterExchange= empty - dlq does not bind to the queues.
  When you just want broker to send messages to dlq - dlqDeadLetterExchange=message.dlq
  and everything works fine without this bean, but requeue is not working.
  In conclusion without this bean requeue + dlq mechanism does not work.
   */
  public DeclarableCustomizer declarableCustomizer() {
    return declarable -> {
      if (declarable instanceof Queue) {
        var queue = (Queue) declarable;
        if (queue.getName().equals("message.queue1")
            || queue.getName().equals("message.queue2")) {
          queue.removeArgument("x-dead-letter-exchange");
          queue.removeArgument("x-dead-letter-routing-key");

          queue.addArgument("x-dead-letter-exchange", "message.dql");
        }
      }
      return declarable;
    };
  }
}
