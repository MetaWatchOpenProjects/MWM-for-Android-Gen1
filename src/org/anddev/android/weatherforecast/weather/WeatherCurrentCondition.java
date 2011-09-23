package org.anddev.android.weatherforecast.weather;

/**
 * Holds the information between the <current_conditions>-tag of what the Google
 * Weather API returned.
 */
public class WeatherCurrentCondition {

	// ===========================================================
	// Fields
	// ===========================================================

	private String dayofWeek = null;
	private Integer tempCelcius = null;
	private Integer tempFahrenheit = null;
	private String iconURL = null;
	private String condition = null;
	private String windCondition = null;
	private String humidity = null;
	private String city = null;

	// ===========================================================
	// Constructors
	// ===========================================================

	public WeatherCurrentCondition() {

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getCity() {
		return this.city;
	}
	
	public String getDayofWeek() {
		return this.dayofWeek;
	}

	public void setDayofWeek(String dayofWeek) {
		this.dayofWeek = dayofWeek;
	}

	public Integer getTempCelcius() {
		return this.tempCelcius;
	}

	public void setTempCelcius(Integer temp) {
		this.tempCelcius = temp;
	}

	public Integer getTempFahrenheit() {
		return this.tempFahrenheit;
	}

	public void setTempFahrenheit(Integer temp) {
		this.tempFahrenheit = temp;
	}

	public String getIconURL() {
		return this.iconURL;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}

	public String getCondition() {
		return this.condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getWindCondition() {
		return this.windCondition;
	}

	public void setWindCondition(String windCondition) {
		this.windCondition = windCondition;
	}

	public String getHumidity() {
		return this.humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
}