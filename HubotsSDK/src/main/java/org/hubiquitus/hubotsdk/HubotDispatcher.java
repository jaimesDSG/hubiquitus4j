package org.hubiquitus.hubotsdk;

import java.util.ArrayList;

import org.hubiquitus.hapi.client.HMessageDelegate;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HubotDispatcher {
	final  Logger logger = LoggerFactory.getLogger(HubotDispatcher.class);
	
	private static final String HUBOT_ADAPTER_OUTBOX = "hubotAdapterOutbox";
    // TODO vérifier la pertinence d'un ArrayList vs. une Map
	private ArrayList<String> adapterOutboxActors;
	
	public HubotDispatcher(ArrayList<String> adapterOutboxActors){
        this.adapterOutboxActors = adapterOutboxActors;
	}
	
	public  void dispatcher(HMessage msg, HMessageDelegate callback){
		if (msg != null) {
			HubotStructure hubotStruct = new HubotStructure(msg, callback);
			String msgActor = msg.getActor();
			if (!adapterOutboxActors.contains(msgActor)) {
				put(HUBOT_ADAPTER_OUTBOX, hubotStruct);
			} else {
				put(msgActor, hubotStruct);
			}
		}
	}
	
	private void put(String adapterActor, HubotStructure hubotStruct) {
		String route = "seda:" + adapterActor;
		try {
			ProducerTemplateSingleton.getProducerTemplate().sendBody(route,hubotStruct);
		} catch (Exception e) {
			logger.debug("message: ", e);
		}
				
	}
}
