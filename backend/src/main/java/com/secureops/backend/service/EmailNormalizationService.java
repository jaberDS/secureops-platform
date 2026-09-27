package com.secureops.backend.service;

import org.springframework.stereotype.Service;

@Service
public class EmailNormalizationService {

    public String normalize(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}