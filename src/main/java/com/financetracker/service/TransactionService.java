package com.financetracker.service;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.repo.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TransactionService {
	private final TransactionRepository repository;

	public TransactionService(TransactionRepository repository) {
		this.repository = repository;
	}

	public void addTransaction(Transaction tx) {
		if (tx.amount().signum() <= 0) {
			throw new IllegalArgumentException("Amount must be positive");
		}
		repository.insert(tx);
	}

	public List<Transaction> getTransactionsBetween(LocalDate from, LocalDate to) {
		if (to.isBefore(from)) {
			throw new IllegalArgumentException("to must be on or after from");
		}
		return repository.findBetween(from, to);
	}

	public BigDecimal getBalance() {
		BigDecimal income = repository.sumByType(TransactionType.INCOME);
		BigDecimal expense = repository.sumByType(TransactionType.EXPENSE);
		return income.subtract(expense);
	}

	public MonthlyReport getMonthlyReport(int year, int month) {
		BigDecimal income = repository.sumForMonth(year, month, TransactionType.INCOME);
		BigDecimal expense = repository.sumForMonth(year, month, TransactionType.EXPENSE);
		Map<String, BigDecimal> byCatExpense = repository.sumByCategoryForMonth(year, month, TransactionType.EXPENSE);
		return new MonthlyReport(income, expense, income.subtract(expense), byCatExpense);
	}

	public record MonthlyReport(
			BigDecimal income,
			BigDecimal expense,
			BigDecimal net,
			Map<String, BigDecimal> amountByCategory
	) {}
}
