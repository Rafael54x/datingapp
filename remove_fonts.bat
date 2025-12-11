@echo off
for %%f in ("c:\Users\rafae\datingapp\app\src\main\res\layout\*.xml") do (
    powershell -Command "(Get-Content '%%f') -replace 'android:fontFamily=\"@font/poppins_[^\"]*\"\s*', '' | Set-Content '%%f'"
)
echo Font references removed from all layout files