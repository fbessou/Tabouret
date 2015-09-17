# Sofa
*Android library that establishes and manages communication between an Android game and several game-pads (on other Android devices) using [wifi peer to peer](https://developer.android.com/guide/topics/connectivity/wifip2p.html).*
**Require Android API level 14 (4.0) minimum**

## Description

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
* In your project, add Sofa to depedencies
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
 * Create a layout wich contains the buttons and the other views that you want to have in your game-pad:
 * Instatiate a GamePadIOHelper object:
 * Define game-pad's informations:
 * Start the game-pad client:
 * Attach the Android views to the GamePadIOHelper:

### Use Sofa in your game
 * Instantiate a GameIOHelper object:
 * Define game's informations:
 * Start the game client
  * Using the listener's methods (To handle the game-pad events in the GUI Thread):
  * Using the pollEvent methods (To handle the game-pad events from any thread you want):
 * Handle the game-pad events:
  * With the listeners:
  * With the pollEvent methods:

### See sample activities
You can check the sample activities in src/com/fbessou/sofa/activity/:
* An example of game-pad activity: GamePadSampleActivity.java with its layout res/layout/activity_gamepad_sample.xml.
* An example of a game (altough it is not really a game) activity using 

### Ask for a doc!
The documentation still does not exist :(
