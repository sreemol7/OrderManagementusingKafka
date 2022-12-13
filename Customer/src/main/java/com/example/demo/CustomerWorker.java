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

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
public class CustomerWorker {
	
	final RestTemplate rest = new RestTemplate();

	private static final String TOPIC1 = "test1Topic";

	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;

	public void sendMessage(String message){
		this.kafkaTemplate.send(TOPIC1,message);

	}

	@Bean
	public NewTopic createTopic(){
		return new NewTopic(TOPIC1,3,(short) 1);

	}
	
	@ZeebeWorker(type = "sendorder", name = "Send order to fullfillment")
	public void getSendorderfullfillment(final JobClient client, final ActivatedJob job) {

		System.out.println("Send order to fullfillment Worker Started");

		Map<String, Object> map = job.getVariablesAsMap();

		String message="orderreceived";
		kafkaTemplate.send(TOPIC1,message);

		map.put("getSendorderfullfillment", message);
		System.out.println("Send order to fullfillment work completed");


		client.newCompleteCommand(job.getKey()).variables(map).send().join();
			


	}

	@ZeebeWorker(type = "sendPayment", name = "Send payment")
	public void getSendpayment(final JobClient client, final ActivatedJob job) {

		System.out.println("Send payment Started");
		Map<String, Object> map = job.getVariablesAsMap();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(headers);
		String url = "http://localhost:5051/paymentreceive";
		ResponseEntity<String> response = rest.getForEntity(url, String.class);
		map.put("getSendpayment", response);
		
		System.out.println("Send payment completed");

		client.newCompleteCommand(job.getKey()).variables(response).send().join();

	}

	@ZeebeWorker(type = "cancel", name = "send cancellation")
	public void getSendcancel(final JobClient client, final ActivatedJob job) {
		
		System.out.println("cancelWorker Started");
		
		Map<String, Object> map = job.getVariablesAsMap();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(headers);
		String url = "http://localhost:5051/cancelreceived";
		ResponseEntity<String> response = rest.getForEntity(url, String.class);
		map.put("getSendpayment", response);
		
		System.out.println("cancelworker" + response);

		client.newCompleteCommand(job.getKey()).variables(response).send().join();

	}

}
