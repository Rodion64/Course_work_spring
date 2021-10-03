package com.cw.ponomarev.back;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Класс, который реализует логику работы оповещения пользователей по их email-адресу.
 * @author Денис Пономарев
 */
@Service
@RequiredArgsConstructor
public class MailService{
    /**
     *  Предлагает простую абстракцию для отправки электронной почты.
     */
    private final JavaMailSender mailSender;

    /**
     * Имя email, от которого будет отослано письмо.
     */
    @Value("${spring.mail.username}")
    private String username;

    /**
     * Метод, который обеспечивает отправку сообщения на email-адрес пользователя.
     * @param mailTo - email-адрес, на который будет отправлено письмо.
     * @param subject - тема письма, которая будет указана.
     * @param message - основной текст письма.
     */
    public void send(String mailTo, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(username);
        mailMessage.setTo(mailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}
