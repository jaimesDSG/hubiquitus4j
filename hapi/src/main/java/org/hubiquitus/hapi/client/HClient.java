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

package org.hubiquitus.hapi.client;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.hubiquitus.hapi.exceptions.MissingAttrException;
import org.hubiquitus.hapi.hStructures.ConnectionError;
import org.hubiquitus.hapi.hStructures.ConnectionStatus;
import org.hubiquitus.hapi.hStructures.HAck;
import org.hubiquitus.hapi.hStructures.HAckValue;
import org.hubiquitus.hapi.hStructures.HAlert;
import org.hubiquitus.hapi.hStructures.HCommand;
import org.hubiquitus.hapi.hStructures.HConvState;
import org.hubiquitus.hapi.hStructures.HMeasure;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.hubiquitus.hapi.hStructures.HMessageOptions;
import org.hubiquitus.hapi.hStructures.HOptions;
import org.hubiquitus.hapi.hStructures.HResult;
import org.hubiquitus.hapi.hStructures.HStatus;
import org.hubiquitus.hapi.hStructures.ResultStatus;
import org.hubiquitus.hapi.structures.JabberID;
import org.hubiquitus.hapi.transport.HTransport;
import org.hubiquitus.hapi.transport.HTransportDelegate;
import org.hubiquitus.hapi.transport.HTransportOptions;
import org.hubiquitus.hapi.transport.socketio.HTransportSocketio;
import org.hubiquitus.hapi.util.HUtil;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 0.5 Hubiquitus client, public API
 */

public class HClient {
	final Logger logger = LoggerFactory.getLogger(HClient.class);

	/**
	 * only connecting , connected , disconnecting , disconnected
	 */
	private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

	@SuppressWarnings("unused")
	private HOptions options = null;
	private HTransportOptions transportOptions = null;
	private HTransport transport;

	private HStatusDelegate statusDelegate = null;
	private HMessageDelegate messageDelegate = null;
	// private HCommandDelegate commandDelegate = null;

	private Hashtable<String, HMessageDelegate> messagesDelegates = new Hashtable<String, HMessageDelegate>();
	private Hashtable<String, Timer> timeoutHashtable = new Hashtable<String, Timer>();

	private TransportDelegate transportDelegate = new TransportDelegate();

	private Timer timeoutTimer = null;

	public HClient() {
		transportOptions = new HTransportOptions();
	}

	/**
	 * Establishes a connection to hNode to allow the reception and sending of messages and commands.
	 * @param publisher : user jid (ie : my_user@domain/resource). Mandatory.
	 * @param password : Mandatory.
	 * @param options : Complementary values used for the connection to the server. Not mandatory.
	 */
	public void connect(String publisher, String password, HOptions options) {
		boolean shouldConnect = false;
		boolean connInProgress = false;
		boolean disconInProgress = false;
		this.options = new HOptions(options);

		// synchronize connection status updates to make sure, we have one
		// connect at a time
		synchronized (this) {
			if (this.connectionStatus == ConnectionStatus.DISCONNECTED) {
				shouldConnect = true;

				// update connection status
				connectionStatus = ConnectionStatus.CONNECTING;
			} else if (this.connectionStatus == ConnectionStatus.CONNECTING) {
				connInProgress = true;
			} else if (this.connectionStatus == ConnectionStatus.DISCONNECTING) {
				disconInProgress = true;
			}
		}

		if (shouldConnect) { // if not connected, then connect

			// notify connection
			this.notifyStatus(ConnectionStatus.CONNECTING, ConnectionError.NO_ERROR, null);

			// fill HTransportOptions
			try {
				this.fillHTransportOptions(publisher, password, options);
			} catch (Exception e) {
				// stop connecting if filling error
				this.notifyStatus(ConnectionStatus.DISCONNECTED, ConnectionError.JID_MALFORMAT, e.getMessage());
				return;
			}

			// choose transport layer
			if (options.getTransport().equals("socketio")) {
				/*
				 * if (this.transport != null) { //check if other transport mode
				 * connect this.transport.disconnect(); }
				 */
				if (this.transport == null || (this.transport.getClass() != HTransportSocketio.class)) {
					this.transport = new HTransportSocketio();
				}
				this.transport.connect(transportDelegate, this.transportOptions);
			} else {
				// for the future transports.
			}
		} else {
			if (connInProgress) {
				notifyStatus(ConnectionStatus.CONNECTING, ConnectionError.CONN_PROGRESS, null);
			} else if (disconInProgress) {
				// updateStatus(ConnectionStatus.DISCONNECTING,
				// ConnectionError.ALREADY_CONNECTED, null);
			} else {
				notifyStatus(ConnectionStatus.CONNECTED, ConnectionError.ALREADY_CONNECTED, null);
			}
		}
	}

	/**
	 * Disconnect the user from the current working session.
	 */
	public void disconnect() {
		boolean shouldDisconnect = false;
		boolean connectInProgress = false;
		synchronized (this) {
			if (this.connectionStatus == ConnectionStatus.CONNECTED) {
				shouldDisconnect = true;
				// update connection status
				connectionStatus = ConnectionStatus.DISCONNECTING;
			} else if (this.connectionStatus == ConnectionStatus.CONNECTING) {
				connectInProgress = true;
			}
		}

		if (shouldDisconnect) {
			notifyStatus(ConnectionStatus.DISCONNECTING, ConnectionError.NO_ERROR, null);
			transport.disconnect();
		} else if (connectInProgress) {
			notifyStatus(ConnectionStatus.CONNECTING, ConnectionError.CONN_PROGRESS, "Can't disconnect while a connection is in progress");
		} else {
			notifyStatus(ConnectionStatus.DISCONNECTED, ConnectionError.NOT_CONNECTED, null);
		}

	}

	/**
	 * Status delegate receive all connection status events.
	 * @param statusDelgate
	 */
	public void onStatus(HStatusDelegate statusDelgate) {
		this.statusDelegate = statusDelgate;
	}

	/**
	 * Message delegate receive all incoming HMessage
	 * @param messageDelegate
	 */
	public void onMessage(HMessageDelegate messageDelegate) {
		this.messageDelegate = messageDelegate;
	}

	/**
	 * Get current connection status
	 */
	public ConnectionStatus status() {
		return this.connectionStatus;
	}

	/**
	 * The hAPI sends the hMessage to the hserver which transfer it to the specified actor.
	 * The hserver will perform one of the following actions :
	 *  (1). If the actor is a channel (ie : #channelName@domain) the hserver will perform a publish operation of the provided hMessage to the channel and send an hMessage with hResult payload containing the published message and cmd name set with hsend to acknowledge publishing only if a timeout value has been provided.
	 *  (2). If the actor is either ‘session’ and payload type is ‘hCommand’ the server will handle it. In other cases, it will send an hMessage with a hResult error NOT_AUTHORIZED. Only if the timeout is provided.
	 *  (3). If the actor is a jid, hserver will relay the message to the relevant actor.
	 * @param message : The message to send. Mandatory.
	 * @param messageDelegate : If provided, called by the hAPI when the first message refering to current message arrive . Not mandatory.
	 */
	public void send(final HMessage message, final HMessageDelegate messageDelegate) {
		if (this.connectionStatus != ConnectionStatus.CONNECTED) {
			notifyResultError(message.getMsgid(), ResultStatus.NOT_CONNECTED, "Not conncected.");
			return;
		}
		if (message == null) {
			notifyResultError(null, ResultStatus.MISSING_ATTR, "Provided message is null", messageDelegate);
			return;
		}
		if (message.getActor() == null) {
			notifyResultError(message.getMsgid(), ResultStatus.MISSING_ATTR, "Actor not found in message");
			return;
		}

		message.setSent(new DateTime());
		message.setPublisher(transportOptions.getJid().getBareJID());
	
		
		if (message.getTimeout() > 0) {
			// hAPI will do correlation. If no answer within the
			// timeout, a timeout error will be sent.
			if (messageDelegate != null) {
				message.setMsgid(UUID.randomUUID().toString());
				messagesDelegates.put(message.getMsgid(), messageDelegate);
				timeoutTimer = new Timer();
				timeoutTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						notifyResultError(message.getMsgid(), ResultStatus.EXEC_TIMEOUT, "The response of message is time out!");
						messagesDelegates.remove(message.getMsgid());
					}
				}, message.getTimeout());
				timeoutHashtable.put(message.getMsgid(), timeoutTimer);
			}else{
				//when there is no callback, timeout has no sense. delete timeout.
				message.setTimeout(null);
			}
		}
		try {
			transport.sendObject(message);
		} catch (Exception e) {
			logger.error("message: ", e);
		}
		
	}

	/**
	 * Demands the server a subscription to the channel id. The hAPI performs a hMessage with a hCommand of type hsubscribe. 
	 * The server will check if not already subscribed and if authorized and subscribe him. 
	 * Nominal response : a hMessage with an hResult payload with status 0.
	 * @param actor : The channel id to subscribe to. (ie : #test@domain”). Mandatory.
	 * @param messageDelegate : A delegate notified when the result is sent by server. Mandatory.
	 */
	public void subscribe(String actor, HMessageDelegate messageDelegate) throws MissingAttrException {
		if(messageDelegate == null){
			throw new MissingAttrException("messageDelegate");
		}
		HMessage cmdMessage = null;
		try {
			cmdMessage = buildCommand(actor, "hsubscribe", null, null);
		} catch (MissingAttrException e) {
			logger.error("message: ", e);
		}
		cmdMessage.setTimeout(options.getTimeout());
		send(cmdMessage, messageDelegate);
	}

	/**
	 * Demands the server an unsubscription to the channel id. 
	 * The hAPI checks the current publisher’s subscriptions and if he is subscribed performs a hCommand of type hunsubscribe.
	 * Nominal response : an hMessage with an hResult where the status 0.
	 * @param actor : The channel to unsubscribe from. Mandatory.
	 * @param messageDelegate : A delegate notified when the result is sent by server. Mandatory.
	 * @throws MissingAttrException 
	 */
	public void unsubscribe(String actor, HMessageDelegate messageDelegate) throws MissingAttrException {
		if(messageDelegate == null){
			throw new MissingAttrException("messageDelegate");
		}
		HMessage cmdMessage = null;
		try {
			cmdMessage = buildCommand(actor, "hunsubscribe", null, null);
		} catch (MissingAttrException e) {
			logger.error("message: ", e);
		}
		cmdMessage.setTimeout(options.getTimeout());
		send(cmdMessage, messageDelegate);
	}

	/**
	 * Demands the hserver a list of the last messages saved for a dedicated channel. 
	 * The publisher must be in the channel’s participants list.
	 * Nominal response: a hMessage with an hResult having an array of hMessages.
	 * @warning HResult result type will be a JSonArray if successful
	 * @param actor : The channel id of the messages. Mandatory.
	 * @param nbLastMsg : The maximum number of messages to retrieve.If this value is not provided, the default value found in the channel header will be used and as callback a default value of 10. Not mandatory.
	 * @param messageDelegate : A delegate notified when the result is sent by server. Mandatory.
	 * @throws MissingAttrException 
	 */
	public void getLastMessages(String actor, int nbLastMsg, HMessageDelegate messageDelegate) throws MissingAttrException {
		if(messageDelegate == null){
			throw new MissingAttrException("messageDelegate");
		}
		JSONObject params = new JSONObject();
		try {
			if (nbLastMsg > 0) {
				params.put("nbLastMsg", nbLastMsg);
			}
		} catch (JSONException e) {
			logger.error("message: ", e);
		}
		HMessage cmdMessage = null;
		try {
			cmdMessage = buildCommand(actor, "hgetlastmessages", params, null);
		} catch (MissingAttrException e) {
			logger.error("message: ", e);
		}
		cmdMessage.setTimeout(options.getTimeout());
		send(cmdMessage, messageDelegate);
	}

	/**
	 * @see getLastMessages(String actor, int nbLastMsg)
	 * @param actor : The channel id of the messages. Mandatory.
	 * @param messageDelegate : A delegate notified when the result is sent by server. Mandatory.
	 * @throws MissingAttrException 
	 */
	public void getLastMessages(String actor, HMessageDelegate messageDelegate) throws MissingAttrException {
		this.getLastMessages(actor, -1, messageDelegate);
	}

	/**
	 * Demands the server a list of the publisher’s subscriptions.
	 * Nominal response : a hMessage with a hResult payload contains an array of channel id which are all active.
	 * @param messageDelegate : A delegate notified when the result is sent by server. Mandatory.
	 * @throws MissingAttrException 
	 */
	public void getSubscriptions(HMessageDelegate messageDelegate) throws MissingAttrException {
		if(messageDelegate == null){
			throw new MissingAttrException("messageDelegate");
		}
		HMessage cmdMessage = null;
		try {
			cmdMessage = buildCommand(transportOptions.getHserverService(), "hgetsubscriptions", null, null);
		} catch (MissingAttrException e) {
			logger.error("message: ", e);
		}
		cmdMessage.setTimeout(options.getTimeout());
		this.send(cmdMessage, messageDelegate);
	}

	/**
	 * Demands to the hserver the list of messages correlated by the convid value on a dedicated channel actor. 
	 * Nominal response : hMessage with hResult where the status is 0 and result is an array of convid.
	 * @param actor : The channel id where the conversations are searched. Mandatory
	 * @param convid : Conversation id. Mandatory
	 * @param messageDelegate : A delegate notified when the result is sent by server. Mandatory.
	 * @throws MissingAttrException 
	 */
	public void getThread(String actor, String convid, HMessageDelegate messageDelegate) throws MissingAttrException {
		if(messageDelegate == null){
			throw new MissingAttrException("messageDelegate");
		}
		JSONObject params = new JSONObject();
		String cmdName = "hgetthread";

		// check mandatory fields
		if (actor == null || actor.length() <= 0) {
			notifyResultError(null, ResultStatus.MISSING_ATTR, "Actor is missing", messageDelegate);
			return;
		}

		if (convid == null || convid.length() <= 0) {
			notifyResultError(null, ResultStatus.MISSING_ATTR, "Convid is missing", messageDelegate);
			return;
		}

		try {
			params.put("convid", convid);
		} catch (JSONException e) {
			logger.error("message: ", e);
		}

		HMessage cmdMessage = null;
		try {
			cmdMessage = this.buildCommand(actor, cmdName, params, null);
		} catch (MissingAttrException e) {
			logger.error("message: ", e);
		}
		cmdMessage.setTimeout(options.getTimeout());
		this.send(cmdMessage, messageDelegate);
	}

	/**
	 * Demands to the hserver the list of convid where there is a hConvState with the status value searched on the channel actor.
	 * Nominal response : hResult where the status is 0 with an array of convid.
	 * @param actor : The channel id where the conversations are searched. Mandatory
	 * @param status : The status searched. Mandatory
	 * @param messageDelegate : A delegate notified when the command result is issued. Mandatory.
	 * @throws MissingAttrException 
	 */
	public void getThreads(String actor, String status, HMessageDelegate messageDelegate) throws MissingAttrException {
		if(messageDelegate == null){
			throw new MissingAttrException("messageDelegate");
		}
		JSONObject params = new JSONObject();
		// check mandatory fields
		if (actor == null || actor.length() <= 0) {
			notifyResultError(null, ResultStatus.MISSING_ATTR, "Actor is missing", messageDelegate);
			return;
		}

		if (status == null || status.length() <= 0) {
			notifyResultError(null, ResultStatus.MISSING_ATTR, "Status is missing", messageDelegate);
			return;
		}

		try {
			params.put("status", status);
		} catch (JSONException e) {
			logger.error("message: ", e);
		}
		HMessage cmdMessage = null;
		try {
			cmdMessage = buildCommand(actor, "hgetthreads", params, null);
		} catch (MissingAttrException e) {
			logger.error("message: ", e);
		}
		cmdMessage.setTimeout(options.getTimeout());
		this.send(cmdMessage, messageDelegate);
	}

	/**
	 * Demands to the hserver the list of the available relevant message for a dedicated channel.
	 * Nominal response : hResult where the status is 0 and a array of HMessage.
	 * @param actor : The channel where the relevant messages are searched. Mandatory.
	 * @param messageDelegate : a delegate notified when the command result is issued. Mandatory.
	 * @throws MissingAttrException 
	 */
	public void getRelevantMessages(String actor, HMessageDelegate messageDelegate) throws MissingAttrException {
		if(messageDelegate == null){
			throw new MissingAttrException("messageDelegate");
		}
		// check mandatory fields
		if (actor == null || actor.length() <= 0) {
			notifyResultError(null, ResultStatus.MISSING_ATTR, "actor is missing", messageDelegate);
			return;
		}

		HMessage cmdMessage = null;
		try {
			cmdMessage = buildCommand(actor, "hRelevantMessages", null, null);
		} catch (MissingAttrException e) {
			logger.error("message: ", e);
		}
		cmdMessage.setTimeout(options.getTimeout());
		this.send(cmdMessage, messageDelegate);
	}

	/* Builder */

	/**
	 * Helper to create a hMessage. Payload type could be instance of JSONObject(HAlert, HAck, HCommand ...), JSONObject, JSONArray, String, Boolean, Number
	 * @param actor : The Actor for the hMessage. Mandatory.
	 * @param type : The type of the hMessage. Not mandatory.
	 * @param payload : The payload for the hMessage. Not mandatory.
	 * @param options : The options if any to use for the creation of the hMessage. Not mandatory.
	 * @throws MissingAttrException
	 */
	public HMessage buildMessage(String actor, String type, Object payload, HMessageOptions options) throws MissingAttrException {

		// check for required attributes
		if (actor == null || actor.length() <= 0) {
			throw new MissingAttrException("actor");
		}

		// build the message
		HMessage hmessage = new HMessage();

		hmessage.setActor(actor);
		hmessage.setType(type);
		if (options != null) {
			hmessage.setRef(options.getRef());
			hmessage.setConvid(options.getConvid());
			hmessage.setPriority(options.getPriority());
			hmessage.setRelevance(options.getRelevance());
			hmessage.setPersistent(options.getPersistent());
			hmessage.setLocation(options.getLocation());
			hmessage.setAuthor(options.getAuthor());
			hmessage.setHeaders(options.getHeaders());
			hmessage.setTimeout(options.getTimeout());
		}
		if (transportOptions != null && transportOptions.getJid() != null) {
			hmessage.setPublisher(transportOptions.getJid().getBareJID());
		} else {
			hmessage.setPublisher(null);
		}
		hmessage.setPayload(payload);
		return hmessage;
	}

	/**
	 * Helper to create a hMessage with a hConvState payload.
	 * @param actor : The channel id for the hMessage. Mandatory
	 * @param convid : The convid where the status have to be updated. Mandatory
	 * @param status : Status of the conversation. Mandatory.
	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
	 * @return A hMessage with a hConvState payload.
	 * @throws MissingAttrException
	 */
	public HMessage buildConvState(String actor, String convid, String status, HMessageOptions options) throws MissingAttrException {

		// check for required attributes
		if (actor == null || actor.length() <= 0) {
			throw new MissingAttrException("actor");
		}

		if (convid == null || convid.length() <= 0) {
			throw new MissingAttrException("convid");
		}

		if (status == null || status.length() <= 0) {
			throw new MissingAttrException("status");
		}

		HConvState hconvstate = new HConvState();
		hconvstate.setStatus(status);

		HMessage hmessage = buildMessage(actor, "hConvState", hconvstate, options);
		hmessage.setConvid(convid);

		return hmessage;
	}

	/**
	 * Helper to create a hMessage wiht a hAck payload.
	 * @param actor : The actor for the hMessage.  Mandatory.
	 * @param ref : The msgid to acknowledged. Mandatory.
	 * @param ack : The following values are authorized : 
	 * (1). “recv” : means that the message has been received by the participant (on at least one of its devices). 
	 * (2). “read” : means that the message has been read by the participant.
	 * Mandatory.
	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
	 * @return A hMessage with a hAck payload.
	 * @throws MissingAttrException
	 */
	public HMessage buildAck(String actor, String ref, HAckValue ack, HMessageOptions options) throws MissingAttrException {
		// check for required attributes
		if (actor == null || actor.length() <= 0) {
			throw new MissingAttrException("actor");
		}

		if (ref == null || ref.length() <= 0) {
			throw new MissingAttrException("ref");
		}
		// check for required attributes
		if (ack == null) {
			throw new MissingAttrException("ack");
		}

		options.setRef(ref);

		HAck hack = new HAck();
		hack.setAck(ack);
		HMessage hmessage = buildMessage(actor, "hAck", hack, options);
		return hmessage;
	}

	/**
	 * Helper to create a hMessage with a hAlert payload.
	 * @param actor : The channel id for the hMessage. Mandatory.
	 * @param alert : The alert message. Mandatory.
	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
	 * @return A hMessage with a hAlert payload.
	 * @throws MissingAttrException
	 */
	public HMessage buildAlert(String actor, String alert, HMessageOptions options) throws MissingAttrException {
		// check for required attributes
		if (actor == null || actor.length() <= 0) {
			throw new MissingAttrException("actor");
		}

		// check for required attributes
		if (alert == null || alert.length() <= 0) {
			throw new MissingAttrException("actor");
		}

		HAlert halert = new HAlert();
		halert.setAlert(alert);
		HMessage hmessage = buildMessage(actor, "hAlert", halert, options);
		return hmessage;
	}

	/**
	 * Helper to create a hMessage with a hMeasure payload.
	 * @param actor : The actor for the hMessage. Mandatory
	 * @param value : The value of the measure. Mandatory
	 * @param unit : The unit of the measure. Mandatory
	 * @param options : The options to use if any for the creation of the hMessage. Not Mandatory.
	 * @return A hMessage with a hMeasure payload. 
	 * @throws MissingAttrException
	 */
	public HMessage buildMeasure(String actor, String value, String unit, HMessageOptions options) throws MissingAttrException {
		// check for required attributes
		if (actor == null || actor.length() <= 0) {
			throw new MissingAttrException("actor");
		}

		// check for required attributes
		if (value == null || value.length() <= 0) {
			throw new MissingAttrException("value");
		}

		// check for required attributes
		if (unit == null || unit.length() <= 0) {
			throw new MissingAttrException("unit");
		}

		HMeasure hmeasure = new HMeasure();
		hmeasure.setValue(value);
		hmeasure.setUnit(unit);
		HMessage hmessage = buildMessage(actor, "hMeasure", hmeasure, options);

		return hmessage;
	}

	/**
	 * Helper to create a hMessage with a hCommand payload.
	 * @param actor : The actor for the hMessage. Mandatory.
	 * @param cmd : The name of the command. Mandatory.
	 * @param params : Parameters of the command. Not mandatory.
	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
	 * @return A hMessage with a hCommand payload.
	 * @throws MissingAttrException
	 */
	public HMessage buildCommand(String actor, String cmd, JSONObject params, HMessageOptions options) throws MissingAttrException {
		// check for required attributes
		if (actor == null || actor.length() <= 0) {
			throw new MissingAttrException("actor");
		}

		// check for required attributes
		if (cmd == null || cmd.length() <= 0) {
			throw new MissingAttrException("cmd");
		}

		HCommand hcommand = new HCommand(cmd, params);
		HMessage hmessage = buildMessage(actor, "hCommand", hcommand, options);
		return hmessage;

	}

	/**
	 * Helper to create a hMessage with a hResult payload.
	 * @param actor : The actor for the hMessage. Mandatory.
	 * @param ref : The id of the message received, for correlation purpose. Mandatory.
	 * @param status : Result status code. Mandatory.
	 * @param result : The result of a command. Possible types: JSONObject, JSONArray, String, Boolean, Number. Not mandatory.
	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
	 * @return A hMessage with a hResult payload.
	 * @throws MissingAttrException
	 */
	public HMessage buildResult(String actor, String ref, ResultStatus status, Object result, HMessageOptions options) throws MissingAttrException {
		// check for required attributes
		if (actor == null || actor.length() <= 0) {
			throw new MissingAttrException("actor");
		}
		// check for required attributes
		if (ref == null || ref.length() <= 0) {
			throw new MissingAttrException("ref");
		}

		// check for required attributes
		if (status == null) {
			throw new MissingAttrException("status");
		}

		HResult hresult = new HResult();
		hresult.setResult(result);
		hresult.setStatus(status);
		options.setRef(ref);
		HMessage hmessage = buildMessage(actor, "hResult", hresult, options);
		return hmessage;
	}

	/* HTransportCallback functions */

	/**
	 * @internal fill htransport, randomly pick an endpoint from availables endpoints. By default it uses options server host to fill serverhost field and as fallback jid domain
	 * @param publisher : publisher as jid format (my_user@serverhost.com/my_resource)
	 * @param password
	 * @param options
	 * @throws Exception : in case jid is malformatted, it throws an exception
	 */
	private void fillHTransportOptions(String publisher, String password, HOptions options) throws Exception {
		JabberID jid = new JabberID(publisher);

		this.transportOptions.setJid(jid);
		this.transportOptions.setPassword(password);

		// by default we user server host rather than publish host if defined

		// for endpoints, pick one randomly and fill htransport options
		if (options.getEndpoints().length() > 0) {
			int endpointIndex = HUtil.pickIndex(options.getEndpoints());
			try {
				String endpoint = options.getEndpoints().getString(endpointIndex);
				this.transportOptions.setEndpointHost(HUtil.getHost(endpoint));
				this.transportOptions.setEndpointPort(HUtil.getPort(endpoint));
				this.transportOptions.setEndpointPath(HUtil.getPath(endpoint));
			} catch (JSONException e) {
				logger.error("message: ", e);
			}
			
		} else {
			this.transportOptions.setEndpointHost(null);
			this.transportOptions.setEndpointPort(0);
			this.transportOptions.setEndpointPath(null);
		}
	}

	/**
	 * @internal change current status and notify delegate through callback
	 * @param status : connection status
	 * @param error : error code
	 * @param errorMsg : a low level description of the error
	 */
	private void notifyStatus(ConnectionStatus status, ConnectionError error, String errorMsg) {
		try {
			connectionStatus = status;
			if (this.statusDelegate != null) {
				// create structure
				final HStatus hstatus = new HStatus();
				hstatus.setStatus(status);
				hstatus.setErrorCode(error);
				hstatus.setErrorMsg(errorMsg);

				// return status asynchronously
				(new Thread(new Runnable() {
					public void run() {
						try {
							statusDelegate.onStatus(hstatus);
						} catch (Exception e) {
							logger.error("message: ", e);
						}
					}
				})).start();
			}
		} catch (Exception e) {
			logger.error("message", e);
		}
	}

	/**
	 *@internal  Notify message delegate of an incoming hmessage. If the callback is not set, it will call onMessage. 
	 *If the callback is set in the service functions, it will call the callback function instead of onMessage
	 * @param message
	 */
	private void notifyMessage(final HMessage message) {
		if (!this.messagesDelegates.isEmpty() && message.getRef() != null && this.messagesDelegates.containsKey(HUtil.getApiRef(message.getRef()))) {
			if (this.timeoutHashtable.containsKey(HUtil.getApiRef(message.getRef()))) {
				Timer timeout = timeoutHashtable.get(HUtil.getApiRef(message.getRef()));
				if (timeout != null) {
					timeout.cancel();
					timeout = null;
				}
			}
			notifyMessage(message, this.messagesDelegates.get(HUtil.getApiRef(message.getRef())));
		} else if (!("hresult").equalsIgnoreCase(message.getType())) {
			try {
				if (this.messageDelegate != null) {
					// return message asynchronously
					(new Thread(new Runnable() {
						public void run() {
							try {
								messageDelegate.onMessage(message);
							} catch (Exception e) {
								logger.error("message: ", e);
							}
						}
					})).start();
				}
			} catch (Exception e) {
				logger.error("message: ", e);
			}
		}
	}

	/**
	 * @internal  Notify message delegate of an incoming hmessage. Run the message delegate.
	 * @param message
	 * @param messageDelegate
	 */
	private void notifyMessage(final HMessage message, final HMessageDelegate messageDelegate) {
		try {
			if (messageDelegate != null) {
				// return result asynchronously
				(new Thread(new Runnable() {
					public void run() {
						try {
							messageDelegate.onMessage(message);
						} catch (Exception e) {
							logger.error("message: ", e);
						}
					}
				})).start();
			} else {
				// results are dropped
			}
		} catch (Exception e) {
			logger.error("message: ", e);
		}
	}

	/**
	 * @internal Helper function to return a hmessage with hresult error
	 * @param ref
	 * @param resultstatus
	 * @param errorMsg
	 */
	private void notifyResultError(String ref, ResultStatus resultstatus, String errorMsg) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("errorMsg", errorMsg);
		} catch (JSONException e) {
			logger.error("message: ", e);
		}
		HResult hresult = new HResult();
		hresult.setResult(obj);
		hresult.setStatus(resultstatus);
		HMessage message = new HMessage();
		message.setRef(ref);
		message.setType("hResult");
		message.setPayload(hresult);

		this.notifyMessage(message);
	}

	/**
	 *@internal  Helper function to return a hmessage with hresult error
	 * @param ref
	 * @param resultstatus
	 * @param errorMsg
	 * @param messageDelegate
	 */
	private void notifyResultError(String ref, ResultStatus resultstatus, String errorMsg, HMessageDelegate messageDelegate) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("errorMsg", errorMsg);
		} catch (JSONException e) {
			logger.error("message: ", e);
		}
		HResult hresult = new HResult();
		hresult.setResult(obj);
		hresult.setStatus(resultstatus);
		HMessage message = new HMessage();
		message.setRef(ref);
		message.setType("hResult");
		message.setPayload(hresult);

		this.notifyMessage(message, messageDelegate);
	}

	/**
	 * @internal Class used to get callbacks from transport layer.
	 */
	private class TransportDelegate implements HTransportDelegate {

		/**
		 * @internal see HTransportDelegate for more informations
		 */
		public void onStatus(ConnectionStatus status, ConnectionError error, String errorMsg) {
			notifyStatus(status, error, errorMsg);
		}

		/**
		 * @internal see HTransportDelegate for more information
		 */
		public void onData(String type, JSONObject jsonData) {
			try {
				if (type.equalsIgnoreCase("hmessage")) {
					HMessage message = new HMessage(jsonData);
					notifyMessage(message);
				}

			} catch (Exception e) {
				logger.error("message: ", e);
			}
		}
	}

}