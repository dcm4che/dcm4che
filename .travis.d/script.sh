#!/bin/bash
set -ev

# Only release from master if
# A) we know of no commits since this build's commit and
# B) this build is not a pull request and
# C) secure variables are available
echo "RELEASE_CANDIDATE = '${RELEASE_CANDIDATE}'"

if [[ ${RELEASE_CANDIDATE} == "true" ]]
then
  echo "The current commit is the latest release candidate: attempt to release."
  
  # Make credentials available
  openssl aes-256-cbc -K $encrypted_1fc90f464345_key -iv $encrypted_1fc90f464345_iv \
    -in .travis.d/secret.tar.enc -out .travis.d/secret.tar -d
  tar xf .travis.d/secret.tar -C .travis.d

  # Generate the build number; the TRAVIS_BUILD_NUMBER will suffice most
  # of the time but will not be unique in the case of rebuilds
  BUILD_VERSION="3.3.5-$(printf '%04d' ${TRAVIS_BUILD_NUMBER})$(date -u '+%Y%m%d%H%M%S')"

  # Stage the release, setting the version number and the commit hash
  mvn -s .travis.d/settings.xml versions:set -DnewVersion="${BUILD_VERSION}"
  mvn -s .travis.d/settings.xml -P ossrh-up,travis-secret deploy -Dscm.revision="${TRAVIS_COMMIT}"
else
  echo "The current commit is not a release candidate: attempt to verify."

  mvn verify
fi
