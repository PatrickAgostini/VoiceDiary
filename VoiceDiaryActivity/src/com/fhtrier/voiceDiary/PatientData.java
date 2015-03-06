package com.fhtrier.voiceDiary;

public class PatientData {

	public boolean male;
	public boolean smoker;
	public String password;
	public String userID;
	public PatientData(String userID, String password, boolean male, boolean smoker)
	{
		this.male     = male;
		this.smoker   = smoker;
		this.password = password;
		this.userID   = userID;

	}
}
