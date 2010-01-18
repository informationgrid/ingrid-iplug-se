package org.apache.nutch.mail;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class MailSender implements Configurable {

    protected static Log LOG = LogFactory.getLog(MailSender.class);

    protected static MailSender _instance;

    private final JavaMailSenderImpl _mailSender;

    private Configuration _conf;

    private boolean _enabled;

    private String _senderAddress;

    private String _senderPersonal;

    private String _receiverAddress;

    private String _receiverPersonal;

    private MailSender(final Configuration configuration) {
        _mailSender = new JavaMailSenderImpl();
        setConf(configuration);
    }

    public static MailSender get(final Configuration configuration) {
        if (_instance == null) {
            _instance = new MailSender(configuration);
        }
        return _instance;
    }

    public void sendMail(final String subject, final String content) {
        sendMail(new String[] { _receiverAddress }, new String[] { _receiverPersonal }, subject, content, null);
    }

    public void sendMail(final String subject, final String content, final File attachment) {
        sendMail(new String[] { _receiverAddress }, new String[] { _receiverPersonal }, subject, content, attachment);
    }

    public void sendMail(final String[] receiverAddress, final String[] receiverPersonl, final String subject,
            final String content) {
        sendMail(receiverAddress, receiverPersonl, subject, content, null);
    }

    public void sendMail(final String[] receiverAddress, final String[] receiverPersonl, final String subject,
            final String content, final File attachment) {
        sendMail(_senderAddress, _senderPersonal, receiverAddress, receiverPersonl, subject, content,
                attachment);
    }

    public void sendMail(final String senderAddress, final String senderPersonal, final String receiverAddresses[],
            final String receiverPersonals[], final String subject, final String content, final File attachment) {
        if (!_enabled) {
            LOG.info("[mail-disabled]");
            LOG.info("    send to: " + Arrays.asList(receiverAddresses));
            LOG.info("    subject: " + subject);
            LOG.info("    message: " + content);
            if (attachment != null) {
                LOG.info("    attachment: " + attachment);
            }
            LOG.info("[/mail-disabled]");
            return;
        }
        final MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(final MimeMessage msg) throws Exception {
                // set sender
                final InternetAddress sender = new InternetAddress(senderAddress);
                sender.setPersonal(senderPersonal, "UTF-8");
                msg.setFrom(sender);

                // set recipients
                for (int i = 0; i < receiverAddresses.length; i++) {
                    final InternetAddress receiver = new InternetAddress(receiverAddresses[i]);
                    if (receiverPersonals != null && receiverPersonals.length > i) {
                        receiver.setPersonal(receiverPersonals[i], "UTF-8");
                    }
                    msg.addRecipient(Message.RecipientType.TO, receiver);
                }

                // set subject
                msg.setSubject(subject, "UTF-8");

                // create multipart
                final Multipart mp = new MimeMultipart();

                // add text content
                final MimeBodyPart textBody = new MimeBodyPart();
                textBody.setText(content, "UTF-8");
                mp.addBodyPart(textBody);

                // attach file
                if (attachment != null) {
                    final MimeBodyPart fileBody = new MimeBodyPart();
                    final FileDataSource data = new FileDataSource(attachment);
                    fileBody.setDataHandler(new DataHandler(data));
                    fileBody.setFileName(attachment.getName());
                    mp.addBodyPart(fileBody);
                }

                // add multipart to message
                msg.setContent(mp);
            }
        };
        _mailSender.send(preparator);
    }

    @Override
    public Configuration getConf() {
        return _conf;
    }

    @Override
    public void setConf(final Configuration conf) {
        _conf = conf;
        _enabled = _conf.getBoolean("mail.enabled", false);
        _senderAddress = _conf.get("mail.sender.address", "empty");
        _senderPersonal = _conf.get("mail.sender.personal", "empty");
        _receiverAddress = _conf.get("mail.receiver.address", "empty");
        _receiverPersonal = _conf.get("mail.receiver.personal", "empty");
        _mailSender.setHost(_conf.get("mail.host", "empty"));
        _mailSender.setPort(_conf.getInt("mail.port", 25));
        _mailSender.setUsername(_conf.get("mail.user", "empty"));
        _mailSender.setPassword(_conf.get("mail.password", "empty"));
        Properties properties = _mailSender.getJavaMailProperties();
        if (properties == null) {
            properties = new Properties();
            _mailSender.setJavaMailProperties(properties);
        }
        properties.setProperty("mail.smtp.auth", _conf.get("mail.auth", "false"));
        properties.setProperty("mail.smtp.starttls.enabled", _conf.get("mail.starttls", "false"));
    }
}
