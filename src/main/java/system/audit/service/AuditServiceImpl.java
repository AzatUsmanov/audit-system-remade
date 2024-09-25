package system.audit.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import system.audit.dto.Audit;
import system.audit.dto.AuditDocumentFile;
import system.audit.dto.AuditView;

import system.audit.enums.DocumentFileType;
import system.audit.repositories.AuditDocumentFileRepository;
import system.audit.repositories.AuditRepository;
import system.audit.tool.converter.AuditConverter;
import system.audit.tool.converter.AuditViewConverter;
import system.audit.tool.exception.AuditNameNotUniqueException;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    private final Converter<AuditView,Audit> auditViewConverter;

    private final Converter<Audit, AuditView> auditConverter;

    @Override
    public void save(AuditView auditView) throws AuditNameNotUniqueException {
        if (auditRepository.existsByAuditName(auditView.getAuditName())) {
            throw new AuditNameNotUniqueException();
        }
        var audit = auditViewConverter.convert(auditView);
        auditRepository.save(audit);

        log.info("Saved : " +  audit);
    }

    @Override
    @Transactional
    public void updateById(AuditView auditView, Integer id) throws AuditNameNotUniqueException {
        var auditCurrent = auditViewConverter.convert(auditView);
        var auditPrevious = auditRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);

        if (auditRepository.existsByAuditName(auditView.getAuditName())
                && !auditPrevious.getAuditName().equals(auditCurrent.getAuditName())) {
            throw new AuditNameNotUniqueException();
        }

        updateAuditFields(auditPrevious, auditCurrent);
        auditRepository.save(auditCurrent);

        log.info("Updated with id = " + id + " : " +  auditCurrent);
    }

    @Override
    @Transactional
    public List<AuditView> getAllSorted(String sortField) {
        return auditRepository
                .findAll(Sort.by(Sort.Direction.ASC, sortField))
                .stream()
                .map(auditConverter::convert)
                .toList();
    }

    @Override
    @Transactional
    public AuditView getById(Integer id) {
        var audit =  auditRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        return auditConverter.convert(audit);
    }

    private void updateAuditFields(Audit previous, Audit current) {
        current.setId(previous.getId());
        current.setFindings(previous.getFindings());

        replaceFile(previous, current, DocumentFileType.AUDIT_REPORT);
        replaceFile(previous, current, DocumentFileType.PAYMENT_CONFIRMATION);

        current.setFiles(Stream.concat(
                        current.getFiles().stream(),
                        previous.getFiles().stream()
                                .filter(x ->
                                x.getDocumentFileType() != DocumentFileType.AUDIT_REPORT
                                        && x.getDocumentFileType() != DocumentFileType.PAYMENT_CONFIRMATION))
                        .toList());
    }

    private void replaceFile(Audit previous, Audit current, DocumentFileType documentFileType) {
        final var currentFile = getFilesByType(
                current.getFiles(), documentFileType);

        final var previousFile = getFilesByType(
                previous.getFiles(), documentFileType);

        if (!previousFile.isEmpty() && !currentFile.isEmpty()) {
            final var previousFileId = previousFile.getFirst().getId();
            currentFile.getFirst().setId(previousFileId);
        }

        if (currentFile.isEmpty() && !previousFile.isEmpty()) {
            current.setFiles(Stream.concat(
                    getFilesExcludingType(current.getFiles(), documentFileType).stream(),
                    previousFile.stream()
            ).toList());
        }
    }

    private List<AuditDocumentFile> getFilesByType(
            List<AuditDocumentFile> files, DocumentFileType documentFileType) {
        return files
                .stream()
                .filter(x -> x.getDocumentFileType() == documentFileType)
                .toList();
    }

    private List<AuditDocumentFile> getFilesExcludingType(
            List<AuditDocumentFile> files, DocumentFileType documentFileType) {
        return files
                .stream()
                .filter(x -> x.getDocumentFileType() != documentFileType)
                .toList();
    }

}

