package com.mobilejazz.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mobilejazz.app.model.User;
import com.mobilejazz.library.ObjectContext;
import com.mobilejazz.library.PersistentSQLiteStore;
import com.mobilejazz.library.iPersistentStore;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();

    private iPersistentStore persistentStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPersistenceStore();

        testInsertUser();
        testGetUser();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initPersistenceStore () {
        persistentStore = new PersistentSQLiteStore(this, "Test");
    }

    private void testInsertUser () {
        ObjectContext objectContext = new ObjectContext(persistentStore);

        User user = new User();
        user.setIdUser(1);
        user.setName("Jose");
        user.setKey(User.class.getName());

        boolean succeed = objectContext.insertObject(user);

        objectContext.save();
    }

    private void testGetUser () {
        ObjectContext objectContext = new ObjectContext(persistentStore);

        User user = (User) objectContext.getObjectForKey(User.class.getName());

        if (user != null)
            Log.d(TAG, user.toString());
    }
}
