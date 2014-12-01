#!/bin/bash
set -ev

if [[ ${TRAVIS_BRANCH} -eq "master" && ${TRAVIS_PULL_REQUEST} -eq "false" ]]
then
  echo "update dependencies"
fi
