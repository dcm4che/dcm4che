#!/bin/bash
set -ev

MASTER_COMMIT=$(git rev-parse master)

# Only release from master if
# A) we know of no commits since this build's commit and
# B) this build is not a pull request.
if [[ ${TRAVIS_BRANCH} == "master" && \
  ${TRAVIS_COMMIT} == ${MASTER_COMMIT} && \
  ${TRAVIS_PULL_REQUEST} == "false" ]]
then
  echo "The current commit is a release candidate: attempt to release."
  
  # Make credentials available
  tar xf .travis.d/secret.tar -C .travis.d

  # Setup git clone for committing release POMs
  export GIT_SSH=${TRAVIS_BUILD_DIR}/.travis.d/ssh.sh
  git config --global user.name "Christopher Redekop"
  git config --global user.email "chris.redekop@gmail.com"
  git checkout master

  # Run the release
  mvn -s .travis.d/settings.xml -B release:prepare
  mvn -s .travis.d/settings.xml -P travis-release-perform release:perform 
else
  echo "The current commit is NOT a release candidate: attempt to  test."
  mvn verify
fi
