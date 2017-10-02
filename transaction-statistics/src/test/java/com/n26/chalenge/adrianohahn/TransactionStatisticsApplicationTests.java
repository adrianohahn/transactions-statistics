package com.n26.chalenge.adrianohahn;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.chalenge.adrianohahn.transaction.Transaction;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=TransactionStatisticsApplication.class)
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
@WebAppConfiguration
public class TransactionStatisticsApplicationTests {

	@Autowired
	private WebApplicationContext ctx;
	 
	private MockMvc mockMvc;
	
	@Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

	@Test
	public void testCreateTransaction() throws Exception {
		final long interval = 50;
        final long time = (System.currentTimeMillis() / 1000 - interval);
        final Transaction transaction = new Transaction();
        
        transaction.setTimestamp(time);
        transaction.setAmount(0d);
        
        mockMvc.perform(post("/transaction")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(convertObjectToJsonBytes(transaction)))
        		.andExpect(status().isCreated());
        
        mockMvc.perform(get("/statistics"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		    .andExpect(status().isOk())
		    .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1));
        
        Thread.sleep(((time + 60) * 1000 + 1) - System.currentTimeMillis());
        
        mockMvc.perform(get("/statistics"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.count").value(0));
	}
	
	@Test
	public void testOldTransaction() throws Exception {
        final long time = (System.currentTimeMillis() / 1000) - 61;
        final Transaction transaction = new Transaction();
        
        transaction.setTimestamp(time);
        transaction.setAmount(0d);
        
        mockMvc.perform(post("/transaction")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(convertObjectToJsonBytes(transaction)))
        		.andExpect(status().isNoContent());
	}
	
	@Test
	public void testStatistics() throws Exception {
		final Transaction firstTransaction = new Transaction();
        final Transaction secondTransaction = new Transaction();
        final Transaction thirdTransaction = new Transaction();
        final Transaction fourthTransaction = new Transaction();
        
        final long interval = 50;
        final long timeFirstAndSecondTransaction = (System.currentTimeMillis() / 1000 - interval);
        final long timeThirdAndFourthTransaction = (System.currentTimeMillis() / 1000);
        
        final double amountFirstTransaction = 10.5d;
        final double amountSecondTransaction = 1d;
        final double amountThirdTransaction = 1.1d;
        final double amountFourthTransaction = 10.4d;
        
        final double initialSum = amountFirstTransaction + amountSecondTransaction + amountThirdTransaction
        		+ amountFourthTransaction;
        final double initialAverage = initialSum / 4;
        
        final double lastSum = amountThirdTransaction + amountFourthTransaction;
        final double lastAverage = lastSum / 2;
        
        firstTransaction.setTimestamp(timeFirstAndSecondTransaction);
		firstTransaction.setAmount(amountFirstTransaction);
        
        secondTransaction.setTimestamp(timeFirstAndSecondTransaction);
        secondTransaction.setAmount(amountSecondTransaction);
        
        thirdTransaction.setTimestamp(timeThirdAndFourthTransaction);
        thirdTransaction.setAmount(amountThirdTransaction);
        
        fourthTransaction.setTimestamp(timeThirdAndFourthTransaction);
        fourthTransaction.setAmount(amountFourthTransaction);
        
        mockMvc.perform(post("/transaction")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(convertObjectToJsonBytes(firstTransaction)))
        		.andExpect(status().isCreated());
        
        mockMvc.perform(post("/transaction")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(convertObjectToJsonBytes(secondTransaction)))
        		.andExpect(status().isCreated());
        
        mockMvc.perform(post("/transaction")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(convertObjectToJsonBytes(thirdTransaction)))
        		.andExpect(status().isCreated());
        
        mockMvc.perform(post("/transaction")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(convertObjectToJsonBytes(fourthTransaction)))
        		.andExpect(status().isCreated());
        
        mockMvc.perform(get("/statistics"))
        		.andExpect(status().isOk())
        		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$.count", is(4)))
            .andExpect(jsonPath("$.sum", is(initialSum)))
            .andExpect(jsonPath("$.avg", is(initialAverage)))
            .andExpect(jsonPath("$.max", is(amountFirstTransaction)))
            .andExpect(jsonPath("$.min", is(amountSecondTransaction)));
        
        Thread.sleep(((timeFirstAndSecondTransaction + 60) * 1000 + 1) - System.currentTimeMillis());
        
        mockMvc.perform(get("/statistics"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
	    .andExpect(jsonPath("$").exists())
	    .andExpect(jsonPath("$.count", is(2)))
	    .andExpect(jsonPath("$.sum", is(lastSum)))
	    .andExpect(jsonPath("$.avg", is(lastAverage)))
	    .andExpect(jsonPath("$.max", is(amountFourthTransaction)))
	    .andExpect(jsonPath("$.min", is(amountThirdTransaction)));
	}
	 
    private byte[] convertObjectToJsonBytes(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }
}
