param([string]$Jdk=$env:JAVA_HOME,[switch]$Native,[switch]$SkipBuild,[switch]$Visual,[switch]$Desktop)
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output/audio-phase1/classes | Out-Null
 if(-not $SkipBuild){
  & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package *> output/audio-phase1/build.log
  if($LASTEXITCODE -ne 0){throw 'Maven failed; see output/audio-phase1/build.log'}
 }
 & "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp target/ShinobiMate.jar -d output/audio-phase1/classes tools/PolishEngineCheck.java tools/CombatCheck.java tools/CombatVisualCheck.java tools/CombatPolishCheck.java tools/ProfessionalAudioCheck.java tools/AudioCheck.java tools/NinjaPolishCheck.java tools/AudioVisualQA.java tools/AudioDesktopPerformanceCheck.java
 if($LASTEXITCODE -ne 0){throw 'Audio check compilation failed'}
 foreach($taskCheck in @('ProfessionalAudioCheck','AudioCheck','CombatCheck','NinjaPolishCheck')){
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskRoot/output/audio-phase1/profiles/$taskCheck" -cp 'target/ShinobiMate.jar;output/audio-phase1/classes' $taskCheck *> "output/audio-phase1/$taskCheck.log"
  if($LASTEXITCODE -ne 0){throw "$taskCheck failed"}
  Get-Content "output/audio-phase1/$taskCheck.log" -Tail 2
 }
 if($Native){
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskRoot/output/audio-phase1/profiles/native" -cp 'target/ShinobiMate.jar;output/audio-phase1/classes' ProfessionalAudioCheck native *> output/audio-phase1/native-check.log
  if($LASTEXITCODE -ne 0){throw 'Native audio test failed'}
  Get-Content output/audio-phase1/native-check.log -Tail 2
 }
 if($Visual){
  $taskProfile=Join-Path $taskRoot 'output/audio-phase1/profile-visual'
  New-Item -ItemType Directory -Force $taskProfile | Out-Null
  [IO.File]::WriteAllText("$taskProfile/settings.properties","OnboardingComplete=true`nLanguage=en-GB`nActiveShimeji=NarutoShippuden/SasukeShippuden`nAlwaysShowShimejiChooser=false`nAlwaysShowInformationScreen=false`nEnvironment=generic`nCloudEnabled=false`nSounds=true`nMute=false`n",[Text.UTF8Encoding]::new($false))
  Start-Process -FilePath "$Jdk/bin/java.exe" -ArgumentList @('--enable-native-access=ALL-UNNAMED',"-Dapp.userDataDir=$taskProfile",'-cp','target/ShinobiMate.jar;output/audio-phase1/classes','AudioVisualQA') -WorkingDirectory $taskRoot -WindowStyle Hidden
 }
 if($Desktop){
  $taskProfile=Join-Path $taskRoot 'output/audio-phase1/profile-desktop'
  New-Item -ItemType Directory -Force $taskProfile | Out-Null
  [IO.File]::WriteAllText("$taskProfile/settings.properties","OnboardingComplete=true`nLanguage=en-GB`nActiveShimeji=NarutoShippuden`nAlwaysShowShimejiChooser=false`nAlwaysShowInformationScreen=false`nEnvironment=generic`nCloudEnabled=false`nSounds=true`nMute=false`n",[Text.UTF8Encoding]::new($false))
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskProfile" -cp 'target/ShinobiMate.jar;output/audio-phase1/classes' AudioDesktopPerformanceCheck *> output/audio-phase1/desktop-performance.log
  if($LASTEXITCODE -ne 0){throw 'Actual desktop audio performance test failed'}
  Get-Content output/audio-phase1/desktop-performance.log -Tail 1
 }
}finally{Pop-Location}
