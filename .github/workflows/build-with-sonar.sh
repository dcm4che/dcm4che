#!/bin/bash

set -e

mvn -B -P test-coverage verify
mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.coverage.jacoco.xmlReportPaths=$(find "$(pwd)" -path '*jacoco.xml' | tr '\n' ',')
