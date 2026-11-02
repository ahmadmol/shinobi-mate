param([string]$Jdk=$env:JAVA_HOME,[switch]$Desktop)
$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output/eren/classes | Out-Null
 & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package > output/eren-build.log 2>&1
 if($LASTEXITCODE -ne 0){throw 'Maven failed: output/eren-build.log'}
 & "$Jdk/bin/javac.exe" -encoding UTF-8 -cp target/ShinobiMate.jar -d output/eren/classes tools/PolishEngineCheck.java tools/CombatCheck.java tools/ErenCombatCheck.java tools/ErenDesktopCheck.java
 if($LASTEXITCODE -ne 0){throw 'Eren check compilation failed'}
 foreach($taskCheck in @('ErenCombatCheck','CombatCheck')) {
  $taskLog=if($taskCheck -eq 'ErenCombatCheck'){'check'}else{'regression'}
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED -cp 'target/ShinobiMate.jar;output/eren/classes' $taskCheck > "output/eren/$taskLog.log" 2>&1
  if($LASTEXITCODE -ne 0){throw "$taskCheck failed"}
  Get-Content "output/eren/$taskLog.log" -Tail 1
 }
 if($Desktop){
  New-Item -ItemType Directory -Force output/eren/native-profile | Out-Null
  @'
RestorePreviousCharacters=true
PreviousCharacters=ErenYeager/NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover
AlwaysShowShimejiChooser=false
OnboardingComplete=true
Language=en
Scaling=1.0
SoundEnabled=false
CombatSound=false
AlwaysOnTop=true
'@ | Set-Content output/eren/native-profile/settings.properties
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskRoot/output/eren/native-profile" "-Deren.output=$taskRoot/output/eren" -cp 'target/ShinobiMate.jar;output/eren/classes' ErenDesktopCheck > output/eren/native.log 2>&1
  if($LASTEXITCODE -ne 0){throw 'Native Eren check failed'}
  Select-String output/eren/native.log -Pattern 'PASS native'
 }
} finally {Pop-Location}
