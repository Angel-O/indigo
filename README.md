[![MIT License](https://img.shields.io/github/license/PurpleKingdomGames/indigo?color=indigo)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Findigo%2Ftags)](https://github.com/PurpleKingdomGames/indigo/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.com/channels/716435281208672356) [![Join the chat at https://gitter.im/Purple-Kingdom-Games/Indigo](https://badges.gitter.im/Purple-Kingdom-Games/Indigo.svg)](https://gitter.im/Purple-Kingdom-Games/Indigo?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Indigo

Indigo is a game engine written in Scala for functional programmers. It allows game developers to build games using a set of purely functional APIs that are focused on productivity and testing.

Indigo is built entirely on Scala.js + WebGL, but it's sbt and Mill plugins will export games for web, desktop (via Electron), and mobile (via Cordova). Hypothetically consoles could also be supported.

Documentation can be found on [indigoengine.io](https://indigoengine.io).

## Full local build and test instructions

### Build requirements

You will need:

- Mill
- SBT
- JDK (Update: 1.8 works, 11 is used in Indigo's development)
- [glslang validator](https://github.com/KhronosGroup/glslang) - can be installed with your favorite package manager.

### Running the build

On Mac / Linux, from the repo root to do a full build and test:

```bash
bash build.sh
```

> Windows users: Most of the things in the script mentioned above should work, but Indigo is not routinely built on Windows machines so we currently offer no guarantees or support. We hope to in the future.

There is also another script which is a bit faster since it doesn't build the examples or demos.

```bash
bash localpublish.sh
```

## Software requirements for running games

The list above covers the software needed to build Indigo itself, but to run a game you may also need:

- NPM and/or Yarn
- NodeJS
- Electron
- An http server that will serve static from a directory (suggestions: `http-server` via npm, or Python's `SimpleHTTPServer`)
- A frontend packaging tool such as Parcel.js
