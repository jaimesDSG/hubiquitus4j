package org.hubiquitus.util;

public enum HubotStatus {
	
	CREATED (0), 			// just created, not connected througth to the hclient API
	STARTED (1), 			// connected to the hAPI but not ready
	INITIALIZED (2),		// connected and initialized
	READY (3), 				// connected and ready. Adapters are all initialized and started
	STOPPED (4);			// disconnected and adapters are all stopped
	
	private int value;
	
	private HubotStatus(int value) {
		this.value = value;
	}
	
	/**
	 * @return int equivalent.
	 */
	public int value() {
		return value;
	}
	
	/**
	 * Get constant for value
	 * @param value
	 * @return
	 */
	public static HubotStatus constant(int value) {
		HubotStatus[] _values = HubotStatus.values();
		return _values[value];
	}
}
