package it.pagopa.selfcare.institution.service;

import it.pagopa.selfcare.institution.model.FileMailData;

import java.util.List;
import java.util.Map;

public interface MailService {

    void sendMailWithFile(List<String> destinationMail, String templateName, Map<String, String> mailParameters, String prefixSubject, FileMailData fileMailData);
}
