package me.mamre.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.*;

public interface RuleVersionRepo extends JpaRepository<RuleVersion, Long> {

    @Query("select coalesce(max(r.version),0) from RuleVersion r where r.decisionKey = :key")
    int maxVersion(String key);

    List<RuleVersion> findByDecisionKeyOrderByVersionAsc(String key);

    Optional<RuleVersion> findFirstByDecisionKeyAndEnabledIsTrueOrderByVersionDesc(String key);

    Optional<RuleVersion> findFirstByDecisionKeyAndVersion(String key, Integer version);

    @Query("select distinct r.decisionKey from RuleVersion r")
    List<String> distinctKeys();

    List<RuleVersion> findByUploadedAtAfterOrderByUploadedAtAsc(Instant since);
}
