# Rythmus Jumper - Open Beta

Rythmus Jumper is a small Java rhythm/platformer game with account login, cloud progress, leaderboards, and replay recording.

This repository contains the public open beta build. The admin panel is not included in this public version.

## Download

Download `Rythmus-Reddit-Open-Beta.zip`, extract it, and run:

```bat
Start-Rythmus-Open-Beta.bat
```

## Requirements

- Java 8 or newer
- Internet connection for login, leaderboard, cloud progress, and replay upload

## Beta Notes

- The game starts in English by default.
- Replays are saved locally in the `replays` folder.
- If logged in, replays and progress are also synced to Firebase.
- This is an open beta, so bugs are expected.

## Reporting Bugs

Please include:

- Your account name
- Level number
- Approximate time
- What happened
- If possible, the replay file from the `replays` folder

## Build

Compile with Java 8 target:

```bat
javac --release 8 -g:none -encoding UTF-8 -d build/classes src/*.java
jar --create --file build/Rythmus-Open-Beta.jar --main-class launcher -C build/classes .
```

## Security Note

This public build removes debug symbols and avoids simple plaintext backend strings in class-file inspection, but any client-side game can still be reverse engineered. Backend rules are the real protection.
