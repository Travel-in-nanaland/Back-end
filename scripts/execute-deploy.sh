#!/bin/bash
DEPLOY_SH=/home/ubuntu/Back-end/scripts/deploy.sh
DEPLOY_LOG=/home/ubuntu/Back-end/deploy.log
DEPLOY_ERROR_LOG=/home/ubuntu/Back-end/deploy-error.log

sh $DEPLOY_SH > DEPLOY_LOG 2> DEPLOY_ERROR_LOG &