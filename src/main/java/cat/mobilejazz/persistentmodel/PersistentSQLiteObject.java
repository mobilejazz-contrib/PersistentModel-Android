package cat.mobilejazz.persistentmodel;

public class PersistentSQLiteObject extends PersistentObject 
{
	public static long UNDEFINED_DATABASE_ID = -1;
	
	private long dbID;
	private PersistentSQLiteStore persistentStore;

	// ---------------------------------------------------------------------------------------------------------
	// Creating instances and initializing
	// ---------------------------------------------------------------------------------------------------------
	
	public PersistentSQLiteObject()
	{
		super();
		this.dbID = UNDEFINED_DATABASE_ID;
	}
	
	public PersistentSQLiteObject(long dbID)
	{
		super();
		this.dbID = dbID;
		this.setHasChanges(true);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	public long getDbID() 
	{
		return dbID;
	}
	
	protected void setDbID(long dbID)
	{
		this.setHasChanges(dbID != this.dbID);
		this.dbID = dbID;
	}

	protected PersistentSQLiteStore getPersistentStore() 
	{
		return persistentStore;
	}

	protected void setPersistentStore(PersistentSQLiteStore persistentStore) 
	{
		this.persistentStore = persistentStore;
	}
	
	@Override
	protected void setHasChanges(boolean hasChanges)
	{
		super.setHasChanges(hasChanges);
		
		if (persistentStore != null && hasChanges == true)
			persistentStore.didChangePersistentSQLiteObject(this);
	}
}
