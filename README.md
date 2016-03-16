DroidModel
==========

DroidModel is an android library which provides model abstraction for SQLite database.
Trivial database tasks like creating tables, inserting or retriving are part of this library.



Usage
-----

- Create a package called models in the repository
- Create a class called ModelManager extending DroidModelManager

```
import android.content.Context;

import com.creativemindz.droidmodel.DroidModelManger;


public class ModelManager extends DroidModelManger {
    public static void init(Context context){
        databaseName = "SampleDatabaseName";
        databaseVersion = 1;
        initAll(context);
        // TODO: Set sync strategy

        // Initialize models to create all required tables
        User u = new User();
    }
}
```

You can provide database names and version here
Note: Automatic database version increment is not supported yet.

- Create models as required extending DroidModel

```
import com.creativemindz.droidmodels.DroidModel;

public class User extends DroidModel {

    @DroidModel.Key(type = "PrimaryKey")
    public String email;

    @DroidModel.Key(type = "String")
    public String name;

    public User(){
        super(User.class);
    }

}

```

Note: With DroidModel, you don't have to write SQL queries for each table ( Model in this case )

Supported Operations
--------------------

### Create table

```
User u = new User();
```

Note: If already created, this won't create table again

### Add entries
```
User u = new User();
u.email = "praveenpenumaka@github.io";
u.name = "Praveen Penumaka";
u.save();
```

### Retrieve one entry
```
User u = new User();
u.email = "praveenpenumaka@github.io";
u.load()
System.out.println(u.name); // prints - Praveen Penumaka
```

### Update one entry
```
User u = new User();
u.email = "praveenpenumaka@github.io";
u.load()
u.name = "Penumaka Praveen";
u.save();
```

### Retrieve all
```
ArrayList<User> users = User.findAll(User.class);
```


TODO
----

- Sync operation with remote URL
- Add threads and callbacks for handling huge operations
- SUpport more data structures like Arraylist, HashMap
