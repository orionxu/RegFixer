#!/bin/bash

rm -f testAllResult.log
javac TestAll.java
java TestAll
rm -f TestAll.class
rm -f TestAll$TestCase.class