#!/usr/bin/env bash
set -e

# This is to sync the resources from java to share resource folder.
# This is only for development use.
echo "Warning: This is synchronize resources from a particular backend to the shared resources."
read -p "Do you want to continue? (y or n)  " continue
if [ $continue == yes ] || [ $continue == y ]
then
  read -p "Synchronize from which backend? (only java support now)  " backend
  if [ $backend == java ] #|| [ $backend == php ] || [ $backend == dotnet ] || [ $backend == go ]
  then
    echo
    echo "Syncing from $backend to shared-resources"
    rsync -avh --delete --ignore-errors $backend/src/main/resources/ shared-resources  \
    --exclude application.yml
  else
    echo "Wrong backend, exit"
  fi
else
 echo "Exit"
fi
