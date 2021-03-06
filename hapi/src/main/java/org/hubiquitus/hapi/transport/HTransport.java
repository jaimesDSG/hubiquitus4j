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

package org.hubiquitus.hapi.transport;

import org.json.JSONObject;

/**
 * @cond internal
 * @version 0.5
 * Interface abstracting transport layer
 */

public interface HTransport {

	/**
	 * transport layer connect
	 * should connect asynchronously and catch all errors and return them through callback
	 * @param callback
	 * @param options
	 */
	public void connect(HTransportDelegate callback, HTransportOptions options);
	
	/**
	 * transport layer disconnect
	 */
	public void disconnect();
	
	/**
	 * transport the object to send it to the server
	 * @param object
	 */
	public void sendObject(JSONObject object);
}

/**
 * @endcond
 */