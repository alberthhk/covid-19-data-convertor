#!/bin/bash

echo "Script executed from: ${PWD}"

BASEDIR=$(pwd)/$(dirname $0)
echo "Script location: ${BASEDIR}"

echo "pulling latest COVID-19 CSSE data from git repo..."
cd /Users/Shared/COVID-19/
git pull
echo "Done git pull"

cd $BASEDIR/..
mvn exec:java