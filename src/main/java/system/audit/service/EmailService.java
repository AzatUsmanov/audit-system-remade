package system.audit.service;

import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    void send(SimpleMailMessage message);

}
