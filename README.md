PersistentModel-Android
=======================

Easy persistent model creation in Android. 

## How To

Create your model classes and use for them the superclass `BaseObject`.

Then simply do the following:

    Context applicationContext = <APPLICATION_CONTEXT>;
    
    // Creating the persistent store
    iPersistentStore persistentStore = new PersistentSQLiteStore(applicationContext, "MyPersistentSQLStoreName");
    
    // Creating a defautl object context
    ObjectContext defaultObjectContext = new ObjectContext(persistentStore);
    
    // Getting the object with key "object-3"
    MyCustomObject myObject = defaultObjectContext.getObjectForKey("object-3");
    
    
    
    
