package com.mobilejazz.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PersistentSQLiteStore extends SQLiteOpenHelper implements iPersistentStore 
{
    // Database Version
    private static final int DATABASE_VERSION = 1;
  
    // Table name
    private static final String TABLE_OBJECTS = "Objects";
    private static final String TABLE_DATA= "Data";
 
    // Objects Table Columns names
    private static final String OBJECTS_COLUMN_ID = "id";
    private static final String OBJECTS_COLUMN_KEY = "key";
    private static final String OBJECTS_COLUMN_TYPE = "type";
    private static final String OBJECTS_COLUMN_CREATION_DATE = "creationDate";
    private static final String OBJECTS_COLUMN_UPDATE_DATE = "updateDate";
    private static final String OBJECTS_COLUMN_ACCESS_DATE = "accessDate";
    
    // Data Table Columns names
    private static final String DATA_COLUMN_ID = "id";
    private static final String DATA_COLUMN_DATA = "data";
    
    // PersistentObjects management
    private Map<String, PersistentSQLiteObject> persistentObjects;
    private Set<PersistentSQLiteObject> insertedObjects;
    private Set<PersistentSQLiteObject> updatedObjects;
    private Set<PersistentSQLiteObject> deletedObjects;
    
	// ---------------------------------------------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------------------------------------------
	
	public PersistentSQLiteStore(Context context, String databaseName) 
	{
		super(context, databaseName, null, DATABASE_VERSION);
		
		this.persistentObjects = new HashMap<String, PersistentSQLiteObject>();
		this.insertedObjects = new HashSet<PersistentSQLiteObject>();
		this.updatedObjects = new HashSet<PersistentSQLiteObject>();
		this.deletedObjects = new HashSet<PersistentSQLiteObject>();
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Extending SQLiteOpenHelper
	// ---------------------------------------------------------------------------------------------------------
	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String CREATE_OBJECTS_TABLE = "CREATE TABLE " + TABLE_OBJECTS + " ("
				+ OBJECTS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ OBJECTS_COLUMN_KEY + " TEXT UNIQUE NOT NULL, "
				+ OBJECTS_COLUMN_TYPE + " TEXT, "
				+ OBJECTS_COLUMN_CREATION_DATE + " INTEGER, "
				+ OBJECTS_COLUMN_UPDATE_DATE + " INTEGER, "
				+ OBJECTS_COLUMN_ACCESS_DATE + " INTEGER" 
				+ ")";
		
		String CREATE_DATA_TABLE = "CREATE TABLE " + TABLE_DATA + " ("
				+ DATA_COLUMN_ID + " INTEGER PRIMARY KEY, "
				+ DATA_COLUMN_DATA + " BLOB, " 
				+ "FOREIGN KEY(" + DATA_COLUMN_ID + ") REFERENCES " + TABLE_OBJECTS + "(" + OBJECTS_COLUMN_ID + ")" 
				+ ")";
		
		db.execSQL(CREATE_OBJECTS_TABLE);
		db.execSQL(CREATE_DATA_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		 db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
		 db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBJECTS);
		 this.onCreate(db);
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Implementing PersistentStore
	// ---------------------------------------------------------------------------------------------------------
	
	@Override
	public PersistentSQLiteObject getPersistentObject(String key)
	{
		PersistentSQLiteObject po = this.persistentObjects.get(key);

		if (po == null)
		{
			SQLiteDatabase db = this.getReadableDatabase();

			String sqlQuery = "SELECT " 
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_ID + ", "
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_TYPE + ", "
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_UPDATE_DATE + ", "
					+ TABLE_DATA 	+ "." + DATA_COLUMN_DATA + " "
					+ "FROM "
					+ TABLE_OBJECTS + " JOIN " +  TABLE_DATA + " ON " 
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_ID + " = " + TABLE_DATA + "." + DATA_COLUMN_ID + " " 
					+ "WHERE "
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_KEY + " = \"" + key + "\"";

			Cursor cursor = db.rawQuery(sqlQuery, null);

			if (cursor.moveToFirst())
			{
				po = new PersistentSQLiteObject(cursor.getInt(0));
				po.setKey(key);
				po.setType(cursor.getString(1));
				po.setLastUpdate(cursor.getLong(2));
				po.setData(cursor.getBlob(3));
				
				po.setHasChanges(false);
				po.setPersistentStore(this);
				
				//Log.d("CTV", "GETTER last update: " + po.getLastUpdate());
				
				cursor.close();
			}
			
			if (po != null)
			{
				this.persistentObjects.put(key, po);
			}
		}
		
		if (po != null)
			this.markAccessOfPersistentObjectWithDatabaseID(po.getDbID());

		return po;
	}

	@Override
	public PersistentSQLiteObject[] getPersistentObjects(String type) 
	{
		if (type == null)
			return null;
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		String sqlQuery = "SELECT " 
				+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_ID + ", "
				+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_KEY + ", "
				+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_TYPE + ", "
				+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_UPDATE_DATE + ", "
				+ TABLE_DATA 	+ "." + DATA_COLUMN_DATA + " "
				+ "FROM "
				+ TABLE_OBJECTS + " JOIN " +  TABLE_DATA + " ON " 
				+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_ID + " = " + TABLE_DATA + "." + DATA_COLUMN_ID + " " 
				+ "WHERE "
				+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_TYPE + " = \"" + type + "\"";
		
		Cursor cursor = db.rawQuery(sqlQuery, null);

		Set<PersistentSQLiteObject> objects = new HashSet<PersistentSQLiteObject>();
		
		while (cursor.moveToNext())
		{
			String key = cursor.getString(1);
			
			PersistentSQLiteObject po = this.persistentObjects.get(key);
			
			if (po == null)
			{
				po = new PersistentSQLiteObject(cursor.getInt(0));
				this.persistentObjects.put(key, po);
			}
			
			po.setKey(key);
			po.setType(cursor.getString(2));
			po.setLastUpdate(cursor.getLong(3));
			po.setData(cursor.getBlob(4));
			
			
			po.setHasChanges(false);
			po.setPersistentStore(this);
			
			//Log.d("CTV", "GETTER ARRAY last update in po: " + po.getLastUpdate());

			objects.add(po);
		}
		
		cursor.close();
		
		for (PersistentSQLiteObject po : objects)
			this.markAccessOfPersistentObjectWithDatabaseID(po.getDbID());
		
		PersistentSQLiteObject[] returnObjects = new PersistentSQLiteObject[objects.size()];
		returnObjects = objects.toArray(returnObjects);
		return returnObjects;
	}

	@Override
	public PersistentSQLiteObject createPersistentObject(String key) 
	{
		assert key != null : "Impossible to create a persistent object with a null key.";
		
		PersistentSQLiteObject po = this.persistentObjects.get(key);
		
		assert po == null : "Cannot create a persistent object because it exists already an object with the given key.";

		po = new PersistentSQLiteObject();
		po.setKey(key);
		
		this.persistentObjects.put(key, po);
		
		po.setHasChanges(true);
		po.setPersistentStore(this);
		
		this.insertedObjects.add(po);
		
		return po;
	}

	@Override
	public void deletePersistentObject(String key) 
	{
		assert key != null : "Impossible to delete a persistent object with a null key.";
		
		PersistentSQLiteObject po = this.persistentObjects.get(key);
		this.persistentObjects.remove(key);
		
		// If the object is queued to be inserted, remove from the queue.
	    if (this.insertedObjects.contains(po))
	    {
	    	this.insertedObjects.remove(po);
	    }
	    // If the object is queued to save changes, remove form the queue and add to deleted objects list.
	    else if (this.updatedObjects.contains(po))
	    {
	        this.updatedObjects.remove(po);
	        this.deletedObjects.add(po);
	    }
	    else
	    {
	        // If exists persistent object, add to deleted objects list
	        if (po != null)
	            this.deletedObjects.add(po);
	        
	        // otherwise, there is nothing to do, the object is not stored in persistence.
	    }
	}

	@Override
	public void deletePersistentObjects(String type, long olderThanTimeInterval, DELETE_POLICY deletePolicy) 
	{
		String deleteOption = null;
		switch (deletePolicy)
		{
			case CREATION_DATE:
				deleteOption = OBJECTS_COLUMN_CREATION_DATE;
				break;
			
			case ACCESS_DATE:
				deleteOption = OBJECTS_COLUMN_ACCESS_DATE;
				break;
			
			case UPDATE_DATE:
				deleteOption = OBJECTS_COLUMN_UPDATE_DATE;
				break;
		}
		
		String dataWhereQuery = null;
		String objectsWhereQuery = null;
		
		if (type != null && olderThanTimeInterval < 0)
		{
			dataWhereQuery = DATA_COLUMN_ID + " IN ("
					+ "SELECT "
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_ID + " "
					+ "FROM " + TABLE_OBJECTS + " "
					+ "WHERE " + OBJECTS_COLUMN_TYPE + " = \"" + type + "\"" 
					+ ")";
			
		}
		else if (type == null && olderThanTimeInterval >= 0)
		{
			dataWhereQuery = DATA_COLUMN_ID + " IN ("
					+ "SELECT "
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_ID + " "
					+ "FROM " + TABLE_OBJECTS + " "
					+ "WHERE " + deleteOption + " < " + olderThanTimeInterval
					+ ")";
			
			objectsWhereQuery = deleteOption + " < " + olderThanTimeInterval;
		}
		else if (type != null && olderThanTimeInterval >= 0)
		{
			dataWhereQuery = DATA_COLUMN_ID + " IN ("
					+ "SELECT "
					+ TABLE_OBJECTS + "." + OBJECTS_COLUMN_ID + " "
					+ "FROM " + TABLE_OBJECTS + " "
					+ "WHERE " 
					+ OBJECTS_COLUMN_TYPE + " = \"" + type + "\" "
					+ "AND "
					+ deleteOption + " < " + olderThanTimeInterval
					+ ")";
			
			objectsWhereQuery = OBJECTS_COLUMN_TYPE + " = \"" + type + "\" "
					+ "AND "
					+ deleteOption + " < " + olderThanTimeInterval;
		}
		else //if (type == null && olderThanTimeInterval < 0)
		{
			// Nothing to do
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		Log.d("CTV - DB", "opening writable db");
		
		db.delete(TABLE_DATA, dataWhereQuery, null);
		db.delete(TABLE_OBJECTS, objectsWhereQuery , null);
		
		db.close();
		Log.d("CTV - DB", "db closed");
	}

	@Override
	public boolean save() 
	{
		boolean succeed = true;
		
		PersistentSQLiteObject [] insertedObjects = new PersistentSQLiteObject[this.insertedObjects.size()];
		PersistentSQLiteObject [] deletedObjects = new PersistentSQLiteObject[this.deletedObjects.size()];
		PersistentSQLiteObject [] updatedObjects = new PersistentSQLiteObject[this.updatedObjects.size()];
		
		insertedObjects = this.insertedObjects.toArray(insertedObjects);
		deletedObjects = this.deletedObjects.toArray(deletedObjects);
		updatedObjects = this.updatedObjects.toArray(updatedObjects);
		
		this.insertedObjects.clear();
		this.deletedObjects.clear();
		this.updatedObjects.clear();
		
        // -- Inserted Objects -- //
        for (PersistentSQLiteObject po : insertedObjects)
        {
        	boolean flag = this.insertPersistentObject(po);
        	succeed = succeed || flag;
        }
        
        // -- Deleted Objects -- //
        for (PersistentSQLiteObject po : deletedObjects)
        {
        	boolean flag = this.deletePersistentObject(po);
        	succeed = succeed || flag;
        }
        
        // -- Updated Objects -- //
        for (PersistentSQLiteObject po : updatedObjects)
        {
        	boolean flag = this.updatePersistentObject(po);
            if (flag)
            	po.setHasChanges(false);
            succeed = succeed || flag;
        }
		
		return succeed;
	}
	
	public void reset()
	{
		this.persistentObjects.clear();
		this.insertedObjects.clear();
		this.deletedObjects.clear();
		this.updatedObjects.clear();
		
		SQLiteDatabase db = this.getWritableDatabase();
		Log.d("CTV - DB", "opening writable db");
		
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBJECTS);
		this.onCreate(db);
		
		db.close();
		Log.d("CTV - DB", "db closed");
	}
	
	protected void didChangePersistentSQLiteObject(PersistentSQLiteObject po)
	{
		if (po.getDbID() != PersistentSQLiteObject.UNDEFINED_DATABASE_ID)
		{
			updatedObjects.add(po);
		}
	}
		
	// ---------------------------------------------------------------------------------------------------------
	// Database CRUD operations
	// ---------------------------------------------------------------------------------------------------------
	
	private boolean insertPersistentObject(PersistentSQLiteObject po)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		Log.d("CTV - DB", "opening writable db");
		
		boolean successful = false;
		
		try
		{
			db.beginTransaction();

			ContentValues objectValues = new ContentValues();
			objectValues.put(OBJECTS_COLUMN_KEY, po.getKey());
			objectValues.put(OBJECTS_COLUMN_TYPE, po.getType());
			objectValues.put(OBJECTS_COLUMN_CREATION_DATE, System.currentTimeMillis());
			objectValues.put(OBJECTS_COLUMN_UPDATE_DATE, po.getLastUpdate());
			
			//Log.d("CTV", "INSERT last update in po: " + po.getLastUpdate());

			long objectsRowID = db.insertOrThrow(TABLE_OBJECTS, null, objectValues);
			po.setDbID(objectsRowID);
			
			ContentValues dataValues = new ContentValues();
			dataValues.put(DATA_COLUMN_ID, objectsRowID);
			dataValues.put(DATA_COLUMN_DATA, po.getData());

			db.insertOrThrow(TABLE_DATA, null, dataValues);
			
			successful = true;
			db.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			// nothing to do
		}
		finally
		{
			db.endTransaction();
		}
		
		db.close();
		Log.d("CTV - DB", "db closed");
		
		return successful;
	}
	
	private boolean updatePersistentObject(PersistentSQLiteObject po)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		Log.d("CTV - DB", "opening writable db");
		
		boolean successful = false;
		
		try
		{
			db.beginTransaction();
			
			ContentValues objectValues = new ContentValues();
			objectValues.put(OBJECTS_COLUMN_TYPE, po.getType());
			objectValues.put(OBJECTS_COLUMN_UPDATE_DATE, po.getLastUpdate());
			objectValues.put(OBJECTS_COLUMN_ACCESS_DATE, System.currentTimeMillis()); // <--- this line might be deleted
			
			//Log.d("CTV", "UPDATE last update in po: " + po.getLastUpdate());
			
			db.update(TABLE_OBJECTS, objectValues, OBJECTS_COLUMN_ID + "=" + po.getDbID(), null);
			
			ContentValues dataValues = new ContentValues();
			dataValues.put(DATA_COLUMN_DATA, po.getData());
			
			db.update(TABLE_DATA, dataValues, DATA_COLUMN_ID + "=" + po.getDbID(), null);
			
			successful = true;
			db.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			// nothing to do
		}
		finally
		{
			db.endTransaction();
		}
		
		db.close();
		Log.d("CTV - DB", "db closed");
		
		return successful;
	}
	
	private boolean deletePersistentObject(PersistentSQLiteObject po)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		Log.d("CTV - DB", "opening writable db");
		
		boolean successful = false;
		
		try
		{
			db.beginTransaction();
			
			db.delete(TABLE_DATA,  DATA_COLUMN_ID + "=" + po.getDbID(), null);
			db.delete(TABLE_OBJECTS,  OBJECTS_COLUMN_ID + "=" + po.getDbID(), null);
			
			successful = true;
			db.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			// nothing to do
		}
		finally
		{
			db.endTransaction();
		}
		
		db.close();
		Log.d("CTV - DB", "db closed");
		
		return successful;
	}
	
	private boolean markAccessOfPersistentObjectWithDatabaseID(long dbID)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		Log.d("CTV - DB", "opening writable db");
		
		boolean successful = false;
		
		try
		{
			db.beginTransaction();
			
			ContentValues objectValues = new ContentValues();
			objectValues.put(OBJECTS_COLUMN_ACCESS_DATE, System.currentTimeMillis()); 
			
			db.update(TABLE_OBJECTS, objectValues, OBJECTS_COLUMN_ID + "=" + dbID, null);
			
			successful = true;
			db.setTransactionSuccessful();
		}
		catch (SQLException e)
		{
			// nothing to do
		}
		finally
		{
			db.endTransaction();
		}
		
		db.close();
		Log.d("CTV - DB", "db closed");
		
		return successful;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	// Other Methods
	// ---------------------------------------------------------------------------------------------------------
	
	public void saveAndClearCache()
	{
		this.save();
		this.persistentObjects.clear();
	}
}
