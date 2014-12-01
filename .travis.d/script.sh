#!/bin/bash
set -ev

doTest() {
  echo "The current commit is NOT a release candidate: attempt to test."
  mvn verify
}

# Only release from master if
# A) we know of no commits since this build's commit and
# B) this build is not a pull request and
# C) secure variables are available
if [[ ${TRAVIS_BRANCH} == "master" && \
  ${TRAVIS_PULL_REQUEST} == "false" && \
  ${TRAVISE_SECURE_ENV_VARS} == "true" ]]
then
    MASTER_COMMIT=$(git ls-remote origin master | cut -f 1)

    if [[ ${TRAVIS_COMMIT} == ${MASTER_COMMIT} ]]
    then
      echo "The current commit is a release candidate: attempt to release."
  
      # Make credentials available
      openssl aes-256-cbc -K $encrypted_1fc90f464345_key -iv $encrypted_1fc90f464345_iv \
        -in .travis.d/secret.tar.enc -out .travis.d/secret.tar -d
      tar xf .travis.d/secret.tar -C .travis.d

      # Generate the build number; the TRAVIS_BUILD_NUMBER will suffice most
      # of the time but will not be unique in the case of rebuilds
      BUILD_VERSION="3.3.5-$(printf '%04d' ${TRAVIS_BUILD_NUMBER})$(date -u '+%Y%m%d%H%M%S')"

      # Run the release
      mvn -s .travis.d/settings.xml versions:set -DnewVersion='${BUILD_VERSION}'
      mvn -s .travis.d/settings.xml -P ossrh,travis-secret deploy
    else
      doTest
    fi
else
  doTest
fi
