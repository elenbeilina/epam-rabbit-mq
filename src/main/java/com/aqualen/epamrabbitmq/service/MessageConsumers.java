package com.aqualen.epamrabbitmq.service;

import com.aqualen.epamrabbitmq.properties.RabbitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageConsumers {

  private final RabbitProperties rabbitProperties;
  @Value("${spring.cloud.stream.bindings.queue1Consumer-in-0.group}")
  private String queue1Name;
  @Value("${spring.cloud.stream.bindings.queue2Consumer-in-0.group}")
  private String queue2Name;

  @Bean
  public Consumer<String> queue1Consumer() {
    return (message) -> log.info("Received message: {} from the queue: {}", message,
        rabbitProperties.getExchangeName() + "." + queue1Name);
  }

  @Bean
  public Consumer<String> queue2Consumer() {
    return (message) -> log.info("Received message: {} from the queue: {}", message,
        rabbitProperties.getExchangeName() + "." + queue2Name);
  }
}
