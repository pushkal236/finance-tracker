package com.financetracker.web;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.repo.Database;
import com.financetracker.repo.TransactionRepository;
import com.financetracker.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionController {
	private final TransactionService service;
	private final Database database;

	public TransactionController() {
		this.database = new Database();
		this.service = new TransactionService(new TransactionRepository(database));
	}

	@GetMapping("/transactions")
	public List<Transaction> list(
			@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return service.getTransactionsBetween(from, to);
	}

	@PostMapping("/transactions")
	public ResponseEntity<?> add(@RequestBody CreateTx body) {
		Transaction tx = new Transaction(
				0L,
				body.date,
				body.amount,
				body.type,
				new Category(body.category),
				body.note
		);
		service.addTransaction(tx);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/balance")
	public Map<String, BigDecimal> balance() {
		return Map.of("balance", service.getBalance());
	}

	@GetMapping("/reports/monthly")
	public TransactionService.MonthlyReport monthly(@RequestParam int year, @RequestParam int month) {
		return service.getMonthlyReport(year, month);
	}

	record CreateTx(LocalDate date, BigDecimal amount, TransactionType type, String category, String note) {}
}
