package com.mobilejazz.library;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectContext 
{
	// ---------------------------------------------------------------------------------------------------------
	// Class attributes
	// ---------------------------------------------------------------------------------------------------------
	private Map<String, BaseObject> objects;
	private Set<BaseObject> deletedObjects;
	private boolean hasChanges;
	
	private iPersistentStore persistentStore;
	private boolean isSaving;
	
	// ---------------------------------------------------------------------------------------------------------
	// Creating instances and initializing
	// ---------------------------------------------------------------------------------------------------------
	
	public ObjectContext() 
	{
		super();
		this.objects = new HashMap<String, BaseObject>();
		this.deletedObjects = new HashSet<BaseObject>();
		this.hasChanges = false;
		this.isSaving = false;
	}
	
	public ObjectContext(iPersistentStore persistentStore) 
	{
		super();
		this.objects = new HashMap<String, BaseObject>();
		this.deletedObjects = new HashSet<BaseObject>();
		this.hasChanges = false;
		
		this.isSaving = false;
		this.persistentStore = persistentStore;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Getters & Setters
	// ---------------------------------------------------------------------------------------------------------

	public boolean hasChanges()
	{
		if (hasChanges)
			return true;

		for (BaseObject object : objects.values()) 
		{
			if (object.hasChanges())
				return true;
		}
		
		return false;
	}
	
	public iPersistentStore getPeristentStore() 
	{
		return persistentStore;
	}
	
	public boolean isSaving()
	{
		return this.isSaving;
	}
		
	/// ---------------------------------------------------------------------------------------------------------
	/// Object manipulation
	/// ---------------------------------------------------------------------------------------------------------
	
	public BaseObject getObjectForKey(String key)
	{
		BaseObject object = objects.get(key);
		
		if (object == null)
			object = this.getBaseObjectFromPersistentStore(key);
		
		return object;
	}
	
	public boolean containsObjectWithKey(String key)
	{
		return objects.containsKey(key);
	}
	
	public BaseObject[] registeredObjects() 
	{
		BaseObject[] array = (BaseObject[]) objects.values().toArray();
		return array;
	}
	
	public boolean insertObject(BaseObject object)
	{
		if (object == null)
			throw new IllegalArgumentException("You cannot insert a nil object into a context.");
	
		if (object.getKey() == null)
			throw new IllegalArgumentException("You cannot insert an object with a null key.");
	
		if (object.getContext() != null)
		{
			if (object.getContext() == this)
			{
				return true;
			}
			else
			{
				// BaseObject cannot register to two different contexts. First unregister from the first context and then register to the second one.
				return false;
			}
		}
		
		if (this.containsObjectWithKey(object.getKey()))
			return false;
		
		this.hasChanges = true;
		this.objects.put(object.getKey(), object);
		
		object.setContext(this);
		
		return true;
	}
	
	public void deleteObject(BaseObject object)
	{
		if (object == null)
			return;
		
		if (objects.containsKey(object.getKey()))
		{
			this.hasChanges = true;
			
			BaseObject deletingObject = objects.get(object.getKey()); 
			
			this.objects.remove(object.getKey());
			this.deletedObjects.add(deletingObject);
			
			deletingObject.setContext(null);
		}
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Dealing with persistence
	// ---------------------------------------------------------------------------------------------------------
	
	// TODO : perform in background thread
	public void save()
	{
		if (this.persistentStore == null)
			return;
		
		this.isSaving = true;
		
		// Lock thread here
		
		boolean shouldSaveContext = this.hasChanges;
		
		// -- Modified objects -- //
		Set<BaseObject> savedObjects = new HashSet<BaseObject>();
		BaseObject[] allObjects = new BaseObject[this.objects.size()];
		allObjects = this.objects.values().toArray(allObjects);
		for (BaseObject bo : allObjects)
		{
			if (bo.hasChanges())
			{
				shouldSaveContext = true;
				this.updatePersistentObjectFromBaseObject(bo);
				bo.setHasChanges(false);
				savedObjects.add(bo);
			}
		}
		
		// -- Deleted objects -- //
		BaseObject[] deletedObjects = new BaseObject[this.deletedObjects.size()];
		deletedObjects = this.deletedObjects.toArray(deletedObjects);
		
		shouldSaveContext |= deletedObjects.length > 0;
		for (BaseObject bo : deletedObjects)
		{
			this.persistentStore.deletePersistentObject(bo.getKey());
		}
		
		// -- Performing Persistent Save --//
		boolean succeed = false;
		if (shouldSaveContext)
			succeed = this.persistentStore.save();
		
		if (succeed)
			this.deletedObjects.clear();
		
		this.hasChanges = false;
		
		this.isSaving = false;
		
		// Unlock thread here
		
		// Send Notifications here
	}
	
	public BaseObject[] getObjectsOfType(String type)
	{
		PersistentObject[] persistentObjects = this.persistentStore.getPersistentObjects(type);
		
		BaseObject[] baseObjects = new BaseObject[persistentObjects.length];
	
		int index = 0;
		for (PersistentObject po : persistentObjects)
		{
			BaseObject bo = this.objects.get(po.getKey());
			
			if (bo == null)
			{
				bo = this.createBaseObjectFromPersistentObject(po);
				this.insertObject(bo);
			}
			
			baseObjects[index] = bo;
			index++;
		}
		
		return baseObjects;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Private Methods
	// ---------------------------------------------------------------------------------------------------------
	
	private BaseObject getBaseObjectFromPersistentStore(String key)
	{
		if (this.persistentStore == null)
			return null;
		
		PersistentObject po = this.persistentStore.getPersistentObject(key);
		
		if (po != null)
		{
			BaseObject baseObject = this.createBaseObjectFromPersistentObject(po);
			this.insertObject(baseObject);
			
			return baseObject;
		}
		
		return null;
	}
	
	private BaseObject createBaseObjectFromPersistentObject(PersistentObject po)
	{
		assert po != null : "PersistentObject must be not null.";
		assert po.getKey() != null : "PersistentObject must have a valid key.";
		
		BaseObject baseObject = BaseObject.deserialize(po.getData());
		baseObject.setLastUpdate(new Date(po.getLastUpdate()));
		
		return baseObject;
	}
	
	private void updatePersistentObjectFromBaseObject(BaseObject bo)
	{
		if (this.persistentStore == null)
			return;
		
		if (bo == null)
			return;
		
		PersistentObject po = this.persistentStore.getPersistentObject(bo.getKey());
		
		if (po == null)
		{
			po = this.persistentStore.createPersistentObject(bo.getKey());
			po.setType(bo.getBaseObjectType());
		}
		
		if (bo.getLastUpdate() != null)
			po.setLastUpdate(bo.getLastUpdate().getTime());
		
		po.setData(bo.serialize());
	}

	// ---------------------------------------------------------------------------------------------------------
	// Typical Methods
	// ---------------------------------------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		return "ObjectContext [objects(" + objects.size() + ")=" + objects + ", deletedObjects(" + deletedObjects.size() + ")="
				+ deletedObjects + ", hasChanges=" + hasChanges
				+ ", persistentStore=" + persistentStore + "]";
	}
}
