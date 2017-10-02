package com.n26.chalenge.adrianohahn.transaction;

import java.util.HashSet;
import java.util.Set;

public class TransactionSummary {
	
	private final Set<Transaction> transactions = new HashSet<Transaction>();
	
	private Long count = 0l;
		
	private Double sum = 0d;
	
	private Double max = 0d;
	
	private Double min = 0d;

	private int minCount = 0;

	private int maxCount = 0;

	public Long getCount() {
		return count;
	}

	public Double getAvg() {
		if (transactions.size() == 0) return 0d;
		return this.sum / this.count;
	}

	public Double getSum() {
		return sum;
	}

	public Double getMax() {
		return max;
	}

	public Double getMin() {
		return min;
	}

	public void addTransaction(Transaction transaction) {
		synchronized (this.transactions) {
			this.transactions.add(transaction);
			incrementQuantity();
			incrementAmount(transaction);
			calculateMin(transaction);
			calculateMax(transaction);
		}
	}
	
	public void removeTransaction(Transaction transaction) {
		synchronized (this.transactions) {
			this.transactions.remove(transaction);
			decrementQuantity();
			decrementAmount(transaction);
			updateMinAndMax();
		}
		
	}

	private void decrementAmount(Transaction transaction) {
		this.sum -= transaction.getAmount();
	}
	
	private void incrementQuantity() {
		this.count++;
	}
	
	private void decrementQuantity() {
		this.count--;
	}
	
	private void incrementAmount(Transaction transaction) {
		this.sum += transaction.getAmount();
	}

	private void updateMinAndMax() {
		boolean minOutdated = true;
		boolean maxOutdated = true;
		
		if (minCount > 1) {
			minOutdated = false;
			this.minCount--;
		}
		
		if (maxCount > 1) {
			maxOutdated = false;
			this.maxCount--;
		}
		
		if (minOutdated || maxOutdated) {
			this.min = minOutdated ? 0d : this.min;
			this.minCount = minOutdated ? 0 : this.minCount;
			this.max = maxOutdated ? 0d : this.max;
			this.maxCount = maxOutdated ? 0 : this.maxCount;
			
			for (Transaction transaction : this.transactions) {
				if (minOutdated) calculateMin(transaction);
				if (maxOutdated) calculateMax(transaction);
			}
		}
	}

	private void calculateMin(Transaction transaction) {
		if (transaction.getAmount() < this.min || this.minCount == 0) {
			this.min = transaction.getAmount();
			this.minCount = 1;
		} else if (this.min.equals(transaction.getAmount())) {
			this.minCount++;
		}
	}
	
	private void calculateMax(Transaction transaction) {
		if (transaction.getAmount() > this.max || this.maxCount == 0) {
			this.max = transaction.getAmount();
			this.maxCount = 1;
		} else if (this.max.equals(transaction.getAmount())) {
			this.maxCount++;
		}
	}
	
}
