package com.api.biblioteca.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public void enviarEmailAprovacaoEmprestimo(
        String destinatario, 
        String nomeUsuario, 
        String tituloLivro,
        Long idEmprestimo)
        throws MessagingException {

        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

        helper.setFrom("contato.biblioteca.api@gmail.com"); 
        helper.setTo(destinatario);
        helper.setSubject("Seu Empréstimo foi Aprovado! - Estante Gira");

        String conteudoHtml = """
    <!DOCTYPE html>
    <html lang='pt-BR'>
    <head>
       <meta charset='UTF-8'>
       <style>
           body { font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 0; margin: 0; }
           .container { background: #ffffff; max-width: 600px; margin: 20px auto; padding: 20px; border-radius: 8px; }
           .header { text-align: center; padding-bottom: 15px; border-bottom: 3px solid #612B9F; }
           .header img { width: 130px; }
           h3 { color: #0E0F30; }
           h4 { color: #0E0F30; margin-top: 20px; }
           .info strong { color: #612B9F; }
           .details, .footer { margin-top: 20px; font-size: 15px; }
           ul { padding-left: 20px; }
           .cta { display: inline-block; margin-top: 15px; background: #612B9F; color: #ffffff !important; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; }
           .footer { text-align: center; color: #555; font-size: 13px; }
       </style>
    </head>
    <body>
       <div class='container'>
           <div class='header'>
               <img src='http://localhost:5173/logoEstanteGira.png' alt='Estante Gira'>
               <h3>Empréstimo Aprovado!</h3>
           </div>
     
           <p>Olá, <strong>%s</strong>!</p>
           <p>Seu empréstimo foi aprovado e está aguardando retirada ✅</p>
           <p class='info'><strong>Número de Rastreio:</strong> %d</p>
           <hr>
           <h4>📚 Detalhes do Empréstimo</h4>
           <p class='info'><strong>Livro:</strong> %s</p>
                        <hr>
           <h4>📍 Como retirar na Biblioteca</h4>
           <ul>
               <li>Apresente este e-mail (celular ou impresso) no atendimento.</li>
               <li>Tenha um documento oficial com foto.</li>
               <li>Disponível para retirada por <strong>48 horas</strong>.</li>
           </ul>
           <p><a class='cta' href='http://localhost:5173/meus-emprestimos'>Ver meus empréstimos</a></p>
     
           <div class='footer'>
               <p>Obrigado por utilizar a <strong>Estante Gira</strong>! 📚✨</p>
               <p>Este e-mail é automático, não responda.</p>
           </div>
       </div>
    </body>
    </html>
    """.formatted(nomeUsuario, idEmprestimo, tituloLivro);
        
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

        helper.setFrom("contato.biblioteca.api@gmail.com"); 
        helper.setTo(destinatario);
        helper.setSubject("Retirada Confirmada - Estante Gira");

        String dataFormatada = dataPrevistaDevolucao.format(formatter);

        String conteudoHtml = """
    <!DOCTYPE html>
    <html lang='pt-BR'>
    <head>
       <meta charset='UTF-8'>
       <style>
           body { font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 0; margin: 0; }
           .container { background: #ffffff; max-width: 600px; margin: 20px auto; padding: 20px; border-radius: 8px; }
           .header { text-align: center; padding-bottom: 15px; border-bottom: 3px solid #612B9F; }
           .header img { width: 130px; }
           h3 { color: #0E0F30; }
           h4 { color: #0E0F30; margin-top: 20px; }
           .info strong { color: #612B9F; }
           .details, .footer { margin-top: 20px; font-size: 15px; }
           ul { padding-left: 20px; }
           .cta { display: inline-block; margin-top: 15px; background: #612B9F; color: #ffffff !important; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; }
           .footer { text-align: center; color: #555; font-size: 13px; }
       </style>
    </head>
    <body>
       <div class='container'>
           <div class='header'>
               <img src='http://localhost:5173/logoEstanteGira.png' alt='Estante Gira'>
               <h3>Retirada de Livro Confirmada!</h3>
           </div>
     
           <p>Olá, <strong>%s</strong>!</p>
           <p>Você retirou seu livro e o seu prazo de empréstimo começou.</p>
           <p class='info'><strong>Número de Rastreio:</strong> %d</p>
           <hr>
           <h4>📚 Detalhes do Empréstimo</h4>
           <p class='info'><strong>Livro:</strong> %s</p>
           <p class='info'><strong>Data prevista para devolução:</strong> %s</p>
           <hr>
           <p><a class='cta' href='http://localhost:5173/meus-emprestimos'>Ver meus empréstimos</a></p>
     
           <div class='footer'>
               <p>Obrigado por utilizar a <strong>Estante Gira</strong>! 📚✨</p>
               <p>Este e-mail é automático, não responda.</p>
           </div>
       </div>
    </body>
    </html>
    """.formatted(nomeUsuario, idEmprestimo, tituloLivro, dataFormatada);
        
        helper.setText(conteudoHtml, true);
        mailSender.send(mensagem);
        }
}
