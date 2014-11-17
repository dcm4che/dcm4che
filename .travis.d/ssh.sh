#!/bin/bash

ssh -i ${TRAVIS_BUILD_DIR}/.travis.d/id_rsa "$@"
