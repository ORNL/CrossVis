package gov.ornl.csed.cda.timevis;

import java.time.Instant;

public class TimeSeriesSelection {
	private Instant startInstant;
	private Instant endInstant;
	private int startScreenLocation;
	private int endScreenLocation;
	
	public TimeSeriesSelection(Instant startInstant, Instant endInstant, int startScreenLocation,
							   int endScreenLocation) {
		this.startInstant = startInstant;
		this.endInstant = endInstant;
		this.startScreenLocation = startScreenLocation;
		this.endScreenLocation = endScreenLocation;
	}
	
	public int getStartScreenLocation() {
		return startScreenLocation;
	}

	public void setStartScreenLocation(int startScreenLocation) {
		this.startScreenLocation = startScreenLocation;
	}

	public int getEndScreenLocation() {
		return endScreenLocation;
	}

	public void setEndScreenLocation(int endScreenLocation) {
		this.endScreenLocation = endScreenLocation;
	}
	
	public void setStartInstant (Instant startInstant) {
		this.startInstant = startInstant;
	}
	
	public Instant getStartInstant() {
		return startInstant;
	}
	
	public void setEndInstant(Instant endInstant) {
		this.endInstant = endInstant;
	}
	
	public Instant getEndInstant() {
		return endInstant;
	}
}
