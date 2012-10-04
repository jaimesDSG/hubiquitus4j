/*
 * Copyright (c) Novedia Group 2012.
 *
 *     This file is part of Hubiquitus.
 *
 *     Hubiquitus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Hubiquitus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.hubiquitus.HelloBot;


import org.hubiquitus.hapi.client.HClient;
import org.hubiquitus.hapi.client.HMessageDelegate;
import org.hubiquitus.hapi.exceptions.MissingAttrException;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.hubiquitus.hubotsdk.Hubot;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class HelloHubot extends Hubot{

	final Logger logger = LoggerFactory.getLogger(HelloHubot.class);
	
	private MessageDelegate1 callback1 = new MessageDelegate1();
	private MessageDelegate2 callback2 = new MessageDelegate2();
	
	private class MessageDelegate1 implements HMessageDelegate{

		@Override
		public void onMessage(HMessage message) {
			logger.info("callback1 called by message: \n" + message);
		}
		
	}
	
	private class MessageDelegate2 implements HMessageDelegate{

		@Override
		public void onMessage(HMessage message) {
			logger.info("callback2 called by message: \n" + message);
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		HelloHubot hubot = new HelloHubot();
		hubot.start();
		
	}
	
	@Override
	public void init(HClient hclient) {
		initialized();
	}
	
	@Override
	protected void inProcessMessage(HMessage messageIncoming) {
		logger.info("--->" + messageIncoming);
		if (!("hresult").equalsIgnoreCase(messageIncoming.getType()) && !("text").equalsIgnoreCase(messageIncoming.getType())) {
			HMessage cmdMessage1 = null;
			try {
				cmdMessage1 = buildCommand("hnode@localhost", "hgetsubscriptions", null, null);
				cmdMessage1.setTimeout(30000);
			} catch (MissingAttrException e) {
				logger.warn("message: ",e);
			}
			HMessage cmdMessage2 = null;
			try {
				cmdMessage2 = buildCommand("#test@localhost", "hgetlastmessages", new JSONObject(), null);
				cmdMessage2.setTimeout(30000);
			} catch (MissingAttrException e) {
				logger.warn("message: ",e);
			}
			send(cmdMessage1, this.callback1);
			send(cmdMessage2, this.callback2);
		}else{
			HMessage message = new HMessage();
			message.setType("hello");
			JSONObject jsonObj = messageIncoming.getPayloadAsJSONObject();
			String name = "Hello ";
			try {
				 name += jsonObj.getString("text");
			} catch (JSONException e) {
				logger.error(e.toString());
			}
			JSONObject payload = new JSONObject();
			try {
				payload.put("text", name);
			} catch (JSONException e) {
				logger.debug(e.toString());
			}
			message.setPayload(payload);
			try {
				message.setActor(messageIncoming.getPublisher());
			} catch (MissingAttrException e) {
				logger.error("message: ", e);
			}
			send(message, null);
		}
	}

}
