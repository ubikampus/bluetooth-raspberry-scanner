### Android scanner (Minimum Viable Product)
Notable points and flaws that can be fixed in future:
* Requires at least Android API 23
* Doesn't use ubiMqtt (uses eclipse paho)
* Handles all BLE signals the same way (doesn't differ beacons)
* Doesn't work with regular bluetooth signals
* Has a delay (1.5 - 2.5s) between scanning waves
* Was tested only with one device (Galaxy S9) 
