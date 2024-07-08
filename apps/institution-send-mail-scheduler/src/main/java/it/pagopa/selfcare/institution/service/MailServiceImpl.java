package it.pagopa.selfcare.institution.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import it.pagopa.selfcare.azurestorage.AzureBlobClient;
import it.pagopa.selfcare.institution.exception.GenericException;
import it.pagopa.selfcare.institution.model.FileMailData;
import it.pagopa.selfcare.institution.model.MailTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.commons.text.StringSubstitutor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class MailServiceImpl implements MailService{

    private static final String ERROR_DURING_SEND_MAIL = "Error during send mail";

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private final AzureBlobClient azureBlobClient;
    private final ObjectMapper objectMapper;
    private final String senderMail;
    private final Boolean destinationMailTest;
    private final String destinationMailTestAddress;
    private final Mailer mailer;

    public MailServiceImpl(AzureBlobClient azureBlobClient,
                           ObjectMapper objectMapper,
                           @ConfigProperty(name = "institution-send-mail.sender-mail") String senderMail,
                           @ConfigProperty(name = "institution-send-mail.destination-mail")Boolean destinationMailTest,
                           @ConfigProperty(name = "institution-send-mail.destination-mail-test-address") String destinationMailTestAddress,
                           Mailer mailer) {
        this.azureBlobClient = azureBlobClient;
        this.objectMapper = objectMapper;
        this.senderMail = senderMail;
        this.destinationMailTest = destinationMailTest;
        this.destinationMailTestAddress = destinationMailTestAddress;
        this.mailer = mailer;
    }

    @Override
    public void sendMailWithFile(List<String> destinationMail, String templateName, Map<String, String> mailParameters, String prefixSubject, FileMailData fileMailData) {
        try {

            // Dev mode send mail to test digital address
            String destination = destinationMailTest
                    ? destinationMailTestAddress
                    : destinationMail.get(0);

            log.info(String.format("Sending mail to %s", destination));
            String template = azureBlobClient.getFileAsText(templateName);
            MailTemplate mailTemplate = objectMapper.readValue(template, MailTemplate.class);
            String html = StringSubstitutor.replace(mailTemplate.getBody(), mailParameters);

            final String subject = Optional.ofNullable(prefixSubject).map(value -> String.format("%s: %s", value, mailTemplate.getSubject())).orElse(mailTemplate.getSubject());

            Mail mail = Mail
                    .withHtml(destination, subject, html)
                    .setFrom(senderMail);

            if(Objects.nonNull(fileMailData)) {
                mail.addAttachment(fileMailData.getName(), fileMailData.getData(), fileMailData.getContentType());
            }

            mailer.send(mail);

            log.info(String.format("End of sending mail to %s, with subject %s", destination, subject));
        } catch (Exception e) {
            log.error(String.format("%s: %s", ERROR_DURING_SEND_MAIL, e.getMessage()));
            throw new GenericException(ERROR_DURING_SEND_MAIL);
        }
    }
}
