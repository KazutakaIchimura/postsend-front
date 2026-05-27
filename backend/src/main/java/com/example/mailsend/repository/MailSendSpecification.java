package com.example.mailsend.repository;

import com.example.mailsend.domain.entity.MailSend;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class MailSendSpecification {

    private MailSendSpecification() {}

    public static Specification<MailSend> withFilters(
            LocalDate dateFrom, LocalDate dateTo, Long officeId, Long userId) {
        return Specification
                .where(dateFrom  != null ? sendMonthGreaterThanOrEqual(dateFrom)  : null)
                .and (dateTo    != null ? sendMonthLessThanOrEqual(dateTo)        : null)
                .and (officeId  != null ? hasOfficeId(officeId)                   : null)
                .and (userId    != null ? hasUserId(userId)                       : null);
    }

    private static Specification<MailSend> sendMonthGreaterThanOrEqual(LocalDate date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("sendMonth"), date);
    }

    private static Specification<MailSend> sendMonthLessThanOrEqual(LocalDate date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("sendMonth"), date);
    }

    private static Specification<MailSend> hasOfficeId(Long officeId) {
        return (root, query, cb) -> cb.equal(root.get("office").get("id"), officeId);
    }

    private static Specification<MailSend> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }
}
