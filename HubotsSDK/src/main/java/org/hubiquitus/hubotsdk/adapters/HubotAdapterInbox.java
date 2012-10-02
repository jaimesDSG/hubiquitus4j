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

package org.hubiquitus.hubotsdk.adapters;

import org.hubiquitus.hapi.client.HMessageDelegate;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.hubiquitus.hubotsdk.AdapterInbox;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubotAdapterInbox extends AdapterInbox implements HMessageDelegate {
	
	final Logger logger = LoggerFactory.getLogger(HubotAdapterInbox.class);
	private String actor;

	public HubotAdapterInbox() {
		super();
	}
	
	@Override
	public void setProperties(JSONObject properties) {
		if(properties != null){
			try {
				if(properties.has("actor")){
					this.actor = properties.getString("actor");
				}
			} catch (Exception e) {
				logger.debug("message: ",e);
			}
		}
	}

	@Override
	public void start() {
		hclient.onMessage(this);
	}

	@Override
	public void stop() {
	}
	
	public void onMessage(HMessage message) {
		if(!message.getPublisher().equals(actor))
			put(message);		
	}
}
