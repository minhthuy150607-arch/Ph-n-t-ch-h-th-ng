package com.library.service;

import com.library.model.LibraryRule;
import com.library.repository.LibraryRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final LibraryRuleRepository ruleRepository;

    public List<LibraryRule> getAllRules() {
        return ruleRepository.findAll();
    }

    @Transactional
    public void updateRule(Long id, String ruleValue, String description) {
        LibraryRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quy định ID: " + id));
        rule.setRuleValue(ruleValue);
        if (description != null) {
            rule.setDescription(description);
        }
        ruleRepository.save(rule);
    }
}
