#!/bin/bash

./nullgtn.sh /home/ubuntu/environment/nullgtn-artifact/ /home/ubuntu/environment/nullgtn-artifact/minstripped/uLeak/ /home/ubuntu/environment/nullgtn-artifact/out/uLeak/
diff -r /home/ubuntu/environment/nullgtn-artifact/fmted_originals/uLeak/ /home/ubuntu/environment/nullgtn-artifact/out/uLeak/
