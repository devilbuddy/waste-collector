package com.dg.ssrl;

public class ScoreData {
	public final int sector;
	public final int wasteCollected;
	public final int score;

	private String wasteCollectedString;
	private String sectorString;
	private String scoreString;

	public ScoreData(int sector, int wasteCollected) {
		this.sector = sector;
		this.wasteCollected = wasteCollected;
		wasteCollectedString = "" + wasteCollected;
		sectorString = "" + sector;

		score = (int) (wasteCollected * 1.5f + sector);
		scoreString = "" + score;
	}

	public String getWasteCollectedString() {
		return wasteCollectedString;
	}

	public String getSectorString() {
		return sectorString;
	}

	public String getScoreString() {
		return scoreString;
	}
}
