package Encuesta;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    
    // Configura aquí tus datos de correo
    private static final String SMTP_HOST = "smtp.gmail.com"; // o el SMTP de tu proveedor
    private static final String SMTP_PORT = "587"; // o 465 para SSL
    private static final String FROM_EMAIL = "agustin.gutierrez.lopez98@gmail.com"; // Reemplaza con tu correo
    private static final String FROM_PASSWORD = "qjgx tbwx ozkx erso";     // Reemplaza con tu contraseña

    public static void sendInvitationEmail(String toEmail, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        // Configuraciones para Gmail; ajusta según tu servidor
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        System.out.println("Correo de invitación enviado correctamente a " + toEmail);
    }
}

