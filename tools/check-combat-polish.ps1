param([string]$Jdk=$env:JAVA_HOME,[switch]$Native)
$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output/combat-polish/classes | Out-Null
 & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package > output/combat-polish-build.log 2>&1
 if($LASTEXITCODE -ne 0){throw 'Maven failed; see output/combat-polish-build.log'}
 & "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp target/ShinobiMate.jar -d output/combat-polish/classes tools/PolishEngineCheck.java tools/CombatCheck.java tools/CombatVisualCheck.java tools/CombatPolishCheck.java tools/AudioCheck.java tools/CombatDesktopCheck.java tools/CombatPolishDesktopCheck.java tools/SettingsCheck.java tools/RuntimeStabilityCheck.java
 if($LASTEXITCODE -ne 0){throw 'Check compilation failed'}
 foreach($taskCheck in @('CombatCheck','CombatVisualCheck','CombatPolishCheck','AudioCheck')) {
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED -cp 'target/ShinobiMate.jar;output/combat-polish/classes' $taskCheck > "output/combat-polish/$taskCheck.log" 2>&1
  if($LASTEXITCODE -ne 0){throw "$taskCheck failed"}
  Get-Content "output/combat-polish/$taskCheck.log" -Tail 2
 }
 if($Native) {
  $taskArea=Join-Path $taskRoot 'output/combat-polish/native'
  New-Item -ItemType Directory -Force "$taskArea/profile","$taskArea/conf" | Out-Null
  Copy-Item -LiteralPath conf -Destination $taskArea -Recurse -Force
  if(-not(Test-Path "$taskArea/img")){New-Item -ItemType Junction -Path "$taskArea/img" -Target (Join-Path $taskRoot 'img')|Out-Null}
  [IO.File]::WriteAllText("$taskArea/profile/settings.properties","ActiveShimeji=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nRestorePreviousCharacters=true`nPreviousCharacters=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nAlwaysShowShimejiChooser=false`nOnboardingComplete=true`nLanguage=ar`nMenuDPI=96`nSounds=true`nMute=false`nMasterVolume=30`nCombatVolume=60`nSFXVolume=70`nCloudEnabled=false`n",[Text.UTF8Encoding]::new($false))
  Push-Location $taskArea
  try {
   & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.dir=$taskArea" "-Dapp.userDataDir=$taskArea/profile" -cp "$taskRoot/target/ShinobiMate.jar;$taskRoot/output/combat-polish/classes" CombatPolishDesktopCheck > ../native-check.log 2>&1
   if($LASTEXITCODE -ne 0){throw 'Native combat polish check failed'}
  }finally{Pop-Location}
 }
}finally{Pop-Location}
