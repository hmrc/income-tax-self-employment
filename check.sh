#!/usr/bin/env bash

export PLAY_ENV=CI

sbt clean scalastyle coverage test it:test coverageReport
