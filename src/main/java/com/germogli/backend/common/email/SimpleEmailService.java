package com.germogli.backend.common.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SimpleEmailService implements EmailService {

    private final JavaMailSender mailSender;

    public SimpleEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async  // Envío asíncrono para no bloquear el hilo principal
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
