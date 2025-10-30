package com.financetracker.repo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Database {
	private final MongoClient client;
	private final MongoDatabase database;

	public Database() {
		String uri = System.getenv().getOrDefault("MONGODB_URI", "mongodb://localhost:27017");
		String dbName = System.getenv().getOrDefault("MONGODB_DB", "finance_tracker");
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(uri))
				.build();
		this.client = MongoClients.create(settings);
		this.database = client.getDatabase(dbName);
	}

	public MongoDatabase getDatabase() {
		return database;
	}

	public void close() {
		client.close();
	}
}
