package com.aqualen.epamrabbitmq.controllers;

import com.aqualen.epamrabbitmq.entities.Message;
import com.aqualen.epamrabbitmq.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("message")
@RequiredArgsConstructor
public class MessagesController {

  private final MessageService messageService;

  @PostMapping("publish")
  ResponseEntity<Void> publishMessage(@RequestBody String message) {
    log.info("Sending message to the ActiveMQ: {}", message);
    messageService.publishMessage(message);
    return ResponseEntity.ok().build();
  }

  @GetMapping("all")
  ResponseEntity<List<Message>> getMessages() {
    return ResponseEntity.ok(messageService.getMessages());
  }
}
