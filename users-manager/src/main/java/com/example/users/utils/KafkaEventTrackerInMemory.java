package com.example.users.utils;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KafkaEventTrackerInMemory {

    private final Set<UUID> sentIds = ConcurrentHashMap.newKeySet();

    public boolean alreadySent(UUID idReference) {
        return sentIds.contains(idReference);
    }

    public void markAsSent(UUID idReference) {
        sentIds.add(idReference);
    }

    public void removeSent(UUID idReference) {
        sentIds.remove(idReference);
    }
}
