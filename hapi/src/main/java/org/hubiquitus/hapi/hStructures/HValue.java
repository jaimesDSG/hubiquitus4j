package org.hubiquitus.hapi.hStructures;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version v0.5
 * This structure defines a simple condition value for the available operand
 */
public class HValue extends JSONObject {
	final Logger logger = LoggerFactory.getLogger(HValue.class);
	
	private String name;
	
	public HValue(){
		super();
	}
	/**
	 * @param name : The name of the attribute to compare with.
	 * @param value : The value of the attribute to compare with.
	 */
	public HValue(String name, Object value){
		super();
		this.name = name;
		setValue(value);
	}
	
	public HValue(JSONObject jsonObj) throws JSONException{
		super(jsonObj, JSONObject.getNames(jsonObj));
	}
	
	public HValue(String jsonString) throws JSONException{
		super(jsonString);
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
	 * @return The value of the attribute to compare with.
	 */
	public Object getValue() {
		if(this.name == null || this.name.length() <= 0){
			logger.error("message: The name of the attribute in HValue is null or empty");
			return null;
		}
		Object value;
		try {
			value  = this.get(this.name);
		} catch (Exception e) {
			value = null;
		}
		return value;
	}
	/**
	 * Set the value of the attribute to compare with.
	 * @param value
	 */
	public void setValue(Object value) {
		if(this.name == null || this.name.length() <= 0){
			logger.error("message: The name of the attribute in HValue is null or empty");
			return;
		}
		try {
			if(value == null){
				this.remove(this.name);
			}else{
				this.put(this.name, value);
			}
		} catch (JSONException e) {
			logger.warn("message : ", e);
		}
	}
	
	
}
