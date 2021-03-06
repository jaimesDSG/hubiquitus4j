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

package org.hubiquitus.hubotsdk.adapters.HHttpAdapter;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class HHttpAdapterRouteBuilder extends RouteBuilder {

	private String jettyCamelUri = "";
	private Processor processor = null;
	
	public HHttpAdapterRouteBuilder(String jettyCamelUri, Processor processor) {
		this.jettyCamelUri = jettyCamelUri;
		this.processor = processor;
	}
	
	@Override
	public void configure() throws Exception {
		from(this.jettyCamelUri).process(this.processor);
	}

}
