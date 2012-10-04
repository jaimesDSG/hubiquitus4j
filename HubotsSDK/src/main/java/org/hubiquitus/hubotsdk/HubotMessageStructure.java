package org.hubiquitus.hubotsdk;

import org.hubiquitus.hapi.client.HMessageDelegate;
import org.hubiquitus.hapi.hStructures.HMessage;

public class HubotMessageStructure {
	private HMessage message;
	private HMessageDelegate callback;
	
	public HubotMessageStructure(){
		
	}
	
	public HubotMessageStructure(HMessage message, HMessageDelegate callback){
		this.message = message;
		this.callback = callback;
	}
	public HMessage getMessage() {
		return message;
	}
	public void setMessage(HMessage message) {
		this.message = message;
	}
	public HMessageDelegate getCallback() {
		return callback;
	}
	public void setCallback(HMessageDelegate callback) {
		this.callback = callback;
	}
	
}
