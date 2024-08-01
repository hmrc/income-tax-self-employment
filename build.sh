#!/usr/bin/env bash

sbt -DPLAY_ENV=CI clean scalafmtAll scalafmtSbt compile test:compile it:compile
