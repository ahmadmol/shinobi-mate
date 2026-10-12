param([string]$Jdk=$env:JAVA_HOME)
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskClasses=Join-Path $taskRoot 'output/image-cache-check'
New-Item -ItemType Directory -Force $taskClasses | Out-Null
& "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp "$taskRoot/target/ShinobiMate.jar" -d $taskClasses "$PSScriptRoot/ImageCacheCheck.java"
if($LASTEXITCODE -ne 0){throw 'Image cache check compilation failed'}
& "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED -Xmx256M "-Dapp.dir=$taskRoot" "-Dapp.userDataDir=$taskClasses/profile" -cp "$taskRoot/target/ShinobiMate.jar;$taskClasses" ImageCacheCheck
if($LASTEXITCODE -ne 0){throw 'Image cache stress failed'}
