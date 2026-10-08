@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@IF "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%
@if "%HOME%" == "" (set "HOME=%USERPROFILE%")

@setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
@IF NOT "%MAVEN_PROJECTBASEDIR%" == "" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
set DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

set MAVEN_CMD_LINE_ARGS=-DargLine="-XX:+EnableDynamicAgentLoading" %*

if not defined MAVEN_OPTS set "MAVEN_OPTS=-XX:+EnableDynamicAgentLoading"

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
for %%i in (%JAVA_EXE%) do set JAVA_EXE=%%~$PATH:i
if "%JAVA_EXE%" == "" goto errorNoJavaHome
goto checkWrapperJar

:findJavaFromJavaHome
set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
if exist %JAVA_EXE% goto checkWrapperJar

echo The JAVA_HOME environment variable is not defined correctly >&2
echo This environment variable is needed to run this program >&2
goto error

:checkWrapperJar
if exist "%WRAPPER_JAR%" goto run

echo Extension maven-wrapper.jar not found, downloading...
if exist "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" (
    "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"
)
if exist "%WRAPPER_JAR%" goto run

echo Trying certutil download...
certutil -urlcache -split -f "%DOWNLOAD_URL%" "%WRAPPER_JAR%"
if exist "%WRAPPER_JAR%" goto run

echo Error: Could not find or download "%WRAPPER_JAR%" >&2
goto error

:run
%JAVA_EXE% %MAVEN_OPTS% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:errorNoJavaHome
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. >&2
goto error

:error
exit /b 1

:end
exit /b 0
