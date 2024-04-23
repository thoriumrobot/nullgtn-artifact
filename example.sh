#!/bin/bash

./nullgtn.sh /home/ubuntu/environment/tolerances/nullgtn-artifact/ /home/ubuntu/environment/tolerances/nullgtn-artifact/fmted_originals/ /home/ubuntu/environment/tolerances/nullgtn-artifact/out/
diff -r /home/ubuntu/environment/tolerances/nullgtn-artifact/fmted_originals/ /home/ubuntu/environment/tolerances/nullgtn-artifact/out/
