# Sofa
*Android library that establishes and manages communication between an Android game and several game-pads (on other Android devices) using [wifi peer to peer](https://developer.android.com/guide/topics/connectivity/wifip2p.html).*  
**Require Android API level 14 (4.0) minimum**

## Overview

### Features
 * Wifi direct
 * Proxy (service in background)
 * clients and helpers
 * Two directionnal communication
 * Views: JoystickView
 * auto-reconnect, recover
 * Log and filter
 * Android views as inputs or outputs
 * Accelerometers and other sensor
 * Feedback

### How it works
 * Wifi direct (Wifi p2p)
 * Proxy (service)
 * Game-pad with UUID / id recovering in proxy
 * Clients: game / game-pad (fragment)
 * Exchanging Messages
 * [Sketch? global interaction, states, ]

### For what can it be used?
 * Multiplayer game requiring game-pads.
 * Game that needs several accelerometers or other sensors of the same type.
 * Game that needs 2 or more screens.
 * This library has been created for games but the application can be more various. Generally, for apps which needs communication between a commun device and several others .
 * Everything you want and you can imagine!


## Getting Sofa

### Import Sofa into your project (Eclipse)

* Import Sofa project into your workspace
* Add the library in your project (Project > Properties > Android > Add... > Sofa)
* In your `AndroidManifest.xml`, add the permissions:  
```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
```
and also the permission to use the vibrate if you want to use game-pad's feedback later:  
```xml
<uses-permission android:name="android.permission.VIBRATE"/>
```
 * In your `AndroidManifest.xml`, declare the proy service:
```xml
<application ... >
    <service android:name="com.fbessou.sofa.GameIOProxy"/>
</application>
```

### Create a game-pad activity
##### Create a simple activity (do not forget to declare it into your manifest)
##### Create a layout which contains the buttons and the views that you want to display on your game-pad:  
```xml
```
##### Define game-pad's informations:  
```java
GamePadInformation info = new GamePadInformation("Best game-pad ever");
```
Note: The constructor `GamePadInformation(String name)` is deprecated. You should use `GamePadInformation(String name, UUID uuid)` instead. The UUID must ideally stay the same each time the app is launched. Consequently, the UUID should be generated during the first launch and it should be immediately stored in the SharedPreferences in order to be reused for future launches. The first constructor is equivalent to `GamePadInformation(name, UUID.randomUUID())`.
##### Instatiate a GamePadIOHelper object in the `onCreate()` of the main activity:
```java
GamePadIOHelper easyIO = new GamePadIOHelper(getContext(), info);
```
##### Start the game-pad IO client:  
```java
// The start method takes a listener in parameter, it can be defined
// to be notified for each change of the state of the connection to
// the game. It can be set to null if you do not need it
easyIO.start(this);
```
##### Attach the Android views to the GamePadIOHelper:  
 * Define an input view:  
```java
// Get the android view
...
// Create a sensor
...
// Attach the sensor to the view. All the interactions with the
// view will be automatically transmit to the game.
...
```
 * Enable a sensor of the device:  
```java
// Create a sensor
...
// Attach the sensor. All the updates of the sensor's values will
// be automatically send to the game.
...
```
 * Define an output view:  
```java
// Get the android view
...
// Create an indicator
...
// Attach the indicator to the view. All the output event generated
// by the game will be automatically diplayed in this view.
...
```
 * Enable feedback:  
```java
// Create feedback indicator
...
// Attach the indicator
...
```

### Integrate Sofa in your game
##### Define the game's informations (currently you can only define the name of the game):
```java
GameInformation info = new GameInformation("The Game!");
```
##### Instantiate a GameIOHelper object in the `onCreate()` of your main activity:
```java
GameIOHelper easyIO = new GameIOHelper(getContext(), info);
```
##### Start the game client
  * Using the listener's methods (To handle the game-pad events in the GUI Thread):
  * Using the pollEvent methods (To handle the game-pad events from any thread you want):

##### Handle the game-pad events:
  * With the listeners:
  * With the pollEvent methods:

##### Send an output event:  
The GameIOHelper has two methods to send output events. The method `sendOutputEvent(event, gamepadId)` sends the output event to the game-pad with the given id. And the method `sendOutputEventBroadcast(event)` sends the output event to all the connected game-pads, it is equivalent to `sendOutputEvent(event, -1)`. The output events can be one of these:  
  * **Feedback** with a long/short vibration:  
```java
OutputEvent.createFeedback(OutputEvent.VIBRATE_SHORT);
```
  * **Feedback** with a custom vibrations:  
```java
ArrayList<Integer> vibrations = new ArrayList();
for(int i = 0; i < 3; i++) {
    vibrations.add(50); // 50ms pause
    vibrations.add(100); // 100m vibrate
}
OutputEvent.createFeedback(vibrations);
```
  * **Text event**, the id of the target game-pad's indicator must be specified:  
```java
OutputEvent.createTextEvent("Text to send", indicatorId);
```
  * **State event**, the id of the target game-pad's indicator must be specified:  
```java
// The state can be either STATE_TRUE or STATE_FALSE for a two state mode (see boolean indicator)
// Or any other values otherwise
OutputEvent.createStateEvent(OutputEvent.STATE_TRUE, indicatorId);
```
### See sample activities
You can check the sample activities in src/com/fbessou/sofa/activity/:
 * An example of game-pad activity: GamePadSampleActivity.java with its layout res/layout/activity_gamepad_sample.xml.
 * An example of a game (altough it is not really a game) activity using the listener's methods: GameListerSampleActivity.java

### Other tips
#### Create your own game-pad indicator

#### Send a personalized input event (game-pad)


#### Create and send a personalized output event (game)


#### Using custom message
You can exchange any data you need between the game-pads and the game by using the custom messages. A custom message must be a string. The two most common ways to deal with the custom messages is to use either JSON objects or base64 encoding.
 * With [JSON objects](https://developer.android.com/reference/org/json/JSONObject.html), you can easily get both a string from an object and an object from a string.  
Example with the class MyData:
```java
class MyData {
    String name;
    int age;
    
    String toString() {
        try {
            JSONObject json = new JSONObject();
            
            json.put("name", name);
            json.put("age", age);
            
            return json.toString();
        } catch(JSONException e) {
            return "";
        }
    }
    
    static MyData createFromString(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        MyData data = new MyData();
        
        data.name = json.getString("name");
        data.age = json.getInt("age");
        
        return data;
    }
}
```
Now, an object of type MyData can be send as a custom message like that:
```java
easyIO.sendCustomMessage(myData.toString())
```
On the other side, the object can be recovered when a custom message is received. In the onCreate() method, define a custom message received listener:
```java
easyIO.setOnCustomMessageReceivedListener(this);
```
And implement the listener's method and make a new MyData object from the string:
```java
@Override
public void onCustomMessageReceived(String customMessage) {
	MyData data = MyData.createFromString(customMessage);
	...
}
```
 * With [base64 encoding and decoding](https://developer.android.com/reference/android/util/Base64.html), you can transform any binary data to a string.
```java
/** From binary to String **/
byte[] binaryData = ...
String encodedBinaryData = Base64.encodeToString(data, Base64.DEFAULT);
easyIO.sendCustomMessage(encodedBinaryData);

/** From String to binary **/
// OnCustomMessageReceivedListener's method
@Override
public void onCustomMessageReceived(String customMessage) {
	byte[] binaryData = Base64.decode(customMessage, Base64.DEAFULT);
	...
}
```
 * It is often preferable to use both the ways at the same time. In fact, it is interresting to add further informations to a binary data. In this case, the encoded string will be one of the attribute of the JSON object.
    
### Ask for a doc!
The documentation still does not exist :(
