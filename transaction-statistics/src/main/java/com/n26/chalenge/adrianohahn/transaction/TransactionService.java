package com.n26.chalenge.adrianohahn.transaction;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Service;

@Service
public class TransactionService {
	
	private final TransactionSummary statisticSummary = new TransactionSummary();

	public void add(Transaction transaction) {
		synchronized (statisticSummary) {
			statisticSummary.addTransaction(transaction);
			addTransactionTimer(transaction);
		}
	}

	public TransactionSummary getStatisticSummary() {
		return statisticSummary;
	}

	private void addTransactionTimer(Transaction transaction) {
		final long interval = (transaction.getTimestamp() + 60) * 1000 - System.currentTimeMillis();
		Timer timer = new Timer();
		timer.schedule(new TransactionCleaner(transaction, this.statisticSummary), interval);
	}
	
	private class TransactionCleaner extends TimerTask {

		private Transaction transaction;
		private TransactionSummary summary;

		public TransactionCleaner(Transaction transaction, TransactionSummary summary) {
			super();
			this.transaction = transaction;
			this.summary = summary;
		}

		@Override
		public void run() {
			System.out.println("TransactionCleaner::run");
			summary.removeTransaction(transaction);
		}
		
	}

}
