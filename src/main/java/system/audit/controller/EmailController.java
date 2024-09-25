package system.audit.controller;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import system.audit.service.EmailService;

import java.util.HashMap;

@Controller
@AllArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/")
    public ModelAndView home() {
        return new ModelAndView("redirect:/audit/list");
    }

    @GetMapping("/email")
    public ModelAndView getEmailForm() {
        final var modelAndView = new ModelAndView("email.html");
        modelAndView.getModel().put("message", new SimpleMailMessage());
        modelAndView.getModel().put("errors", new HashMap<String, String>());
        return modelAndView;
    }

    @PostMapping("/email")
    public ModelAndView send(@ModelAttribute("message") SimpleMailMessage message) {
        emailService.send(message);
        return new ModelAndView("redirect:/email");
    }

}
