package main;

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


import org.hubiquitus.hapi.client.HCallback;
import org.hubiquitus.hapi.hStructures.HStatus;
import org.hubiquitus.hapi.hStructures.ConnectionError;
import org.hubiquitus.hapi.structures.HJSONSerializable;

/**
 * 
 * @author j.desousag
 * @version 0.3
 * Callback of the example
 */
public class CallbackExample implements HCallback {
	
	private MainPanel panel;

	public CallbackExample(MainPanel panel2) {
		this.panel = panel2;
	}
	
	@Override
	public void hCallback(String type, HJSONSerializable data) {
		// TODO Auto-generated method stub
		panel.setStatusArea(type);
		if(type.equals("hStatus")) {
			HStatus status = (HStatus) data;
			panel.addTextArea(status.toString());
			if(status.getErrorCode() != ConnectionError.NO_ERROR)
				panel.setStatusArea(status.getStatus().toString());
			else
				panel.setStatusArea(status.getStatus().toString() + " : " + status.getErrorMsg() );
		}
	}

}
