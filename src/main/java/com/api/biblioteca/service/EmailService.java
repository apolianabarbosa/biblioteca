package com.api.biblioteca.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void enviarEmailRecuperacao(String destinatario, String token) throws MessagingException {

        String link = "http://localhost:5173/resetar-senha?token=" + token;

        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

        Context context = new Context();
        context.setVariable("link", link);

        String conteudoHtml = templateEngine.process("email/recuperacaoSenha", context);

        helper.setFrom("contato.biblioteca.api@gmail.com");
        helper.setTo(destinatario);
        helper.setSubject("Recuperação de Senha - Estante Gira");
        helper.setText(conteudoHtml, true);

        mailSender.send(mensagem);
    }

    public void enviarEmailAprovacaoEmprestimo(
        String destinatario,
        String nomeUsuario,
        String tituloLivro,
        Long idEmprestimo)
        throws MessagingException {

        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
        
        Context context = new Context();
        context.setVariable("nomeUsuario", nomeUsuario);
        context.setVariable("idEmprestimo", idEmprestimo);
        context.setVariable("tituloLivro", tituloLivro);

        String conteudoHtml = templateEngine.process("email/emprestimoAprovado", context);

        helper.setFrom("contato.biblioteca.api@gmail.com"); 
        helper.setTo(destinatario);
        helper.setSubject("Seu Empréstimo foi Aprovado! - Estante Gira");
        helper.setText(conteudoHtml, true);

        mailSender.send(mensagem);
    }

    public void enviarEmailRetiradaConfirmada(
        String destinatario, 
        String nomeUsuario, 
        String tituloLivro,
        Long idEmprestimo,
        LocalDateTime dataPrevistaDevolucao,
        DateTimeFormatter formatter) throws MessagingException {

        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
        
        String dataFormatada = dataPrevistaDevolucao.format(formatter);

        Context context = new Context();
        context.setVariable("nomeUsuario", nomeUsuario);
        context.setVariable("idEmprestimo", idEmprestimo);
        context.setVariable("tituloLivro", tituloLivro);
        context.setVariable("dataFormatada", dataFormatada);

        String conteudoHtml = templateEngine.process("email/retiradaConfirmada", context);

        helper.setFrom("contato.biblioteca.api@gmail.com");
        helper.setTo(destinatario);
        helper.setSubject("Retirada Confirmada - Estante Gira");
        helper.setText(conteudoHtml, true);

        mailSender.send(mensagem);
    }
}
