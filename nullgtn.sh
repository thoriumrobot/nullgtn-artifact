#!/bin/bash

mkdir $3
cd reann_summ
mvn exec:java -Dexec.args="$1 $2 $3"
cd ..
cd reann_cond_pairs
mvn exec:java -Dexec.args="$1 $2 $3"
cd ..
