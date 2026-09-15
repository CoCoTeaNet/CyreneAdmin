@echo off
cd /d D:\CodeLife\CyreneAdmin\e2e
set JAVA_OPTS=-Xmx512m -Dsolon.profiles.active=test --myapp.db1.password=test123456
"D:\Application\Environment\graalvm-jdk-21\bin\java.exe" -jar "D:\CodeLife\CyreneAdmin\cyrene-starter-solon\target\launcher.jar"
exit /b %ERRORLEVEL%
