# SharedDataAccess


[![](https://jitpack.io/v/eiliyaabedini/lib-sharedDataAccess.svg)](https://jitpack.io/#eiliyaabedini/lib-sharedDataAccess)

SharedDataAccess library is for simple use of SharedPreferences in android

all thing you need is import library, and create interface with setter and getter methods

Also see[My Android learning blog](http://iact.ir)



Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.eiliyaabedini:lib-sharedDataAccess:1.0.2'
}
```

## How to use

Create an interface with getter and setter methods like this

```java
public interface SharedModel {
    String getToken();
    void setToken(String token);
}
```

Then you need to create an instance of SharedDataAccess in your Application class

```java
SharedModel sharedModel = new SharedDataAccess(this).create(SharedModel.class);
```

Now you can use your method every where you want

set data to SharedPreferences 
```java
sharedModel.setToken("MyToken");
```

get data from SharedPreferences 
```java
String token = sharedModel.getToken();
```


## Note

- Interface methods must start with `set` or `get` and then with a name which used for key.
- **Double** not supported by SharedPreferences
- Supported type is `String`, `int`, `long`, `float`, `boolean`
- you can also set and get a custom Object with **supported types fields**.


## Save an Object

- To save an Object in SharedPreferences your class must have **supported type fields**.
- Only `public` fields can access by SharedDataAccess and store data
- Your class must have a **default empty constructor**


My custom Object:
```java
public class MyObject {
    public int id;
    public String name;

    public MyObject(){

    }

    public MyObject(int id,String name){
        this.id = id;
        this.name = name;
    }
}
```

Interface:
```java
public interface SharedModel {
    MyObject getMyObject();
    void setMyObject(MyObject myObject);
}
```

Create instance of SharedDataAccess
```java
SharedModel sharedModel = new SharedDataAccess(this).create(SharedModel.class);
```


set data to SharedPreferences
```java
sharedModel.setMyObject(new MyObject(1,"Test Data"));
```

get data from SharedPreferences
```java
MyObject myObject = sharedModel.getMyObject();
```
## More

Your interface can extends from SharedDefaultValues to have default value methods

```java
public interface SharedModel extends SharedDefaultValues {
    String getToken();
    void setToken(String token);
}
```

