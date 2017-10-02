package com.n26.chalenge.adrianohahn.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {
	
	@Autowired
	private TransactionService transactionService;
	
	@RequestMapping(method=RequestMethod.POST, consumes="application/json")
	public ResponseEntity<?> transaction(@RequestBody Transaction transaction) {
		final long now = System.currentTimeMillis() / 1000;
		if (transaction.getTimestamp() < (now - 60)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		transactionService.add(transaction);
		return new ResponseEntity<>(HttpStatus.CREATED);
		
	}
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	@ResponseBody
	public ResponseEntity<TransactionSummary> statistics() {
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<TransactionSummary>(transactionService.getStatisticSummary(),httpHeaders,HttpStatus.OK);
//		return transactionService.getStatisticSummary();
		
	}
	
}
