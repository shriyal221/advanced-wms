@echo off
setlocal

set "ROOT=%~dp0.."
set "JAVA_HOME=%ROOT%\.tools\jdk-21.0.11+10"
set "MAVEN_HOME=%ROOT%\.tools\apache-maven-3.9.9"
set "MAVEN_REPO=%ROOT%\.m2\repository"
set "SERVER_PORT=8081"

cd /d "%ROOT%\backend"
"%MAVEN_HOME%\bin\mvn.cmd" -Dmaven.repo.local="%MAVEN_REPO%" spring-boot:run -Dspring-boot.run.profiles=local
