package org.anddev.android.weatherforecast.weather;

import java.util.ArrayList;

/**
 * Combines one WeatherCurrentCondition with a List of
 * WeatherForecastConditions.
 */
public class WeatherSet {
	
	// ===========================================================
	// Fields
	// ===========================================================

	private WeatherCurrentCondition myCurrentCondition = null;
	private ArrayList<WeatherForecastCondition> myForecastConditions = 
		new ArrayList<WeatherForecastCondition>(4);

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public WeatherCurrentCondition getWeatherCurrentCondition() {
		return myCurrentCondition;
	}

	public void setWeatherCurrentCondition(
			WeatherCurrentCondition myCurrentWeather) {
		this.myCurrentCondition = myCurrentWeather;
	}

	public ArrayList<WeatherForecastCondition> getWeatherForecastConditions() {
		return this.myForecastConditions;
	}

	public WeatherForecastCondition getLastWeatherForecastCondition() {
		return this.myForecastConditions
				.get(this.myForecastConditions.size() - 1);
	}
}