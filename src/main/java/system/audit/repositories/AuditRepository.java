package system.audit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import system.audit.dto.Audit;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Integer> {

    boolean existsByAuditName(String auditName);

}
