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

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version v 0.5
 */
public class HHttpData extends JSONObject {
	
	final Logger logger = LoggerFactory.getLogger(HHttpData.class);
	
	
	public HHttpData() {
		super();
	};
	
	public HHttpData(JSONObject jsonObj) throws JSONException{
		super(jsonObj, JSONObject.getNames(jsonObj));
	}

	/* getters and setters */
	/**
	 * @return Post attachments sent with the query.
	 */
	public JSONObject getAttachments() {
		JSONObject attachments = null;
		try {
			attachments = this.getJSONObject("attachments");
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
		return attachments;
	}

	public void setAttachments(JSONObject attachments) {
		if(attachments == null || attachments.length()<=0){
			try {
				this.remove("attachments");
			} catch (Exception e) {
				logger.debug(e.toString());
			}
			return;
		}
		try {
			this.put("attachments", attachments);
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
	}

	/**
	 * @return Body content as raw bytes encoded in Base64 for json.
	 */
	public byte[] getRawBody() {
		String encodedRawBody;
		byte[] rawBody = null;
		try {
			encodedRawBody = this.getString("rawBody");
			if (encodedRawBody != null) {
				rawBody = Base64.decodeBase64(encodedRawBody);
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
		return rawBody;
	}

	/**
	 * Set body content.
	 * @param rawBody
	 */
	public void setRawBody(byte[] rawBody) {
		try {
			if(rawBody == null){
				this.remove("rawBody");
			}else{
				this.put("rawBody", Base64.encodeBase64String(rawBody));
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
		
	}

	/**
	 * @return The method of the data. Possible values : get, post, put, delete
	 */
	public String getMethod() {
		String method = null;
		try {
			method = this.getString("method");
		} catch (Exception e) {
			logger.debug(e.toString());
		}
		return method;
	}

	/**
	 * Define the method of the data. Possible values : get, post, put, delete
	 * @param method
	 */
	public void setMethod(String method) {
		try {
			if(method == null){
				logger.error("message: method in hHttpData is mandatory.");
				return;
			}else{
				this.put("method", method);
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
	}

	/**
	 * @return Parameters applied to the URI. eg : “?a=2”
	 */
	public String getQueryArgs() {
		String queryArgs = null;
		try {
			queryArgs = this.getString("queryArgs");
		} catch (Exception e) {
			logger.debug(e.toString());
		}
		return queryArgs;
	}

	/**
	 * Set the parameters applied to the URI. eg : “?a=2”
	 * @param queryArgs
	 */
	public void setQueryArgs(String queryArgs) {
		try {
			if(queryArgs == null){
				this.remove("queryArgs");
			}else{
				this.put("queryArgs", queryArgs);
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
	}

	/**
	 * @return Path to the resource. eg : “/path” 
	 */
	public String getQueryPath() {
		String queryPath = null;
		try {
			queryPath = this.getString("queryPath");
		} catch (Exception e) {
			logger.debug(e.toString());
		}
		return queryPath;
	}

	/**
	 * Set path to the resource. eg : "/path"
	 * @param queryPath
	 */
	public void setQueryPath(String queryPath) {
		try {
			if(queryPath == null){
				this.remove("queryPath");
			}else{
				this.put("queryPath", queryPath);
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
	}

	/**
	 * @return Host name used to do the query. eg : “localhost”
	 */
	public String getServerName() {
		String serverName = null;
		try {
			serverName = this.getString("serverName");
		} catch (Exception e) {
			logger.debug(e.toString());
		}
		return serverName;
	}

	/**
	 * Set the host name used to do the query. eg : "localhost"
	 * @param serverName
	 */
	public void setServerName(String serverName) {
		try {
			if(serverName == null){
				logger.error("message: serverName in hHttpData is mandatory.");
				return;
			}else{
				this.put("serverName", serverName);
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
	}

	/**
	 * @return Port used to do the query. eg : 8080
	 */
	public Integer getServerPort() {
		Integer serverPort = null;
		try {
			serverPort = this.getInt("serverPort");
		} catch (Exception e) {
			logger.debug(e.toString());
		}
		return serverPort;
	}

	/**
	 * Set port used to do the query. eg : 8080
	 * @param serverPort
	 */
	public void setServerPort(Integer serverPort) {
		try {
			if(serverPort == null){
				logger.error("message: serverPort in hHttpData is mandatory.");
				return;
			}else{
				this.put("serverPort", serverPort);
			}
		} catch (JSONException e) {
			logger.debug(e.toString());
		}
	}
}
