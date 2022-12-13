package com.example.demo;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
public class OrderWorker {
	
	@Autowired
	ZeebeClient zeebeClient;
	
	final RestTemplate rest = new RestTemplate();

	private static final String TOPIC2 = "test2Topic";

	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;

	public void sendMessage(String message){
		this.kafkaTemplate.send(TOPIC2,message);
	}

	@Bean
	public NewTopic createTopic(){
		return new NewTopic(TOPIC2,3,(short) 1);
	}

	@ZeebeWorker(type = "sendorder2", name = "Send Order Confirmation")
	public void getSendOrderConfirmation(final JobClient client, final ActivatedJob job) {

		System.out.println("Send Order Confirmation Started");

		Map<String, Object> map = job.getVariablesAsMap();

		String message="confirmationreceived";
		kafkaTemplate.send(TOPIC2,message);

		map.put("getSendOrderConfirmation", message);
		System.out.println("Send Order Confirmation completed");


		client.newCompleteCommand(job.getKey()).variables(map).send().join();

	}
	@ZeebeWorker(type = "fulfill", name = "order fulfilled")
	public void getSendOrderfulfillment(final JobClient client, final ActivatedJob job) {
		
		System.out.println("order fulfilled Start");
		
		Map<String, Object> map = job.getVariablesAsMap();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(headers);
		String url = "http://localhost:5050/orderfulfilled";
		ResponseEntity<String> response = rest.getForEntity(url,String.class);
		map.put("getorderfulfilled", response);
		
		System.out.println("order fulfilled completed");

		client.newCompleteCommand(job.getKey()).variables(response).send().join();

	}
	
	@ZeebeWorker(type = "cancelled", name = "order cancelled")
	public void getSendOrderCancellation(final JobClient client, final ActivatedJob job) {
		
		System.out.println("order cancelled Start");
		
		Map<String, Object> map = job.getVariablesAsMap();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(headers);
		String url = "http://localhost:5050/ordercancelled";
		ResponseEntity<String> response = rest.getForEntity(url,String.class);
		map.put("getorderfulfilled", response);
		
		System.out.println("order cancelled completed");

		client.newCompleteCommand(job.getKey()).variables(response).send().join();

	}

}
