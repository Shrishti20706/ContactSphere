package com.contact.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {
		boolean f = false;

		String from = "shrishtijain20706@acropolis.in";

		// Variable for Gmail host
		String host = "smtp.gmail.com";

		// Get the system property
		Properties properties = System.getProperties();

		// Setting important information to properties object
		// Host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// Step 1: to get session object
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("shrishtijain20706@acropolis.in", "#acro123456");
			}
		});

		// Step 2: compose the message[text,multimessage]
		MimeMessage m = new MimeMessage(session);

		try {
			// From email id
			m.setFrom(new InternetAddress(from));

			// Adding recipient
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Adding text to message
			// m.setText(message);
			m.setContent(message, "text/html");

			// Setting the subject
			m.setSubject(subject);
			// Step 3: send the message using Transport class
			Transport.send(m);
			System.out.println("Successfully sent email.....");
			f = true;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
}
