package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.camunda.zeebe.client.ZeebeClient;

@RestController
public class OrderController {
	
	@Autowired
	ZeebeClient client;

	@KafkaListener(topics = "test1Topic",groupId = "group_id")
	public String messaging(String message) {
		client.newPublishMessageCommand().messageName(message).correlationKey("").send().join();
		return "Message";
	}
	
	@GetMapping("/paymentreceive")
	public String message() {
		
		String Message="payementrec";
		String correlationkey="2345";
		String str = Message+" "+correlationkey;
		client.newPublishMessageCommand().messageName(Message).correlationKey(correlationkey).send().join();
		return str;
	}
	
	@GetMapping("/cancelreceived")
	public String cancelreceive() {
		
		String Message = "cancelrec";
		String correlationkey = "234";
		String str = Message + " " + correlationkey;
		client.newPublishMessageCommand().messageName(Message).correlationKey(correlationkey).send().join();

		return str;
	}

}
