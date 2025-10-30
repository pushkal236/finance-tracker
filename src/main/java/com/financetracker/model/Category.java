package com.financetracker.model;

import java.util.Objects;

public final class Category {
	private final String name;

	public Category(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Category name cannot be blank");
		}
		this.name = name.trim();
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Category category = (Category) o;
		return name.equalsIgnoreCase(category.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name.toLowerCase());
	}
}
