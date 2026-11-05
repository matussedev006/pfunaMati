@echo off
setlocal
set "MAVEN_PROJECTBASEDIR=%~dp0."
set "MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
if not exist "%MAVEN_WRAPPER_JAR%" (
    echo Maven Wrapper JAR not found: %MAVEN_WRAPPER_JAR%
    exit /b 1
)
java -classpath "%MAVEN_WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
