package com.mobilejazz.library;

public interface iPersistentStore 
{
	static public enum DELETE_POLICY 
	{
		CREATION_DATE,
		ACCESS_DATE,
		UPDATE_DATE,
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Main Methods
	// ---------------------------------------------------------------------------------------------------------

	public PersistentObject getPersistentObject(String key);
	public PersistentObject[] getPersistentObjects(String type);
	
	public PersistentObject createPersistentObject(String key);
	public void deletePersistentObject(String key);
	public void deletePersistentObjects(String type, long olderThanTime, DELETE_POLICY deletePolicy);
	
	public boolean save();
	public void reset();
}
