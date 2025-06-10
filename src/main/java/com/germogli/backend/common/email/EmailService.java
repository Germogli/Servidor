package com.germogli.backend.common.email;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
}
