package cat.mobilejazz.persistentmodel;

public abstract class PersistentObject 
{
	private String key;
	private String type;
	private long lastUpdate;
	private byte[] data;
	
	private boolean hasChanges;
	
	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------	
	
	public String getKey() 
	{
		return key;
	}
	
	public void setKey(String key) 
	{
		if (this.key != null)
			this.setHasChanges(this.key.equals(key));
		else if (key != null)
			this.setHasChanges(true);
		
		this.key = key;
	}
	
	public String getType() 
	{
		return type;
	}
	
	public void setType(String type) 
	{
		if (this.type != null)
			this.setHasChanges(!this.type.equals(type));
		else if (type != null)
			this.setHasChanges(true);
		
		this.type = type;
	}
	
	public long getLastUpdate() 
	{
		return lastUpdate;
	}
	
	public void setLastUpdate(long lastUpdate) 
	{
		this.setHasChanges(lastUpdate != this.lastUpdate);
		this.lastUpdate = lastUpdate;
	}
	
	public byte[] getData() 
	{
		return data;
	}
	
	public void setData(byte[] data) 
	{
		if (this.data != null)
			this.setHasChanges(this.data.equals(data));
		else if (data != null)
			this.setHasChanges(true);
		
		this.data = data;
	}
	
	public boolean hasChanges()
	{
		return hasChanges;
	}
	
	protected void setHasChanges(boolean hasChanges)
	{
		this.hasChanges = hasChanges;
	}
}
