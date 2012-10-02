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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.hubiquitus.hubotsdk.AdapterInbox;
import org.hubiquitus.hubotsdk.adapters.HHttpAdapter.HHttpAdapterRouteBuilder;
import org.hubiquitus.hubotsdk.adapters.HHttpAdapter.HHttpAttachement;
import org.hubiquitus.hubotsdk.adapters.HHttpAdapter.HHttpData;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HHttpAdapterInbox extends AdapterInbox implements Processor{

	final Logger logger = LoggerFactory.getLogger(HHttpAdapterInbox.class);
	
	private String host = "0.0.0.0";
	private int port = 80;
	private String path = "";
	private String jettyCamelUri = "";
	
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
		String jettyOptions = "matchOnUriPrefix=true";
		jettyCamelUri = "jetty:http://" + this.host + ":" + this.port + this.path;
		jettyCamelUri += "?" + jettyOptions;
		
		System.out.println("Starting HttpAdapter with jettyCamelUri : " + jettyCamelUri);
		
		//add route 
		HHttpAdapterRouteBuilder routes = new HHttpAdapterRouteBuilder(jettyCamelUri, this);
		try {
			camelContext.addRoutes(routes);
		} catch (Exception e) {
			logger.warn("message: ", e);
		}
	}

	@Override
	public void stop() {
				
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		
		HttpServletRequest request = exchange.getIn().getBody(HttpServletRequest.class);
		
		//Gather data to send through an hmessage
		byte[] rawBody = (byte[]) in.getBody(byte[].class);
		Map<String, Object> headers = in.getHeaders();
		Map<String, DataHandler> attachments = in.getAttachments();
		
		String method = request.getMethod().toLowerCase();
		String queryArgs = request.getQueryString();
		String queryPath = request.getRequestURI();
		String serverName = request.getServerName();
		Integer serverPort = request.getServerPort();
		
		//create message to send
		HMessage message = new HMessage();
		message.setAuthor(this.actor);
		if (headers != null) {
			JSONObject jsonHeaders = new JSONObject(); 
			for (String key : headers.keySet()) {
				Object header = headers.get(key);
				String value = null;
				if (header != null) {
					value = header.toString();
				}
				jsonHeaders.put(key, value);
			}
		}
		
		message.setPublished(new DateTime());
		
		message.setType("hHttpData");
		//create payload
		HHttpData httpData = new HHttpData();
		httpData.setMethod(method);
		httpData.setQueryArgs(queryArgs);
		httpData.setQueryPath(queryPath);
		httpData.setServerName(serverName);
		httpData.setServerPort(serverPort);
		httpData.setRawBody(rawBody);
		
		//create attachements
		JSONObject hattachements = new JSONObject();
		for (String key : attachments.keySet()) {
			DataHandler attachement = attachments.get(key);
			if(attachement != null) {
				HHttpAttachement hattachement = new HHttpAttachement();
				hattachement.setContentType(attachement.getContentType());
				hattachement.setName(attachement.getName());
				
				//read attachement
				byte[] buffer = new byte[8 * 1024];
				InputStream input = attachement.getInputStream();
				
				ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
				int bytesRead;
				while ((bytesRead = input.read(buffer)) != -1) {
					byteOutputStream.write(buffer, 0, bytesRead);
				}
				hattachement.setData(byteOutputStream.toByteArray());
				try {
					hattachements.put(key, hattachement);
				} catch (JSONException e) {
					logger.debug(e.toString());
				}
			} else {
				//if attachment is null, do not put the key in hattachments.
			}
		}
		
		httpData.setAttachments(hattachements);
		message.setPayload(httpData);
		
		//finally send message to actor
		this.put(message);
	}

}
