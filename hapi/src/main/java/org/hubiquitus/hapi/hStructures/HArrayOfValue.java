package org.hubiquitus.hapi.hStructures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HArrayOfValue extends JSONObject {
	final Logger logger = LoggerFactory.getLogger(HArrayOfValue.class);
	
	private String name;
	
	public HArrayOfValue() {
		super();
	}
	
	public HArrayOfValue(String name, JSONArray values){
		super();
		this.name = name;
	}
	
	public HArrayOfValue(String jsonString) throws JSONException{
		super(jsonString);
	}
	
	public HArrayOfValue(JSONObject jsonObj) throws JSONException{
		super(jsonObj, JSONObject.getNames(jsonObj));
	}
	/**
	 * @return The name of the attribute to compare with.
	 */
	public String getName() {
		return name;
	}
	/**
	 * Set the name of the attribute to compare with.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return The Array of the values of the attribute to compare with.
	 */
	public JSONArray getValues() {
		if(this.name == null || this.name.length() <= 0){
			logger.error("message: The name of the attribute in HArrayOfValue is null or empty");
			return null;
		}
		JSONArray values;
		try {
			values = this.getJSONArray(this.name);
		} catch (Exception e) {
			values = null;
		}
		return values;
	}
	/**
	 * Set the values of the attribute to compare with; 
	 * @param values
	 */
	public void setValues(JSONArray values) {
		if(this.name == null || this.name.length() <= 0){
			logger.error("message: The name of the attribute in HArrayOfValue is null or empty");
			return;
		}
		try {
			if(values == null){
				this.remove(this.name);
			}else{
				this.put(this.name, values);
			}
		} catch (JSONException e) {
			logger.warn("message: ", e);
		}
	}
}
