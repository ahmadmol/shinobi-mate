param([string]$Jdk=$env:JAVA_HOME)
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=Split-Path $PSScriptRoot -Parent
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output/ninja-polish/classes | Out-Null
 & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package > output/ninja-polish/build.log 2>&1
 if($LASTEXITCODE -ne 0){throw 'Maven failed'}
 & "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp target/ShinobiMate.jar -d output/ninja-polish/classes tools/PolishEngineCheck.java tools/CombatCheck.java tools/CombatVisualCheck.java tools/CombatPolishCheck.java tools/NinjaPolishCheck.java tools/AudioCheck.java tools/CombatDesktopCheck.java tools/CombatPolishDesktopCheck.java tools/NinjaNativeMoveCheck.java
 if($LASTEXITCODE -ne 0){throw 'Check compilation failed'}
 foreach($taskCheck in @('NinjaPolishCheck','CombatCheck','CombatVisualCheck','CombatPolishCheck','AudioCheck')){
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskRoot/output/ninja-polish/profiles/$taskCheck" -cp 'target/ShinobiMate.jar;output/ninja-polish/classes' $taskCheck > "output/ninja-polish/$taskCheck.log" 2>&1
  if($LASTEXITCODE -ne 0){throw "$taskCheck failed"}
  Get-Content "output/ninja-polish/$taskCheck.log" -Tail 2
 }
}finally{Pop-Location}
