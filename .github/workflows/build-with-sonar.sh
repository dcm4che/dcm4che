#!/bin/bash

set -e

./mvnw -B -P test-coverage verify

if [ -z "$SONAR_TOKEN" ]
then
  echo "Cannot run sonar analysis as there is no SONAR_TOKEN. \
  This happens for external pull requests (from forks). See https://jira.sonarsource.com/browse/MMF-1371"
else
  ./mvnw -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.coverage.jacoco.xmlReportPaths=$(find "$(pwd)" -path '*/target/site/*/jacoco.xml' | tr '\n' ',')
fi
