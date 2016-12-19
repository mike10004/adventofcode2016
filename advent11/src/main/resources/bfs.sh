#!/bin/bash

MAVEN_OPTS=-Xmx2048m
mvn exec:java -Dexec.mainClass=aoc2016day11.BreadthFirstAgent
