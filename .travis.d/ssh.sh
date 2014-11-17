#!/bin/bash

echo "*** TESTING SSH TO GITHUB ***" 1>& 2
ssh -T -i ${TRAVIS_BUILD_DIR}/.travis.d/id_rsa git@ssh.github.com 1 >& 2

echo "*** CALLING SSH ON BEHALF OF GIT ***" 1>& 2
echo "ssh -i ${TRAVIS_BUILD_DIR}/.travis.d/id_rsa $@" 1 >& 2

ssh -i ${TRAVIS_BUILD_DIR}/.travis.d/id_rsa "$@"
