package com.aqualen.epamrabbitmq.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "rabbit-mq")
public class RabbitProperties {
  private String exchangeName;
  private String queueName;
  private String routingKey;
  private int interval;
  private int retries = 3;
}
