package com.aqualen.epamrabbitmq.repositories;

import com.aqualen.epamrabbitmq.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
