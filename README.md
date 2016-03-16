# DroidModel
Android library for managing model

#Usage

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

# Operations

```
User u = new User();
u.email = "praveen@gmail.com";
u.name = "Praveen Penumaka";
u.save();
```

```
ArrayList<User> users = User.findAll(User.class);
```

```
User u = new User();
u.email = "praveen@gmail.com"; // primary key
u.load();
System.out.println(u.name); // prints - Praveen Penumaka
```


