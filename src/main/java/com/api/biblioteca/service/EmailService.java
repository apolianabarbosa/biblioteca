package com.api.biblioteca.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmailRecuperacao(String destinatario, String token) throws MessagingException {
        // ATENÇÃO: O link deve apontar para a rota do seu frontend em React!
        // O frontend receberá o token e fará a chamada para a API.
        String link = "http://localhost:5173/resetar-senha?token=" + token;

        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

        helper.setFrom("contato.biblioteca.api@gmail.com"); 
        helper.setTo(destinatario);
        helper.setSubject("Recuperação de Senha - Estante Gira");
        
        String conteudoHtml = "<h3>Recuperação de Senha</h3>"
                + "<p>Você solicitou a redefinição de sua senha.</p>"
                + "<p>Clique no link abaixo para criar uma nova senha:</p>"
                + "<a href=\"" + link + "\">Redefinir Senha</a>"
                + "<p>Este link expirará em 1 hora.</p>"
                + "<p>Se você não solicitou isso, por favor, ignore este e-mail.</p>";
        
        helper.setText(conteudoHtml, true); // true indica que o conteúdo é HTML

        mailSender.send(mensagem);
    }
}
