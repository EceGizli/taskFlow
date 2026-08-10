package com.beat.taskFlow.user.service;

import com.beat.taskFlow.common.exception.EmailSendingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(
            String to,
            String token,
            long expirationMinutes) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(to);
            message.setSubject("TaskFlow - Şifre Sıfırlama Talebi");

            message.setText("""
                    Merhaba,

                    TaskFlow hesabınız için bir şifre sıfırlama talebi oluşturuldu.

                    Şifrenizi sıfırlamak için aşağıdaki token değerini kullanabilirsiniz:

                    Token: %s

                    Bu token yalnızca bir kez kullanılabilir.
                    Geçerlilik süresi: %d dakikadır.

                    Eğer bu talebi siz oluşturmadıysanız bu e-postayı dikkate almayabilirsiniz.

                    TaskFlow Destek Ekibi
                    """.formatted(token, expirationMinutes));

            mailSender.send(message);

        } catch (Exception ex) {
            throw new EmailSendingException(
                    "Şifre sıfırlama e-postası gönderilemedi.",
                    ex
            );
        }
    }
}