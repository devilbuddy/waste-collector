package com.dg.ssrl;

public class ScoreData {
	public final int sector;
	public final int wasteCollected;

	private String wasteCollectedString;
	private String sectorString;

	public ScoreData(int sector, int wasteCollected) {
		this.sector = sector;
		this.wasteCollected = wasteCollected;
		wasteCollectedString = "" + wasteCollected;
		sectorString = "" + sector;
	}

	public String getWasteCollectedString() {
		return wasteCollectedString;
	}

	public String getSectorString() {
		return sectorString;
	}
}
