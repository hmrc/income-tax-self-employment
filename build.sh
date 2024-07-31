#!/usr/bin/env bash

export PLAY_ENV=CI

sbt clean scalafmtAll scalafmtSbt compile test:compile it:compile
