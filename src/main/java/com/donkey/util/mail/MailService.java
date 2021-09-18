package com.donkey.util.mail;

import com.donkey.util.mail.MailDto;
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


        helper.setSubject("[당나귀] 임시 패스워드입니다.");
        helper.setTo(mailDto.getAddress());

        Context context = new Context();
        context.setVariable("user_name", mailDto.getUserName());
        context.setVariable("token_string", mailDto.getToken());
        helper.setText(templateEngine.process("mail-password", context),true);

        mailSender.send(message);
    }

}
