package com.financetracker;

import com.financetracker.repo.Database;
import com.financetracker.repo.TransactionRepository;
import com.financetracker.service.TransactionService;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static void main(String[] args) {
		Database database = new Database();
		TransactionRepository repository = new TransactionRepository(database);
		TransactionService service = new TransactionService(repository);

		Scanner scanner = new Scanner(System.in);
		System.out.println("Finance Tracker (Java + MongoDB)\n");
		System.out.println("Using MONGODB_URI=\"" + System.getenv().getOrDefault("MONGODB_URI", "mongodb://localhost:27017") + "\" and MONGODB_DB=\"" + System.getenv().getOrDefault("MONGODB_DB", "finance_tracker") + "\"");
		System.out.println();

		while (true) {
			printMenu();
			System.out.print("Select option: ");
			String choice = scanner.nextLine().trim();
			switch (choice) {
				case "1" -> addTransaction(scanner, service, TransactionType.INCOME);
				case "2" -> addTransaction(scanner, service, TransactionType.EXPENSE);
				case "3" -> listTransactions(scanner, service);
				case "4" -> showBalance(service);
				case "5" -> monthlyReport(scanner, service);
				case "0" -> {
					System.out.println("Goodbye!");
					return;
				}
				default -> System.out.println("Invalid choice. Try again.\n");
			}
		}
	}

	private static void printMenu() {
		System.out.println("1) Add income");
		System.out.println("2) Add expense");
		System.out.println("3) List transactions (by date range)");
		System.out.println("4) Show current balance");
		System.out.println("5) Monthly report");
		System.out.println("0) Exit");
	}

	private static void addTransaction(Scanner scanner, TransactionService service, TransactionType type) {
		try {
			System.out.print("Date (yyyy-MM-dd, blank for today): ");
			String dateStr = scanner.nextLine().trim();
			LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr, DATE_FMT);

			System.out.print("Amount: ");
			BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

			System.out.print("Category (e.g., Salary, Food, Rent): ");
			String categoryName = scanner.nextLine().trim();
			Category category = new Category(categoryName);

			System.out.print("Note (optional): ");
			String note = scanner.nextLine().trim();

			Transaction tx = new Transaction(0L, date, amount, type, category, note);
			service.addTransaction(tx);
			System.out.println("Saved.\n");
		} catch (Exception ex) {
			System.out.println("Failed to add transaction: " + ex.getMessage() + "\n");
		}
	}

	private static void listTransactions(Scanner scanner, TransactionService service) {
		try {
			System.out.print("From date (yyyy-MM-dd, blank for 1970-01-01): ");
			String fromStr = scanner.nextLine().trim();
			LocalDate from = fromStr.isEmpty() ? LocalDate.of(1970, 1, 1) : LocalDate.parse(fromStr, DATE_FMT);
			System.out.print("To date (yyyy-MM-dd, blank for today): ");
			String toStr = scanner.nextLine().trim();
			LocalDate to = toStr.isEmpty() ? LocalDate.now() : LocalDate.parse(toStr, DATE_FMT);

			List<Transaction> transactions = service.getTransactionsBetween(from, to);
			if (transactions.isEmpty()) {
				System.out.println("No transactions found.\n");
				return;
			}

			System.out.printf(Locale.US, "%n%-12s %-8s %-12s %-15s %s%n", "Date", "Type", "Amount", "Category", "Note");
			for (Transaction t : transactions) {
				System.out.printf(Locale.US, "%-12s %-8s %-12s %-15s %s%n",
						DATE_FMT.format(t.date()),
						t.type(),
						t.amount().toPlainString(),
						t.category().name(),
						t.note() == null ? "" : t.note());
			}
			System.out.println();
		} catch (Exception ex) {
			System.out.println("Failed to list transactions: " + ex.getMessage() + "\n");
		}
	}

	private static void showBalance(TransactionService service) {
		BigDecimal balance = service.getBalance();
		System.out.println("Current balance: " + balance.toPlainString() + "\n");
	}

	private static void monthlyReport(Scanner scanner, TransactionService service) {
		try {
			System.out.print("Year (e.g., 2025): ");
			int year = Integer.parseInt(scanner.nextLine().trim());
			System.out.print("Month (1-12): ");
			int month = Integer.parseInt(scanner.nextLine().trim());

			TransactionService.MonthlyReport report = service.getMonthlyReport(year, month);
			System.out.println();
			System.out.println("Income:  " + report.income().toPlainString());
			System.out.println("Expense: " + report.expense().toPlainString());
			System.out.println("Net:     " + report.net().toPlainString());
			System.out.println();
			System.out.println("By category:");
			report.amountByCategory().forEach((cat, amt) ->
					System.out.println(" - " + cat + ": " + amt.toPlainString())
			);
			System.out.println();
		} catch (Exception ex) {
			System.out.println("Failed to generate report: " + ex.getMessage() + "\n");
		}
	}
}
