#!/bin/bash

cd reann_cond_pairs
mvn exec:java -Dexec.args="$1 $2 $3"
cd ..
