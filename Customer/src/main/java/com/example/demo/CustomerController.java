package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.camunda.zeebe.client.ZeebeClient;

@RestController
public class CustomerController {
	
	@Autowired
	ZeebeClient client;
	
	@PostMapping("/start")
	public String messaging() {
		client.newCreateInstanceCommand().bpmnProcessId("CustomerModel").latestVersion().send().join();
		return "Flow Started";
	}
	
	@GetMapping("/confirmationreceived")
	public String message() {
		
		String Message="receivedorder";
		String correlationkey="123";
		String str = Message+" "+correlationkey;
		client.newPublishMessageCommand().messageName(Message).correlationKey(correlationkey).send().join();
		return str;
	}
	
	@GetMapping("/orderfulfilled")
	public String orderfulfill() {
		
		String Message="orderfulfill";
		String correlationkey="12";
		String str = Message+" "+correlationkey;
		client.newPublishMessageCommand().messageName(Message).correlationKey(correlationkey).send().join();
		return str;
	}
	
	@GetMapping("/ordercancelled")
	public String ordercancel() {
		
		String Message = "cancel";
		String correlationkey = "23";
		String str = Message + " " + correlationkey;
		client.newPublishMessageCommand().messageName(Message).correlationKey(correlationkey).send().join();
		return str;
	}

}
