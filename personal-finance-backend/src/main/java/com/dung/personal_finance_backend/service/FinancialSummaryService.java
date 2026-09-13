package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.FinancialSummaryDto;
import com.dung.personal_finance_backend.dto.FinancialSummaryRequestDto;
import com.dung.personal_finance_backend.enums.PeriodType;

public interface FinancialSummaryService {
    FinancialSummaryDto getSummary(
            Long userId,
            FinancialSummaryRequestDto request
    );
}
