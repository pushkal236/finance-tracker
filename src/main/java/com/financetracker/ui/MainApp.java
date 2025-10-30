package com.financetracker.ui;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.repo.Database;
import com.financetracker.repo.TransactionRepository;
import com.financetracker.service.TransactionService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private Database database;
	private TransactionService service;

	private final ObservableList<TransactionRow> tableData = FXCollections.observableArrayList();

	@Override
	public void start(Stage primaryStage) {
		database = new Database();
		service = new TransactionService(new TransactionRepository(database));

		TabPane tabs = new TabPane();
		tabs.getTabs().add(createAddTab());
		tabs.getTabs().add(createListTab());
		tabs.getTabs().add(createDashboardTab());
		tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		BorderPane root = new BorderPane(tabs);
		Scene scene = new Scene(root, 900, 600);
		primaryStage.setTitle("Finance Tracker");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void stop() {
		if (database != null) database.close();
	}

	private Tab createAddTab() {
		Tab tab = new Tab("Add");

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);
		form.setPadding(new Insets(16));

		DatePicker datePicker = new DatePicker(LocalDate.now());
		ComboBox<TransactionType> typeBox = new ComboBox<>(FXCollections.observableArrayList(TransactionType.values()));
		typeBox.getSelectionModel().select(TransactionType.EXPENSE);
		TextField amountField = new TextField();
		TextField categoryField = new TextField();
		TextField noteField = new TextField();
		Label status = new Label();

		Button saveBtn = new Button("Save");
		saveBtn.setOnAction(e -> {
			try {
				LocalDate date = datePicker.getValue();
				TransactionType type = typeBox.getValue();
				BigDecimal amount = new BigDecimal(amountField.getText().trim());
				Category category = new Category(categoryField.getText().trim());
				String note = noteField.getText().trim();
				service.addTransaction(new Transaction(0L, date, amount, type, category, note));
				status.setText("Saved");
				amountField.clear();
				categoryField.clear();
				noteField.clear();
			} catch (Exception ex) {
				status.setText("Error: " + ex.getMessage());
			}
		});

		int r = 0;
		form.add(new Label("Date"), 0, r); form.add(datePicker, 1, r++);
		form.add(new Label("Type"), 0, r); form.add(typeBox, 1, r++);
		form.add(new Label("Amount"), 0, r); form.add(amountField, 1, r++);
		form.add(new Label("Category"), 0, r); form.add(categoryField, 1, r++);
		form.add(new Label("Note"), 0, r); form.add(noteField, 1, r++);
		form.add(saveBtn, 1, r);

		VBox box = new VBox(10, form, status);
		box.setPadding(new Insets(10));
		tab.setContent(box);
		return tab;
	}

	private Tab createListTab() {
		Tab tab = new Tab("List");

		DatePicker fromPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
		DatePicker toPicker = new DatePicker(LocalDate.now());
		Button loadBtn = new Button("Load");

		TableView<TransactionRow> table = new TableView<>(tableData);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		TableColumn<TransactionRow, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		TableColumn<TransactionRow, String> typeCol = new TableColumn<>("Type");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
		TableColumn<TransactionRow, String> amountCol = new TableColumn<>("Amount");
		amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
		TableColumn<TransactionRow, String> categoryCol = new TableColumn<>("Category");
		categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
		TableColumn<TransactionRow, String> noteCol = new TableColumn<>("Note");
		noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
		table.getColumns().addAll(dateCol, typeCol, amountCol, categoryCol, noteCol);

		loadBtn.setOnAction(e -> reloadTable(fromPicker.getValue(), toPicker.getValue()));
		reloadTable(fromPicker.getValue(), toPicker.getValue());

		HBox filters = new HBox(10, new Label("From"), fromPicker, new Label("To"), toPicker, loadBtn);
		filters.setPadding(new Insets(10));
		filters.setAlignment(Pos.CENTER_LEFT);

		BorderPane pane = new BorderPane();
		pane.setTop(filters);
		pane.setCenter(table);
		tab.setContent(pane);
		return tab;
	}

	private Tab createDashboardTab() {
		Tab tab = new Tab("Dashboard");

		DatePicker monthPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
		Label incomeLbl = new Label("Income:  0");
		Label expenseLbl = new Label("Expense: 0");
		Label netLbl = new Label("Net:     0");
		ListView<String> byCat = new ListView<>();
		Button refreshBtn = new Button("Refresh");

		refreshBtn.setOnAction(e -> {
			LocalDate d = monthPicker.getValue();
			var report = service.getMonthlyReport(d.getYear(), d.getMonthValue());
			incomeLbl.setText("Income:  " + report.income().toPlainString());
			expenseLbl.setText("Expense: " + report.expense().toPlainString());
			netLbl.setText("Net:     " + report.net().toPlainString());
			byCat.getItems().setAll(formatByCategory(report.amountByCategory()));
		});

		refreshBtn.fire();

		VBox box = new VBox(10,
				new HBox(10, new Label("Month"), monthPicker, refreshBtn),
				incomeLbl, expenseLbl, netLbl,
				new Label("Expenses by category"),
				byCat
		);
		box.setPadding(new Insets(16));
		tab.setContent(box);
		return tab;
	}

	private void reloadTable(LocalDate from, LocalDate to) {
		List<Transaction> list = service.getTransactionsBetween(from, to);
		tableData.setAll(list.stream().map(TransactionRow::from).toList());
	}

	private List<String> formatByCategory(Map<String, java.math.BigDecimal> map) {
		return map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().toPlainString()).toList();
	}

	public static class TransactionRow {
		private final String date;
		private final String type;
		private final String amount;
		private final String category;
		private final String note;

		public TransactionRow(String date, String type, String amount, String category, String note) {
			this.date = date;
			this.type = type;
			this.amount = amount;
			this.category = category;
			this.note = note;
		}

		public static TransactionRow from(Transaction t) {
			return new TransactionRow(
					DATE_FMT.format(t.date()),
					t.type().name(),
					t.amount().toPlainString(),
					t.category().name(),
					t.note() == null ? "" : t.note()
			);
		}

		public String getDate() { return date; }
		public String getType() { return type; }
		public String getAmount() { return amount; }
		public String getCategory() { return category; }
		public String getNote() { return note; }
	}

	public static void main(String[] args) {
		launch(args);
	}
}
