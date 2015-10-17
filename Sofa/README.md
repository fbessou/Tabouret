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
* In your AndroidManifest.xml, add the permissions:
```xml
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
```
and also the permission to use the vibrate if you want to use game-pad's feedback later:
```xml
    <uses-permission android:name="android.permission.VIBRATE"/>
```


### Create a game-pad activity
 * Create a simple activity (do not forget to declare it into your manifest)
 * Create a layout which contains the buttons and the views that you want to display on your game-pad:
 * Define game-pad's informations:
```java
    GamePadInformation info = new GamePadInformation("Best game-pad ever");
```
Note: The constructor `GamePadInformation(String name)` is deprecated. You should use `GamePadInformation(String name, UUID uuid)` instead. The UUID must ideally stay the same each time the app is launched. Consequently, the UUID should be generated during the first launch and it should be immediately stored in the SharedPreferences in order to be reused for future launches. The first constructor is equivalent to `GamePadInformation(name, UUID.randomUUID())`.
 * Instatiate a GamePadIOHelper object in the `onCreate()` of the main activity:
```java
    GamePadIOHelper easyIO = new GamePadIOHelper(getContext(), info);
```
 * Start the game-pad IO client:
```java
    // The start method takes a listener in parameter, it can be defined
    // to be notified for each change of the state of the connection to
    // the game. It can be set to null if you do not need it
	easyIO.start(this);
```
 * Attach the Android views to the GamePadIOHelper:
Define an input view:
```java
    // Get the android view
    ...
    // Create a sensor
    ...
    // Attach the sensor to the view. All the interactions with the
    // view will be automatically transmit to the game.
    ...
```
Define an output view:
```java
    // Get the android view
    ...
    // Create an indicator
    ...
    // Attach the indicator to the view. All the output event generated
    // by the game will be automatically diplayed in this view.
    ...
```

### Integrate Sofa in your game
 * Define the game's informations (currently you can only define the name of the game):
```java
    GameInformation info = new GameInformation("The Game!");
```
 * Instantiate a GameIOHelper object in the `onCreate()` of your main activity:
```java
    GameIOHelper easyIO = new GameIOHelper(getContext(), info);
```
 * Start the game client
  * Using the listener's methods (To handle the game-pad events in the GUI Thread):
  * Using the pollEvent methods (To handle the game-pad events from any thread you want):
 * Handle the game-pad events:
  * With the listeners:
  * With the pollEvent methods:

### See sample activities
You can check the sample activities in src/com/fbessou/sofa/activity/:
* An example of game-pad activity: GamePadSampleActivity.java with its layout res/layout/activity_gamepad_sample.xml.
* An example of a game (altough it is not really a game) activity using the listener's methods: GameListerSampleActivity.java

### Other tips
#### Create your own game-pad indicator

#### Send a personalized input event (game-pad)
#### Create and send a personalized output event (game)
#### Using custom message
 * With JSON objects
 * With base64 encoding and decoding (See [Base64 in android developers](https://developer.android.com/reference/android/util/Base64.html))

### Ask for a doc!
The documentation still does not exist :(
