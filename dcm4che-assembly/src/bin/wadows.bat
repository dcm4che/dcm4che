@echo off
rem -------------------------------------------------------------------------
rem wadows  Launcher
rem -------------------------------------------------------------------------

if not "%ECHO%" == ""  echo %ECHO%
if "%OS%" == "Windows_NT"  setlocal

set MAIN_CLASS=org.dcm4che3.tool.wadows.WadoWS
set MAIN_JAR=dcm4che-tool-wadows-${project.version}.jar

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

set CP=%DCM4CHE_HOME%\etc\wadows\
set CP=%CP%;%DCM4CHE_HOME%\lib\%MAIN_JAR%
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-core-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-mime-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-tool-common-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\dcm4che-xdsi-${project.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\slf4j-api-${slf4j.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\logback-core-${logback.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\logback-classic-${logback.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\commons-cli-${commons-cli.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\jakarta.activation-api-${jakarta.activation.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\angus-activation-${eclipse.angus.activation.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\jakarta.xml.bind-api-${jakarta.xml.bind.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\jaxb-runtime-${jaxb-runtime.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\jaxb-core-${jaxb-runtime.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\jakarta.xml.ws-api-${jakarta.xml.ws.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\rt-${jakarta.xml.ws.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\jakarta.xml.soap-api-${jakarta.xml.soap-api.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\istack-commons-runtime-${com.sun.istack.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\saaj-impl-${com.sun.xml.messaging.saaj.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\streambuffer-${com.sun.xml.stream.buffer.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\policy-${com.sun.xml.ws.policy.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\gmbal-api-only-${org.glassfish.gmbal.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\mimepull-${org.jvnet.mimepull.version}.jar
set CP=%CP%;%DCM4CHE_HOME%\lib\stax-ex-${org.jvnet.staxex.version}.jar

"%JAVA%" %JAVA_OPTS% -cp "%CP%" %MAIN_CLASS% %ARGS%
