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
import org.hubiquitus.hubotsdk.AdapterOutbox;
import org.hubiquitus.hubotsdk.HubotMessageStructure;
import org.hubiquitus.hubotsdk.ProducerTemplateSingleton;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubotAdapterOutbox extends AdapterOutbox {

	final Logger logger = LoggerFactory.getLogger(HubotAdapterOutbox.class);

	public HubotAdapterOutbox() {
		super();
	}

	@Override
	public void setProperties(JSONObject properties) {
		// nop
		
	}

	@Override
	public void start() {
		// nop
	}

	@Override
	public void stop() {
		// nop
		
	}

	@Override
	public void sendMessage(HMessage message, final HMessageDelegate callback) {

            if (callback != null) {
                    hclient.send(message, new HMessageDelegate() {
                    @Override
                    public void onMessage(HMessage message) {
                        ProducerTemplateSingleton.getProducerTemplate().sendBody("seda:inbox",new HubotMessageStructure(message, callback));
                    }
                });
            } else {
                hclient.send(message, null);
            }
	}

}
