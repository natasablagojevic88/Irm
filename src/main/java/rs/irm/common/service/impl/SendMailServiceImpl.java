package rs.irm.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.SmtpServer;
import rs.irm.administration.enums.SmtpSecurity;
import rs.irm.common.service.SendMailService;

@Named
public class SendMailServiceImpl implements SendMailService {

	@Override
	public void sendMail(SmtpServer smtpServer,String toAddress, String subject, String text,File attachment) {
		
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpServer.getHost());
		props.put("mail.smtp.port", smtpServer.getPort()); 
		props.put("mail.smtp.auth", smtpServer.getAuthentication()); 
		props.put("mail.smtp.starttls.enable", "false");
		if(smtpServer.getSecurity().equals(SmtpSecurity.STARTTLS)){
			props.put("mail.smtp.starttls.enable", "true");
		}
		if(smtpServer.getSecurity().equals(SmtpSecurity.SSL)){
			props.put("mail.smtp.socketFactory.port", smtpServer.getPort()); 
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory"); 
		}
		
		Authenticator auth=null;
		
		if(smtpServer.getAuthentication()) {
			auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(smtpServer.getUsername(), smtpServer.getPassword());
				}
			};
		}
		
		Session session = Session.getDefaultInstance(props, auth);
		
		MimeMessage message=new MimeMessage(session);
		try {
			message.addHeader("Content-type", "text/html; charset=UTF-8");
			message.setSubject(subject,"UTF-8");
			
			MimeBodyPart bodyPart=new MimeBodyPart();
			bodyPart.setContent(text==null?"":text,"text/html; charset=UTF-8");

			
			Multipart multipart=new MimeMultipart();
			multipart.addBodyPart(bodyPart);
			
			if(attachment!=null) {
				MimeBodyPart attPart=new MimeBodyPart();
				try {
					attPart.attachFile(attachment);
					multipart.addBodyPart(attPart);
				} catch (IOException e) {
					throw new WebApplicationException(e);
				}
			}

			message.setContent(multipart);
			
			message.setFrom(new InternetAddress(smtpServer.getFromMail()));
			message.setRecipients(RecipientType.TO,InternetAddress.parse(toAddress));
			
			Transport.send(message);
		} catch (MessagingException e) {
			throw new WebApplicationException(e);
		}
		
	}

}
