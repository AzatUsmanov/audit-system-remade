package system.audit.controller;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
import system.audit.enums.FindingField;
import system.audit.enums.FindingGradationType;
import system.audit.service.FieldValidator;
import system.audit.service.FindingService;
import system.audit.tool.exception.FindingNameNotUniqueException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/finding")
public class FindingController {

    private final FindingService findingService;

    private final FieldValidator validator;

    @ModelAttribute("findingFields")
    public List<FindingField> fields() {
        return Arrays.stream(FindingField.values()).toList();
    }

    @ModelAttribute("findingGradationType")
    public List<FindingGradationType> findingGradationType() {
        return Arrays.stream(FindingGradationType.values()).toList();
    }

    @GetMapping("/create-form/{auditId}")
    public ModelAndView getCreateFindingForm(@PathVariable("auditId") Integer auditId) {
        final var modelAndView = new ModelAndView("finding/create-form.html");
        modelAndView.getModel().put("finding", new FindingView());
        modelAndView.getModel().put("auditId", auditId);
        modelAndView.getModel().put("errors", new HashMap<String, String>());
        return modelAndView;
    }

    @PostMapping("/create-form")
    public ModelAndView createFinding(@ModelAttribute("finding") FindingView findingView,
                              @RequestParam("auditId") Integer auditId) throws FindingNameNotUniqueException {
        var errors = validator.validate(findingView);

        if (!errors.isEmpty()) {
            final var modelAndView = new ModelAndView("finding/create-form.html");
            modelAndView.getModel().put("auditId", auditId);
            modelAndView.getModel().put("finding", new FindingView());
            modelAndView.getModel().put("errors", errors);
            return modelAndView;
        }
        findingService.save(findingView, auditId);
        return new ModelAndView("redirect:/finding/create-form/" + auditId);
    }

    @GetMapping("/list")
    public ModelAndView getFindingList(@RequestParam(defaultValue = "id") String sortFindingField,
                                       @RequestParam Integer auditId) {
        final var modelAndView = new ModelAndView("finding/list.html");
        final var findings = findingService.getAllSortedByAuditId(sortFindingField, auditId);
        modelAndView.getModel().put("findingList", findings);
        modelAndView.getModel().put("auditId", auditId);
        return modelAndView;
    }

    @GetMapping("/one/{id}")
    public ModelAndView getFindingById(@PathVariable("id") Integer id) {
        final var modelAndView = new ModelAndView("finding/one.html");
        final var findingView =  findingService.getById(id);
        modelAndView.getModel().put("finding", findingView);
        return modelAndView;
    }

    @GetMapping("/update-form/{id}")
    public ModelAndView getUpdateFindingFormById(@PathVariable("id") Integer id){
        final var modelAndView = new ModelAndView("finding/update-form.html");
        final var findingView =  findingService.getById(id);
        modelAndView.getModel().put("finding", findingView);
        modelAndView.getModel().put("errors", new HashMap<String, String>());
        return modelAndView;
    }

    @PostMapping("/update-form")
    public ModelAndView updateFinding(@ModelAttribute("finding") FindingView findingView) throws FindingNameNotUniqueException {
        final var errors = validator.validate(findingView);
        final var findingId = findingView.getId();

        if (!errors.isEmpty()) {
            final var modelAndView = new ModelAndView("finding/update-form.html");
            final var newFindingView =  findingService.getById(findingId);
            modelAndView.getModel().put("finding", newFindingView);
            modelAndView.getModel().put("errors", errors);
            return modelAndView;
        }

        findingService.updateByFindingId(findingView, findingId);
        return new ModelAndView("redirect:/finding/update-form/" + findingView.getId());
    }

}
