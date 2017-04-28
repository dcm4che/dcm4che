@echo off
rem -------------------------------------------------------------------------
rem stowrs client
rem -------------------------------------------------------------------------

if not "%ECHO%" == ""  echo %ECHO%
if "%OS%" == "Windows_NT"  setlocal

set MAIN_CLASS=org.dcm4che3.tool.stowrs.StowRS
set MAIN_JAR=dcm4che-tool-stowrs-${project.version}.jar

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

rem Read all command line arguments

set ARGS=
:loop
if [%1] == [] goto end
        set ARGS=%ARGS% %1
        shift
        goto loop
:end

if not "%DCM4CHE_HOME%" == "" goto HAVE_DCM4CHE_HOME

set DCM4CHE_HOME=%DIRNAME%..

:HAVE_DCM4CHE_HOME

if not "%JAVA_HOME%" == "" goto HAVE_JAVA_HOME

set JAVA=java

goto SKIP_SET_JAVA_HOME

:HAVE_JAVA_HOME

set JAVA=%JAVA_HOME%\bin\java

:SKIP_SET_JAVA_HOME

set CP=%DCM4CHE_HOME%\etc\stowrs\
set CP=%CP%;%DCM4CHE_HOME%\lib\%MAIN_JAR%
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-core-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-net-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-imageio-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-json-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-tool-common-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\slf4j-api-1.7.5.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\slf4j-log4j12-1.7.5.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\log4j-1.2.17.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\commons-cli-1.2.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\httpclient-4.3.3.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\httpmime-4.3.3.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\httpcore-4.3.2.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\commons-logging-1.1.1.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\javax.json-1.0.4.jar

"%JAVA%" %JAVA_OPTS% -cp "%CP%" %MAIN_CLASS% %ARGS%
