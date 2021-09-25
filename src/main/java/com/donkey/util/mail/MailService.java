package com.donkey.util.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendEmailForPassword(MailDto mailDto) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(mailDto.getTo());
        helper.setSubject(mailDto.getSubject());

        Context context = new Context();
        context.setVariable("user_name", mailDto.getUserName());
        context.setVariable("token_string", mailDto.getToken());
        helper.setText(templateEngine.process("mail-password", context),true);

        mailSender.send(message);
    }

    public void sendEmailForEmailVerification(MailDto mailDto) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(mailDto.getTo());
        helper.setSubject(mailDto.getSubject());

        Context context = new Context();
        context.setVariable("user_name", mailDto.getUserName());
        context.setVariable("url_string", mailDto.getToken());
        helper.setText(templateEngine.process("mail-verify", context),true);

        mailSender.send(message);
    }
}
