#!/usr/bin/env bash

sbt -DPLAY_ENV=CI clean scalastyle coverage test it/test coverageReport
