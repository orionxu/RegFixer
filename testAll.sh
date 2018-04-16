#!/bin/bash

rm -rf testAllResult.log
javac TestAll.java
java TestAll
rm -rf TestAll.class
