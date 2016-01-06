package com.dg.ssrl;

public class ScoreData {
	public final int depth;
	public final int wasteCollected;
	private String wasteCollectedString;
	private String sectorString;

	public ScoreData(int depth, int wasteCollected) {
		this.depth = depth;
		this.wasteCollected = wasteCollected;
		wasteCollectedString = "" + wasteCollected;
		sectorString = "" + depth;
	}

	public String getWasteCollectedString() {
		return wasteCollectedString;
	}

	public String getSectorString() {
		return sectorString;
	}
}
