package org.infinity.sixtalebackend.domain.rule.repository;

import org.infinity.sixtalebackend.domain.rule.domain.CommonAction;
import org.infinity.sixtalebackend.domain.rule.domain.Rule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonActionRepository extends JpaRepository<CommonAction, Long> {
    List<CommonAction> findByRule(Rule rule);
    @Query("SELECT c FROM CommonAction c JOIN FETCH c.rule WHERE c.rule.id = :ruleID")
    List<CommonAction> findByRuleIdWithFetch(@Param("ruleID") Long ruleID);
}
