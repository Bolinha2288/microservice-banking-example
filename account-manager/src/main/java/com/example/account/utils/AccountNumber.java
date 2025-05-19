package com.example.account.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountNumber {

    public String generate(UUID userReference) {
        return generateUniqueNumberAccount(userReference);
    }

    private String generateUniqueNumberAccount(UUID userReference) {
        String hash = DigestUtils.sha256Hex(userReference.toString());
        return hash.replaceAll("[^0-9]", "").substring(0, 10);
    }
}
