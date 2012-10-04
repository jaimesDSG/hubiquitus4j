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

package org.hubiquitus.HelloHttpBot;

import org.hubiquitus.hapi.exceptions.MissingAttrException;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.hubiquitus.hubotsdk.Hubot;
import org.hubiquitus.hubotsdk.adapters.HHttpAdapter.HHttpData;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class HelloHttpHubot extends Hubot{

	final Logger logger = LoggerFactory.getLogger(HelloHttpHubot.class);
	public static void main(String[] args) throws Exception{
		HelloHttpHubot hubot = new HelloHttpHubot();
		hubot.start();
	}
	
	@Override
	public void inProcessMessage(HMessage incomingMessage) {
		System.out.println(incomingMessage);
		incomingMessage.setType("hHttpData");
		if (incomingMessage != null && incomingMessage.getType().equals("hHttpData")) {
			System.out.println("httpdata : " + incomingMessage.getPayloadAsJSONObject());
			HHttpData httpData = null;
			try {
				httpData = new HHttpData(incomingMessage.getPayloadAsJSONObject());
			} catch (JSONException e) {
				logger.debug(e.toString());
			}
			try {
				incomingMessage.setActor("httpOutbox@domain");
			} catch (MissingAttrException e) {
				logger.error("message: ", e);
			}// send to http outbox.
			send(incomingMessage, null);
			System.out.println(httpData);
		}
	}

}
