#!/usr/bin/env bash

sbt -DPLAY_ENV=CI clean compile test:compile it:compile
