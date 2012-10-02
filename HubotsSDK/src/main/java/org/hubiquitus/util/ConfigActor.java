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

package org.hubiquitus.util;

import java.util.Map;

import org.hubiquitus.hapi.hStructures.HCondition;
import org.json.JSONArray;

public class ConfigActor {

		public static class AdapterConfig {
		public String name,type;
		public Map<String,String> properties;

		public Map<String,String> getProperties() { return properties; }
		public String getName() { return name; }
		public String getType() { return type; }

		public void setName(String s) { name = s; }
		public void setType(String s) { type = s; }
		public void setProperties(Map<String,String> map) { properties = map;}

		@Override
		public String toString() {
			return "Adapter [name=" + name + ", type=" + type + ", properties="
					+ properties + "]";
		}
	}

	public String type;
	public String actor;
	public String pwd;
	public String hserver;
	public String endpoint;
	public HCondition filter;
	public JSONArray adapters;
	
	public String getType() { return type; }
	public String getActor() { return actor; }
	public String getPwd() { return pwd; }
	public String getHServer() {return hserver; }
	public String getEndpoint() { return endpoint; }
	public HCondition getFilter() { return filter; }
	public JSONArray getAdapters() { return adapters; }

	public void setType(String s) { type = s; }
	public void setActor(String s) { actor = s; }
	public void setPwd(String s) { pwd = s; }
	public void setHServer(String s) { hserver = s; }
	public void setEndpoint(String s) { endpoint = s; }
	public void setFilter(HCondition f) {filter = f; }
	public void setAdapters(JSONArray a) { adapters = a; }

	@Override
	public String toString() {
		String test = "ConfigActor [type=" + type + ", actor=" + actor
				+ ", pwd=" + pwd + ", hserver=" + hserver + ", endpoint="
				+ endpoint + ", filter=" + filter + ", adapters="
				+ adapters + "]";
		return test;
	}
}
