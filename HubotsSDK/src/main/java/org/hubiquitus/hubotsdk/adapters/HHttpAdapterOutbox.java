package org.hubiquitus.hubotsdk.adapters;

import org.hubiquitus.hapi.client.HMessageDelegate;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.hubiquitus.hubotsdk.AdapterOutbox;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HHttpAdapterOutbox extends AdapterOutbox {
	final Logger logger = LoggerFactory.getLogger(HHttpAdapterOutbox.class);
	
	private String host = "0.0.0.0";
	private int port = 80;
	private String path = "";
	
	public HHttpAdapterOutbox(){
		super();
	}
	@Override
	public void sendMessage(HMessage message, HMessageDelegate callback) {
		logger.debug("-------------------> message send to http.");
	}

	@Override
	public void setProperties(JSONObject properties) {
		if(properties != null){
			try {
				if (properties.has("host")){
					this.host = properties.getString("host");
				}
				if (properties.has("port")){
					this.port = properties.getInt("port");
				}
				if (properties.has("path")){
					this.path = properties.getString("path");
					if (this.path.contains("?")) {
						int interrogationIndex = this.path.indexOf("?");
						this.path = this.path.substring(interrogationIndex, this.path.length());
					}
				}
			} catch (Exception e) {
				logger.warn("message: ", e);
			}
		}

	}

	@Override
	public void start() {
		System.out.println("---> hHttpAdapterOutbox start!!");
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		System.out.println("---> hHttpAdapterOutbox stop!!");
		// TODO Auto-generated method stub

	}

}
