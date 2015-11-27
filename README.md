# BotoneraBluetooth
Pair with your friend's phone and play sounds on HIS phone.
For android only.

# Building

Compile it with <code>./gradlew build</code>.
Install it with <code>adb install build/outputs/apk/BotoneraBluetooth-release-unsigned.apk</code>

# Usage

## Pair both android devices
1. Open your bluetooth settings
1. Turn on your bluetooth 
1. Allow your phone to be discovered by tapping 'Only visible to paired devices'. This will start a 2 minute countdown (approximately)
1. Open friend's phone bluetooth settings
1. Search for devices in your friend's phone
1. When you see your own phone select it
1. Both phones will open a popup dialog, press OK on BOTH phones

This procedure needs to be done only once for each pair of devices. Repeat to add another friend.

## Open app
1. On your phone press 'server'
1. On your friend's phone press 'client'. A list of known devices will be displayed. Select your own phone.

Tap the 'iiiih' button as many times as you want, you should hear a sound on both YOUR device as well as in your friends' device.

To start a new connection simply close the app and open it again.
