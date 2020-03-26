#!/bin/zsh

echo "Script executed from: ${PWD}"

BASEDIR=$(dirname $0)
echo "Script location: ${BASEDIR}"

echo "pulling latest COVID-19 CSSE data from git repo..."
cd /Users/Shared/COVID-19/
git pull
echo "Done git pull"

cd $BASEDIR/
cd ..
echo "currently at : ${PWD}"
/usr/local/bin/mvn clean install exec:java