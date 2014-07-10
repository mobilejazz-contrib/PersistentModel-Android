package cat.mobilejazz.persistentmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

public class BaseObject implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String key;
	private transient Date lastUpdate;
	private transient boolean hasChanges;
	private transient ObjectContext context;
	
	// ---------------------------------------------------------------------------------------------------------
	// Getters and Setters
	// ---------------------------------------------------------------------------------------------------------
	
	public String getKey()
	{
		return key;
	}
	
	public void setKey(String key)
	{
		this.key = key;
	}

	public Date getLastUpdate()
	{
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public boolean hasChanges() 
	{
		return hasChanges;
	}

	public void setHasChanges(boolean hasChanges) 
	{
		this.hasChanges = hasChanges;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Public Methods
	// ---------------------------------------------------------------------------------------------------------
	
	public void flagChanges()
	{
		this.setHasChanges(true);
	}
	
	public String getBaseObjectType()
	{
		return this.getClass().getSimpleName();
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Creating instances and initializing
	// ---------------------------------------------------------------------------------------------------------

    public BaseObject(){};

	public BaseObject(String key)  
	{
		super();
		
		this.key = key;
		this.hasChanges = (key != null);
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Object context management
	// ---------------------------------------------------------------------------------------------------------
		
	public ObjectContext getContext()
	{
		return context;
	}
	
	void setContext(ObjectContext context)
	{
		this.context = context;
	}
	
	public void deleteObjectFromContext()
	{
		if (this.context != null)
			this.context.deleteObject(this);
	}
	
//	public boolean registerToContext(ObjectContext context)
//	{
//		if (context != null)
//		{
//			boolean succeed = context.insertObject(this);
//
//			if (succeed == false)
//				this.setContext(null);
//
//			return succeed;
//		}
//		else
//		{
//			this.deleteObjectFromContext();
//			return true;
//		}
//	}

	// ---------------------------------------------------------------------------------------------------------
	// Serialization
	// ---------------------------------------------------------------------------------------------------------
	
	protected final byte[] serialize()
	{
		try 
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);   
			out.writeObject(this);
			
			byte[] bytes = bos.toByteArray();
			
			out.close();
			bos.close();
			
			return bytes;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected final static BaseObject deserialize(byte[] bytes)
	{
		try 
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = null;

			in = new ObjectInputStream(bis);
			BaseObject bo = (BaseObject)in.readObject(); 
			
			bis.close();
			in.close();
			
			return bo;
		} 
		catch (Exception e) 
		{
			// Nothing to do, just catch the exception.
			e.printStackTrace();
		} 
		
		return null;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Typical Methods
	// ---------------------------------------------------------------------------------------------------------
	
	@Override
	public String toString() 
	{
		return "BaseObject [key=" + key + ", lastUpdate=" + lastUpdate
				+ ", hasChanges=" + hasChanges + "]";
	}
}
