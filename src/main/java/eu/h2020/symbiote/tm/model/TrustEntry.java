package eu.h2020.symbiote.tm.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author RuggenthalerC
 *
 *         Object to store platform and resource trust/reputation values.
 */
public class TrustEntry {

	public TrustEntry() {
		// left intentionally empty
	}

	/**
	 * Create new trust entry object.
	 * 
	 * @param type
	 * @param platformId
	 * @param resourceId
	 * @param value
	 */
	public TrustEntry(Type type, String platformId, String resourceId, Double value) {
		this.type = type;
		this.platformId = platformId;
		this.resourceId = resourceId;
		this.value = value;
		this.dateUpdated = new Date();
	}

	public enum Type {
		PLATFORM, RESOURCE
	}

	@JsonProperty("type")
	private Type type;

	@JsonProperty("date_updated")
	private Date dateUpdated;

	@JsonProperty("platform_id")
	private String platformId;

	@JsonProperty("resource_id")
	private String resourceId;

	@JsonProperty("value")
	private Double value;

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getDateUpdated() {
		return this.dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getPlatformId() {
		return this.platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getResourceId() {
		return this.resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public Double getValue() {
		return this.value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}