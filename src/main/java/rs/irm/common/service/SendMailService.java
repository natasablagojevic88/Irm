package rs.irm.common.service;

import java.io.File;

import rs.irm.administration.entity.SmtpServer;

public interface SendMailService {

	void sendMail(SmtpServer smtpServer,String toAddress, String subject,String text,File attachment);
}
