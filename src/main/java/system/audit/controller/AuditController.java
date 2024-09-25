package system.audit.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import system.audit.dto.AuditView;
import system.audit.dto.FindingView;
import system.audit.enums.AuditField;
import system.audit.enums.AuditType;
import system.audit.enums.CertificationBodyType;
import system.audit.service.AuditService;
import system.audit.service.FieldValidator;
import system.audit.tool.exception.AuditNameNotUniqueException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/audit")
public class AuditController {

    private final AuditService auditService;

    private final FieldValidator validator;

    @ModelAttribute("auditFields")
    public List<AuditField> auditFields() {
        return Arrays.stream(AuditField.values()).toList();
    }

    @ModelAttribute("certificationBodyType")
    public List<CertificationBodyType> certificationBodyType() {
        return Arrays.stream(CertificationBodyType.values()).toList();
    }

    @ModelAttribute("auditType")
    public List<AuditType> auditType() {
        return Arrays.stream(AuditType.values()).toList();
    }

    @GetMapping("/create-form")
    public ModelAndView getCreateAuditForm(){
        final var modelAndView = new ModelAndView("audit/create-form.html");
        modelAndView.getModel().put("audit", new AuditView());
        modelAndView.getModel().put("errors", new HashMap<String, String>());
        return modelAndView;
    }

    @PostMapping("/create-form")
    public ModelAndView createAudit(@ModelAttribute("audit") AuditView audit) throws AuditNameNotUniqueException {
        final var errors = validator.validate(audit);

        if (!errors.isEmpty()) {
            final var modelAndView = new ModelAndView("audit/create-form.html");
            modelAndView.getModel().put("audit", new AuditView());
            modelAndView.getModel().put("errors", errors);
            return modelAndView;
        }
        auditService.save(audit);
        return new ModelAndView("redirect:/audit/create-form");
    }

    @GetMapping("/update-form/{id}")
    public ModelAndView getUpdateAuditFormById(@PathVariable Integer id) {
        final var modelAndView = new ModelAndView("audit/update-form.html");
        final var auditView =  auditService.getById(id);
        modelAndView.getModel().put("audit", auditView);
        modelAndView.getModel().put("errors", new HashMap<String, String>());
        return modelAndView;
    }

    @PostMapping("/update-form")
    public ModelAndView updateAudit(@ModelAttribute("audit") AuditView audit) throws AuditNameNotUniqueException {
        final var auditId = audit.getId();
        final var errors = validator.validate(audit);

        if (!errors.isEmpty()) {
            final var modelAndView = new ModelAndView("audit/update-form.html");
            final var newAuditView =  auditService.getById(auditId);
            modelAndView.getModel().put("audit", newAuditView);
            modelAndView.getModel().put("errors", errors);
            return modelAndView;
        }
        auditService.updateById(audit, auditId);
        return new ModelAndView("redirect:/audit/update-form/" + auditId);
    }

    @GetMapping("/list")
    public ModelAndView getAuditList(@RequestParam(defaultValue = "id") String sortAuditField) {
        final var modelAndView = new ModelAndView("audit/list.html");
        final var audits = auditService.getAllSorted(sortAuditField);
        modelAndView.getModel().put("auditList", audits);
        return modelAndView;
    }

    @GetMapping("/one/{id}")
    public ModelAndView getAuditById(@PathVariable Integer id) {
        final var modelAndView = new ModelAndView("audit/one.html");
        final var auditView =  auditService.getById(id);
        modelAndView.getModel().put("audit", auditView);
        return modelAndView;
    }

}
