package com.financetracker.repo;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

public class TransactionRepository {
	private final MongoCollection<Document> collection;

	public TransactionRepository(Database database) {
		MongoDatabase db = database.getDatabase();
		this.collection = db.getCollection("transactions");
	}

	public void insert(Transaction tx) {
		Document doc = new Document()
				.append("date", tx.date().toString())
				.append("amount", new Decimal128(tx.amount()))
				.append("type", tx.type().name())
				.append("category", tx.category().name())
				.append("note", tx.note());
		collection.insertOne(doc);
	}

	public List<Transaction> findBetween(LocalDate from, LocalDate to) {
		List<Transaction> result = new ArrayList<>();
		collection.find(and(gte("date", from.toString()), lte("date", to.toString())))
				.sort(ascending("date"))
				.forEach(doc -> result.add(mapDoc(doc)));
		return result;
	}

	public BigDecimal sumByType(TransactionType type) {
		List<Document> pipeline = List.of(
				new Document("$match", new Document("type", type.name())),
				new Document("$group", new Document("_id", null).append("total", new Document("$sum", "$amount")))
		);
		List<Document> out = collection.aggregate(pipeline).into(new ArrayList<>());
		if (out.isEmpty()) return BigDecimal.ZERO;
		Decimal128 dec = out.get(0).get("total", Decimal128.class);
		return dec == null ? BigDecimal.ZERO : dec.bigDecimalValue();
	}

	public Map<String, BigDecimal> sumByCategoryForMonth(int year, int month, TransactionType type) {
		String ymPrefix = String.format("%04d-%02d-", year, month);
		List<Document> pipeline = List.of(
				new Document("$match", new Document("type", type.name()).append("date", new Document("$regex", "^" + ymPrefix))),
				new Document("$group", new Document("_id", "$category").append("total", new Document("$sum", "$amount"))),
				new Document("$sort", new Document("total", -1))
		);
		Map<String, BigDecimal> result = new LinkedHashMap<>();
		collection.aggregate(pipeline).forEach(doc -> {
			String cat = doc.getString("_id");
			Decimal128 dec = doc.get("total", Decimal128.class);
			result.put(cat, dec == null ? BigDecimal.ZERO : dec.bigDecimalValue());
		});
		return result;
	}

	public BigDecimal sumForMonth(int year, int month, TransactionType type) {
		String ymPrefix = String.format("%04d-%02d-", year, month);
		List<Document> pipeline = List.of(
				new Document("$match", new Document("type", type.name()).append("date", new Document("$regex", "^" + ymPrefix))),
				new Document("$group", new Document("_id", null).append("total", new Document("$sum", "$amount")))
		);
		List<Document> out = collection.aggregate(pipeline).into(new ArrayList<>());
		if (out.isEmpty()) return BigDecimal.ZERO;
		Decimal128 dec = out.get(0).get("total", Decimal128.class);
		return dec == null ? BigDecimal.ZERO : dec.bigDecimalValue();
	}

	private static Transaction mapDoc(Document doc) {
		long id = 0L; // Mongo _id not exposed in this CLI
		LocalDate date = LocalDate.parse(doc.getString("date"));
		Decimal128 amt = doc.get("amount", Decimal128.class);
		BigDecimal amount = amt == null ? BigDecimal.ZERO : amt.bigDecimalValue();
		TransactionType type = TransactionType.valueOf(doc.getString("type"));
		Category category = new Category(doc.getString("category"));
		String note = doc.getString("note");
		return new Transaction(id, date, amount, type, category, note);
	}
}
