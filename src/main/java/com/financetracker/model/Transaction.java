package com.financetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
		long id,
		LocalDate date,
		BigDecimal amount,
		TransactionType type,
		Category category,
		String note
) {
	public Transaction {
		if (date == null) throw new IllegalArgumentException("date required");
		if (amount == null) throw new IllegalArgumentException("amount required");
		if (type == null) throw new IllegalArgumentException("type required");
		if (category == null) throw new IllegalArgumentException("category required");
	}
}
