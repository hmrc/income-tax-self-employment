#!/usr/bin/env bash

sbt -DPLAY_ENV=CI clean scalafmt scalafmtAll test:scalafmtAll it:scalafmtAll scalafmtSbt compile test:compile it:compile
