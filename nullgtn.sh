#!/bin/bash

mkdir -p $3
cp -R $2* $3
cd reann_summ
mvn clean compile exec:java -Dexec.args="$1 $2 $3"
cd ..
#cd reann_cond_pairs
#mvn clean compile exec:java -Dexec.args="$1 $2 $3"
#cd ..
