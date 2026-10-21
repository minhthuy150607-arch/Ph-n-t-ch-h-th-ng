package com.library.repository;

import com.library.model.LibraryRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibraryRuleRepository extends JpaRepository<LibraryRule, Long> {
    Optional<LibraryRule> findByRuleKey(String ruleKey);
}
