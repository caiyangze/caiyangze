@echo off
cd lovelink-social\src\main\java
javac -cp ".;..\..\..\..\target\classes\*" com\zhentao\service\impl\LocationServiceImpl.java
if %errorlevel% equ 0 (
    echo Compilation successful!
) else (
    echo Compilation failed with errors.
)
pause
