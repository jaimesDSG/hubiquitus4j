package org.hubiquitus.hapi.hStructures;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  @version v0.5
 *  Specifies the exact longitude and latitude of the location
 */
public class HGeo extends JSONObject {

	final Logger logger = LoggerFactory.getLogger(HGeo.class);

	public HGeo(JSONObject jsonObj) throws JSONException{
		super(jsonObj, JSONObject.getNames(jsonObj));
	}
	
	//ac lat and lng is mandatory
	public HGeo(double lng, double lat) {
		super();
		setLng(lng);
		setLat(lat);
	}

	/* Setter & Getter */
/**
 * @return Longitude of the location. Null if undefined.
 */
	public double getLng() {
		double lng;
		try {
			lng = this.getDouble("lng");
		} catch (Exception e) {
			lng = 0;
		}
		return lng;
	}

	/**
	 * Set the longitude of the location.
	 * @param lng
	 */
	public void setLng(double lng) {
		try {
			this.put("lng", lng);
		} catch (JSONException e) {
			logger.warn("message: ", e);
		}
	}

	/**
	 * @return Latitude of the location. Null if undefined.
	 */
	public double getLat() {
		double lat;
		try {
			lat = this.getDouble("lat");
		} catch (Exception e) {
			lat = 0;
		}
		return lat;
	}

	/**
	 * Set the latitude of the location.
	 * @param lat
	 */
	public void setLat(double lat) {
		try {
			this.put("lat", lat);
		} catch (JSONException e) {
			logger.warn("message: ", e);
		}
	}
}
