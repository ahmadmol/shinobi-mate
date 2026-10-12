param([string]$Jdk=$env:JAVA_HOME,[switch]$Performance)
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$taskStamp=Get-Date -Format 'yyyyMMdd-HHmmss'
Push-Location $taskRoot
try {
 $taskPortable=Join-Path $taskRoot 'output/release-image/ShinobiMate-Portable'
 if(-not(Test-Path "$taskPortable/ShinobiMate.exe")){throw 'Build the portable distribution first'}
 $taskSmoke=Join-Path $env:TEMP "output/portable-smoke-$taskStamp"
 Copy-Item -LiteralPath $taskPortable -Destination $taskSmoke -Recurse
 New-Item -ItemType Directory -Force "$taskSmoke/check-classes","$taskSmoke/test-profile",output/portable-smoke-working | Out-Null
 & "$Jdk/bin/javac.exe" --release 21 -cp target/ShinobiMate.jar -d "$taskSmoke/check-classes" tools/CombatDesktopCheck.java tools/DistributionSmokeCheck.java tools/PerformanceCheck.java
 if($LASTEXITCODE -ne 0){throw 'Distribution checks did not compile'}
 & "$Jdk/bin/jar.exe" --create --file "$taskSmoke/app/smoke-tests.jar" -C "$taskSmoke/check-classes" .
 if($LASTEXITCODE -ne 0){throw 'Distribution check JAR failed'}
 $taskConfigPath="$taskSmoke/app/ShinobiMate.cfg"
 $taskConfig=[IO.File]::ReadAllText($taskConfigPath).Replace('app.classpath=$APPDIR\ShinobiMate.jar','app.classpath=$APPDIR\ShinobiMate.jar;$APPDIR\smoke-tests.jar')
 $taskConfig+="`njava-options=-Dapp.userDataDir=$taskSmoke/test-profile`n"
 $taskSettings="Language=ar`nActiveShimeji=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nOnboardingComplete=true`nAlwaysShowShimejiChooser=false`nMenuDPI=96`nMute=true`n"
 [IO.File]::WriteAllText("$taskSmoke/test-profile/settings.properties",$taskSettings,[Text.UTF8Encoding]::new($false))
 [IO.File]::WriteAllText($taskConfigPath,$taskConfig.Replace('app.mainclass=com.group_finity.mascot.Main','app.mainclass=DistributionSmokeCheck'),[Text.UTF8Encoding]::new($false))
 $taskSavedPath=$env:PATH;$taskSavedJava=$env:JAVA_HOME
 $env:JAVA_HOME=$null;$env:PATH="$env:SystemRoot/System32;$env:SystemRoot"
 $taskProcess=Start-Process -FilePath "$taskSmoke/ShinobiMate.exe" -WorkingDirectory (Join-Path $taskRoot 'output/portable-smoke-working') -WindowStyle Hidden -PassThru
 if(-not $taskProcess.WaitForExit(60000)){throw "Smoke check timed out (owned process $($taskProcess.Id)); inspect $taskSmoke/smoke-result.txt"}
 Get-Content -LiteralPath "$taskSmoke/smoke-result.txt" -Tail 5
 if($taskProcess.ExitCode -ne 0){throw 'Real portable launcher smoke check failed'}
 Copy-Item "$taskSmoke/smoke-result.txt" -Destination output/portable-smoke-result.txt -Force
 if($Performance){
  foreach($taskCount in @(1,5)){
   $taskBenchSettings=$taskSettings
   if($taskCount -eq 1){$taskBenchSettings=$taskSettings.Replace('NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover','NarutoShippuden')}
   [IO.File]::WriteAllText("$taskSmoke/test-profile/settings.properties",$taskBenchSettings,[Text.UTF8Encoding]::new($false))
   [IO.File]::WriteAllText($taskConfigPath,$taskConfig.Replace('app.mainclass=com.group_finity.mascot.Main','app.mainclass=PerformanceCheck'),[Text.UTF8Encoding]::new($false))
   $taskBench=Start-Process -FilePath "$taskSmoke/ShinobiMate.exe" -WorkingDirectory (Join-Path $taskRoot 'output/portable-smoke-working') -WindowStyle Hidden -PassThru
   if(-not $taskBench.WaitForExit(60000)){throw "Performance check timed out (owned process $($taskBench.Id))"}
   if($taskBench.ExitCode -ne 0){throw 'Performance measurement failed'}
   Copy-Item "$taskSmoke/performance-result.json" -Destination "output/performance-$taskCount.json" -Force
   Write-Output "PASS real Java 21 performance measurement: $taskCount active character(s)"
  }
 }
 $taskZip=Join-Path $taskRoot "dist/ShinobiMate-Portable-$((ConvertFrom-StringData ([IO.File]::ReadAllText((Join-Path $taskRoot 'target/classes/build-info.properties')))).version).zip"
 $taskArchive=[IO.Compression.ZipFile]::OpenRead($taskZip)
 try {
  foreach($taskEntry in $taskArchive.Entries){
   if(-not $taskEntry.FullName.StartsWith('ShinobiMate-Portable/')){throw 'Unexpected archive root'}
   if($taskEntry.FullName -match '(^|/)(src|target|output|logs|\.git|test|tests)(/|$)' -or $taskEntry.FullName -match '\.(java|class|ps1|log)$'){throw "Development file shipped: $($taskEntry.FullName)"}
  }
  Write-Output "PASS portable ZIP: $($taskArchive.Entries.Count) entries, runtime included, no source/tests/logs"
 }finally{$taskArchive.Dispose()}
}finally{if($taskSavedPath){$env:PATH=$taskSavedPath;$env:JAVA_HOME=$taskSavedJava};Pop-Location}
