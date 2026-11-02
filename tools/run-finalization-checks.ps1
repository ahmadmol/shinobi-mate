param([string]$Jdk=$env:JAVA_HOME,[switch]$SkipBuild,[switch]$Native)
$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$Jdk=& "$PSScriptRoot/resolve-jdk.ps1" -Jdk $Jdk
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output/finalization/classes,output/finalization/logs | Out-Null
 if(-not $SkipBuild){& tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package *> output/finalization/logs/build.log;if($LASTEXITCODE -ne 0){throw 'Maven build failed'}}
 $taskNames=@('PolishEngineCheck','CombatCheck','CombatPolishCheck','CombatVisualCheck','FullCharacterCheck','RepairCombatCheck','ErenCombatCheck','LocomotionCheck','ProfessionalAudioCheck','RuntimeStabilityCheck','FirstRunStopCheck','ReloadConcurrencyCheck','LayeringCheck','FinalMotionCheck','FullCharacterDesktopCheck','RefinementCheck','CharacterCheck','UiRefinementCheck','RefinementUiCheck')
 $taskSources=$taskNames | ForEach-Object {"tools/$_.java"}
 & "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp target/ShinobiMate.jar -d output/finalization/classes @taskSources
 if($LASTEXITCODE -ne 0){throw 'Check compilation failed'}
 foreach($taskCheck in @('CombatCheck','RepairCombatCheck','FullCharacterCheck','ErenCombatCheck','LocomotionCheck','ProfessionalAudioCheck','RuntimeStabilityCheck','FirstRunStopCheck','ReloadConcurrencyCheck','LayeringCheck','RefinementCheck','RefinementUiCheck')){
  $taskProfile=Join-Path $taskRoot "output/finalization/profiles/$taskCheck-$(Get-Date -Format yyyyMMddHHmmssfff)"
  New-Item -ItemType Directory -Force $taskProfile | Out-Null
  & "$Jdk/bin/java.exe" -Xmx512m --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskProfile" -cp 'target/ShinobiMate.jar;output/finalization/classes' $taskCheck *> "output/finalization/logs/$taskCheck.log"
  if($LASTEXITCODE -ne 0){throw "$taskCheck failed; inspect its log"}
  Get-Content "output/finalization/logs/$taskCheck.log" -Tail 1
 }
 & tools/check-product.ps1 -Jdk $Jdk -SkipBuild -Checks CharacterCheck,SettingsCheck,PackCheck,PackImportCheck,ProductionCheck,FocusCheck,ProgressionCheck,MoodCheck,UiRefinementCheck,AudioCheck,SpiritualCloudCheck
 & tools/check-startup-behavior.ps1 -Jdk $Jdk -SkipBuild
 if($Native){
  foreach($taskScale in @('1.0','1.5')){
   $taskProfile=Join-Path $taskRoot "output/finalization/profiles/motion-$taskScale-$(Get-Date -Format yyyyMMddHHmmssfff)"
   New-Item -ItemType Directory -Force $taskProfile | Out-Null
   [IO.File]::WriteAllText("$taskProfile/settings.properties","OnboardingComplete=true`nLanguage=en-GB`nRestorePreviousCharacters=true`nPreviousCharacters=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover/Tom/Jerry/ErenYeager`nMaxCharacters=8`nScaling=$taskScale`nMute=true`nCloudEnabled=false`n")
   & "$Jdk/bin/java.exe" -Xmx512m --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskProfile" -cp 'target/ShinobiMate.jar;output/finalization/classes' FinalMotionCheck *> "output/finalization/logs/FinalMotionCheck-$taskScale.log"
   if($LASTEXITCODE -ne 0){throw "Native motion $taskScale failed"}
  }
 }
}finally{Pop-Location}
