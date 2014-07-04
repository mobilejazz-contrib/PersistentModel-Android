PersistentModel-Android
=======================

Easy persistent model creation in Android. 

## How To

### Classes

- **`BaseObject`**: Superclass for your model objects. A `BaseObject` has a reference to its `ObjectContext` (that might be `NULL` if there is no context). Also, a `BaseObject` has a `key` attribute. This key wil uniquely identify the object in the persistent model. Therefore, this key must be unique among all other base objects.

- **`ObjectContext`**: Class that retains living instances of `BaseObjects`. An object context has an `iPersistentStore` (optionally), and will awake persisted objects from the persistent store when objects are requested. Also, when calling the method `save()`, the object context will save all objects to the persistent store (if available).

- **`iPersistentStore`**: Abstract superclass for persistent stores. This class is a interface to the persistent storage, that must be implemented in a subclass of `iPersistentStore`.

- **`PersistentObject`**: Abstract superclass for the persistent stores objec entries. This class is used by the `iPersistentStore` and must be subclassed. 

- **`PersistentSQLiteStore`**: Implementation of the `iPersistentStore` using a SQLite database.

- **`PersistentSQLiteObject`**: Implementation of the `PersistentObject` for the `PersistentSQLiteStore`.

## Sample Code

### Getting an stored instance

The following code shows how to retrieve from persistence an stored object:

    Context applicationContext = <APPLICATION_CONTEXT>;
    
    // Creating the persistent store
    iPersistentStore persistentStore = new PersistentSQLiteStore(applicationContext, "MyPersistentSQLStoreName");
    
    // Creating a defautl object context
    ObjectContext defaultObjectContext = new ObjectContext(persistentStore);
    
    // Getting the object with key "object-3". 
    // If the object is on the SQLite data base, will be awaked, created and stored in the context and finally returned.
    // If already created previously, the context will retain it and will be return it directly.
    // Otherwise will return NULL.
    MyCustomObject myObject = defaultObjectContext.getObjectForKey("object-3");
    
### Creating a new instance and saving it
    
The following code shows how to create a new object and store it to the persistent storage:

    // Creating the persistent store
    iPersistentStore persistentStore = new PersistentSQLiteStore(applicationContext, "MyPersistentSQLStoreName");
    
    // Creating a defautl object context
    ObjectContext defaultObjectContext = new ObjectContext(persistentStore);
    
    // Creating a new object with key "object-4"
    MyCustomObject myObject = new MyCustomObject("object-4"); 
    
    // Inserting the new object into the context. 
    // Returns true if succeed, false otherwise.
    // This method will return false if there is already an existing object with myObject.key key.
    boolean succeed = defaultObjectContext.insertObject(myObject);
    
    // Saving the context to the persistent store.
    defaultObjectContext.save();
    
### Getting groups of objects

The following code shows how to manage groups of objects:

    // Creating the persistent store
    iPersistentStore persistentStore = new PersistentSQLiteStore(applicationContext, "MyPersistentSQLStoreName");
    
    // Creating a defautl object context
    ObjectContext defaultObjectContext = new ObjectContext(persistentStore);
    
    // Getting from persistence all objects of type "MyCustomObject"
    BaseObject [] objects = defaultObjectContext.getObjectsOfType("MyCustomObject");
    
    
