@echo off
echo Testing DNS server...
echo.

REM Test DNS query
nslookup google.com localhost 5354
echo.

echo Test completed.
pause