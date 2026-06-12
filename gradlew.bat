@echo off
setlocal

set "GRADLE_DIST=C:\Users\16333\.gradle\wrapper\dists\gradle-8.7-bin\bhs2wmbdwecv87pi65oeuq5iu\gradle-8.7\bin\gradle.bat"

if not exist "%GRADLE_DIST%" (
  echo ERROR: Local Gradle 8.7 distribution was not found at:
  echo %GRADLE_DIST%
  exit /b 1
)

call "%GRADLE_DIST%" %*
exit /b %ERRORLEVEL%
