param([string]$Jdk=$env:JAVA_HOME)
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskImage=Join-Path $taskRoot 'output/release-image/ShinobiMate-Portable'
$taskTemp=Join-Path $env:TEMP "ShinobiMate-beta-$(Get-Date -Format 'yyyyMMdd-HHmmssfff')"
New-Item -ItemType Directory -Force $taskTemp,(Join-Path $taskRoot 'output/beta-native-check') | Out-Null
Copy-Item -LiteralPath $taskImage -Destination "$taskTemp/Application" -Recurse
$taskApp=Join-Path $taskTemp 'Application';$taskWorking=Join-Path $taskTemp 'UnrelatedWorkingDirectory';New-Item -ItemType Directory -Force $taskWorking | Out-Null
& "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp (Join-Path $taskImage 'app/ShinobiMate.jar') -d (Join-Path $taskRoot 'output/beta-native-check') (Join-Path $PSScriptRoot 'BetaStartupCheck.java') (Join-Path $PSScriptRoot 'GuardProbe.java') (Join-Path $PSScriptRoot 'CombatDesktopCheck.java') (Join-Path $PSScriptRoot 'MultiMonitorBetaCheck.java')
if($LASTEXITCODE -ne 0){throw 'Beta startup check compilation failed'}
& "$Jdk/bin/jar.exe" --create --file "$taskApp/app/beta-tests.jar" -C (Join-Path $taskRoot 'output/beta-native-check') .
if($LASTEXITCODE -ne 0){throw 'Beta test JAR creation failed'}
$taskOriginal=[IO.File]::ReadAllText("$taskApp/app/ShinobiMate.cfg")
$taskInstrumented=$taskOriginal.Replace('app.classpath=$APPDIR\ShinobiMate.jar','app.classpath=$APPDIR\ShinobiMate.jar;$APPDIR\beta-tests.jar').Replace('app.mainclass=com.group_finity.mascot.Main','app.mainclass=BetaStartupCheck')
$taskSavedPath=$env:PATH;$taskSavedJava=$env:JAVA_HOME
try{
 $env:JAVA_HOME=$null;$env:PATH="$env:SystemRoot/System32;$env:SystemRoot"
 foreach($taskLanguage in @('ar','en-GB')){
  $taskProfile=Join-Path $taskTemp "Profile-$taskLanguage"
  $taskConfig=$taskInstrumented+"`njava-options=-Dapp.userDataDir=$taskProfile`njava-options=-Dbeta.language=$taskLanguage`njava-options=-Dbeta.firstRun=true`n"
  [IO.File]::WriteAllText("$taskApp/app/ShinobiMate.cfg",$taskConfig,[Text.UTF8Encoding]::new($false))
  $taskProcess=Start-Process -FilePath "$taskApp/ShinobiMate.exe" -WorkingDirectory $taskWorking -WindowStyle Hidden -PassThru
  if(-not $taskProcess.WaitForExit(60000)){throw "Beta startup timed out: owned process $($taskProcess.Id)"}
  Copy-Item -LiteralPath "$taskApp/beta-startup-result.txt" -Destination (Join-Path $taskRoot "output/beta-startup-$taskLanguage.txt") -Force
  Get-Content "$taskApp/beta-startup-result.txt" -Tail 3
  if($taskProcess.ExitCode -ne 0){throw "Beta $taskLanguage first run failed"}
 }
 $taskMonitorProfile=Join-Path $taskTemp 'MultiMonitorProfile';New-Item -ItemType Directory -Force $taskMonitorProfile | Out-Null
 [IO.File]::WriteAllText("$taskMonitorProfile/settings.properties","Language=ar`nOnboardingComplete=true`nAlwaysShowShimejiChooser=false`nActiveShimeji=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nRestorePreviousCharacters=true`nPreviousCharacters=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nMute=true`n",[Text.UTF8Encoding]::new($false))
 $taskMonitorConfig=$taskInstrumented.Replace('app.mainclass=BetaStartupCheck','app.mainclass=MultiMonitorBetaCheck')+"`njava-options=-Dapp.userDataDir=$taskMonitorProfile`n"
 [IO.File]::WriteAllText("$taskApp/app/ShinobiMate.cfg",$taskMonitorConfig,[Text.UTF8Encoding]::new($false))
 $taskMonitorProcess=Start-Process -FilePath "$taskApp/ShinobiMate.exe" -WorkingDirectory $taskWorking -WindowStyle Hidden -PassThru
 if(-not $taskMonitorProcess.WaitForExit(60000)){throw 'Multi-monitor native check timed out'}
 Copy-Item -LiteralPath "$taskApp/multi-monitor-result.txt" -Destination (Join-Path $taskRoot 'output/beta-multi-monitor.txt') -Force
 Get-Content "$taskApp/multi-monitor-result.txt" -Tail 5
 if($taskMonitorProcess.ExitCode -ne 0){throw 'Multi-monitor native check failed'}
 # Real unmodified production entry point, a completed profile and read-only resources.
 $taskRealProfile=Join-Path $taskTemp 'Profile-en-GB'
 [IO.File]::WriteAllText("$taskApp/app/ShinobiMate.cfg",$taskOriginal+"`njava-options=-Dapp.userDataDir=$taskRealProfile`n",[Text.UTF8Encoding]::new($false))
 foreach($taskArea in @('conf','img')){Get-ChildItem -LiteralPath "$taskApp/$taskArea" -Recurse -File | ForEach-Object {$_.IsReadOnly=$true}}
 $taskProcess=Start-Process -FilePath "$taskApp/ShinobiMate.exe" -WorkingDirectory $taskWorking -WindowStyle Hidden -PassThru
 try{
  Start-Sleep -Seconds 5
  if($taskProcess.HasExited){throw 'Actual production entry point exited unexpectedly'}
  $taskJvmProcess=Get-CimInstance Win32_Process | Where-Object {$_.ParentProcessId -eq $taskProcess.Id -and $_.ExecutablePath -eq "$taskApp\ShinobiMate.exe"} | Select-Object -First 1
  $taskJvmPid=if($taskJvmProcess){$taskJvmProcess.ProcessId}else{$taskProcess.Id}
  $taskJvm=(Get-Process -Id $taskJvmPid).Modules | Where-Object {$_.ModuleName -eq 'jvm.dll'} | Select-Object -First 1
  if(-not $taskJvm -or -not $taskJvm.FileName.StartsWith("$taskApp\runtime\",[StringComparison]::OrdinalIgnoreCase)){throw 'Actual EXE did not load bundled JVM'}
  # Probe the same lock from a second native process with the same bundled runtime.
  Copy-Item -LiteralPath "$taskApp/ShinobiMate.exe" -Destination "$taskApp/GuardProbe.exe"
  $taskGuard=$taskOriginal.Replace('app.classpath=$APPDIR\ShinobiMate.jar','app.classpath=$APPDIR\ShinobiMate.jar;$APPDIR\beta-tests.jar').Replace('app.mainclass=com.group_finity.mascot.Main','app.mainclass=GuardProbe')+"`njava-options=-Dapp.userDataDir=$taskRealProfile`n"
  [IO.File]::WriteAllText("$taskApp/app/GuardProbe.cfg",$taskGuard,[Text.UTF8Encoding]::new($false))
  $taskProbe=Start-Process -FilePath "$taskApp/GuardProbe.exe" -WorkingDirectory $taskWorking -WindowStyle Hidden -PassThru
  if(-not $taskProbe.WaitForExit(30000)-or $taskProbe.ExitCode -ne 0){throw 'Second-process profile guard failed'}
  $taskEvidence=@("PASS actual ShinobiMate.exe production Main, relocated application, unrelated CWD, read-only conf/img, no JAVA_HOME or Java/Maven PATH", "Bundled JVM: $($taskJvm.FileName)", [IO.File]::ReadAllText("$taskApp/guard-result.txt"), "Fixture retained: $taskTemp", 'LIMIT: host is Windows 11; this is not a clean VM or another physical machine')
  [IO.File]::WriteAllLines((Join-Path $taskRoot 'output/beta-actual-launcher.txt'),[string[]]$taskEvidence,[Text.UTF8Encoding]::new($false));$taskEvidence
 }finally{
  if($taskJvmPid -and $taskJvmPid -ne $taskProcess.Id){Stop-Process -Id $taskJvmPid -Force -ErrorAction SilentlyContinue}
  if($taskProcess -and -not $taskProcess.HasExited){Stop-Process -Id $taskProcess.Id -Force}
 }
}finally{$env:PATH=$taskSavedPath;$env:JAVA_HOME=$taskSavedJava}
