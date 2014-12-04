#!/bin/bash

# Only release from master if
# A) we know of no commits since this build's commit and
# B) this build is not a pull request and
# C) secure variables are available
if [[ ${TRAVIS_BRANCH} == "master" && \
  ${TRAVIS_PULL_REQUEST} == "false" && \
  ${TRAVIS_SECURE_ENV_VARS} == "true" ]]
then
    MASTER_COMMIT=$(git ls-remote origin master | cut -f 1)

    if [[ ${TRAVIS_COMMIT} == ${MASTER_COMMIT} ]]
    then
      echo true
    else
      echo false
    fi
else
  echo false
fi
