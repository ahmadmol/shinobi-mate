param([string]$Jdk='C:/Program Files/Android/Android Studio/jbr')
$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskOut=Join-Path $taskRoot 'output/repair-20261015'
$taskPortable=Join-Path $taskOut 'portable/ShinobiMate-Test'
$taskFfmpeg='C:/Users/molha/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/Lib/site-packages/imageio_ffmpeg/binaries/ffmpeg-win-x86_64-v7.1.exe'
Copy-Item "$taskRoot/target/ShinobiMate.jar" "$taskPortable/app/ShinobiMate.jar" -Force
Copy-Item "$taskRoot/target/ShinobiMate.jar" "$taskRoot/Shimeji-ee/app/Shimeji-ee.jar" -Force
foreach($taskPack in @('ErenYeager','Tom','Jerry')){
 Copy-Item -Path "$taskRoot/img/$taskPack/*" -Destination "$taskPortable/img/$taskPack" -Recurse -Force
 Copy-Item -Path "$taskRoot/img/$taskPack/*" -Destination "$taskRoot/Shimeji-ee/img/$taskPack" -Recurse -Force
}
foreach($taskCase in @(
 @{Name='motion-final-100';Scale='1.0';Characters='ErenYeager';Class='RepairMotionDesktopCheck';Cartoon='false'},
 @{Name='motion-final-150';Scale='1.5';Characters='ErenYeager';Class='RepairMotionDesktopCheck';Cartoon='false'},
 @{Name='cartoon-final-100';Scale='1.0';Characters='Tom/Jerry';Class='RepairDesktopCheck';Cartoon='true'},
 @{Name='eren-final-150';Scale='1.5';Characters='ErenYeager/NarutoShippuden/SasukeShippuden/Tom';Class='RepairDesktopCheck';Cartoon='false'}
)){
 $taskDir=Join-Path $taskOut $taskCase.Name
 New-Item -ItemType Directory -Force "$taskDir/profile" | Out-Null
 [IO.File]::WriteAllText("$taskDir/profile/settings.properties",@"
RestorePreviousCharacters=true
PreviousCharacters=$($taskCase.Characters)
AlwaysShowShimejiChooser=false
OnboardingComplete=true
Language=en
Scaling=$($taskCase.Scale)
AlwaysOnTop=true
Sounds=true
Mute=false
MasterVolume=25
CombatVolume=50
CloudEnabled=false
"@)
 & "$Jdk/bin/java.exe" -Xmx512M -Xms64M --enable-native-access=ALL-UNNAMED "-Dapp.dir=$taskPortable" "-Dapp.userDataDir=$taskDir/profile" "-Drepair.output=$taskDir" "-Drepair.ffmpeg=$taskFfmpeg" "-Drepair.cartoon=$($taskCase.Cartoon)" -cp "$taskPortable/app/ShinobiMate.jar;$taskOut/classes" $taskCase.Class > "$taskDir/run.log" 2>&1
 if($LASTEXITCODE -ne 0){throw "Failed $($taskCase.Name), see $taskDir/run.log"}
 Select-String "$taskDir/run.log" -Pattern '^PASS'
}
