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

	public HGeo() {
		super();
	}

	public HGeo(JSONObject jsonObj) throws JSONException {
		super(jsonObj, JSONObject.getNames(jsonObj));
	}

	public HGeo(double lng, double lat) {
		this();
		setLng(lng);
		setLat(lat);
	}

	/* Setter & Getter */
/**
 * @return Longitude of the location. Null if undefined.
 */
	public double getLng() {
		Double lng;
		try {
			lng = this.getDouble("lng");
		} catch (Exception e) {
			lng = null;
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
		Double lat;
		try {
			lat = this.getDouble("lat");
		} catch (Exception e) {
			lat = null;
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
