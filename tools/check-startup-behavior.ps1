param([string]$Jdk=$env:JAVA_HOME,[switch]$SkipBuild)
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output/startup-fix/classes | Out-Null
 if(-not $SkipBuild){
  & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package *> output/startup-fix/build.log
  if($LASTEXITCODE -ne 0){throw 'Startup build failed'}
 }
 & "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp target/ShinobiMate.jar -d output/startup-fix/classes tools/StartupBehaviorCheck.java
 if($LASTEXITCODE -ne 0){throw 'Startup check compilation failed'}
 $taskMatrix="output/startup-fix/profiles/$(Get-Date -Format 'yyyyMMdd-HHmmss-fff')"
 New-Item -ItemType Directory -Force $taskMatrix | Out-Null
 Set-Content output/startup-fix/current-matrix.txt $taskMatrix
 foreach($taskLanguage in @('en-GB','ar')) {
  foreach($taskMode in @('first','off','on','off-again')) {
   & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskMatrix/$taskLanguage" -cp 'target/ShinobiMate.jar;output/startup-fix/classes' StartupBehaviorCheck $taskMode $taskLanguage
   $taskCode=$LASTEXITCODE
   Get-Content "$taskMatrix/$taskLanguage/$taskMode-result.log" -Tail 1
   if($taskCode -ne 0){throw "$taskLanguage/$taskMode failed"}
  }
 }
 foreach($taskMode in @('legacy','duplicates','missing')) {
  New-Item -ItemType Directory -Force "$taskMatrix/$taskMode" | Out-Null
  $taskSettings="OnboardingComplete=true`nLanguage=ar`nActiveShimeji=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nAlwaysShowShimejiChooser=true`nCloudEnabled=false`nMute=true`n"
  if($taskMode -eq 'duplicates'){$taskSettings+="RestorePreviousCharacters=true`nPreviousCharacters=NarutoShippuden/NarutoShippuden/SasukeShippuden`n"}
  if($taskMode -eq 'missing'){$taskSettings+="RestorePreviousCharacters=true`nPreviousCharacters=RemovedPack`n"}
  [IO.File]::WriteAllText("$taskMatrix/$taskMode/settings.properties",$taskSettings,[Text.UTF8Encoding]::new($false))
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskMatrix/$taskMode" -cp 'target/ShinobiMate.jar;output/startup-fix/classes' StartupBehaviorCheck $taskMode ar
  $taskCode=$LASTEXITCODE
  Get-Content "$taskMatrix/$taskMode/$taskMode-result.log" -Tail 1
  if($taskCode -ne 0){throw "$taskMode failed"}
 }
 Write-Output "PASS startup/restore bilingual matrix; results: $taskMatrix"
}finally{Pop-Location}
