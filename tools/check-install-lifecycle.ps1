param([string]$Jdk='C:/Program Files/Android/Android Studio/jbr')
$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskQa=Join-Path $taskRoot 'output/lifecycle-qa'
$taskApp=Join-Path $taskQa 'Application'
$taskProfile=Join-Path $taskQa 'Profile'
$taskExe=Join-Path $taskApp 'ShinobiMate.exe'
function Assert-Result($Condition,[string]$Message){if(-not $Condition){throw $Message};Add-Content "$taskQa/results.txt" "PASS $Message"}
function Get-TargetProcesses { @(Get-CimInstance Win32_Process -Filter "Name='ShinobiMate.exe'" | Where-Object {$_.ExecutablePath -eq $taskExe}) }
function Invoke-Agent([string]$Mode,[string]$ResultName){
 $taskProcesses=Get-TargetProcesses
 $taskChild=$taskProcesses | Where-Object {$_.ParentProcessId -in @($taskProcesses.ProcessId)}
 Assert-Result ($null -ne $taskChild) 'one launcher child located'
 & "$Jdk/bin/java.exe" --add-modules jdk.attach -cp "$taskQa/classes" AttachBetaProbe $taskChild.ProcessId "$taskQa/agent.jar" "$taskQa/$ResultName|$Mode"
 Assert-Result ($LASTEXITCODE -eq 0) "application agent $Mode"
}
function Start-Fixture {
 $taskProcess=Start-Process -FilePath $taskExe -WindowStyle Hidden -PassThru
 Start-Sleep -Seconds 12
 Assert-Result (-not $taskProcess.HasExited) 'native production launcher remains running'
 return $taskProcess
}
New-Item -ItemType Directory -Force "$taskQa/classes" | Out-Null
if(-not(Test-Path $taskExe)){
 Copy-Item "$taskRoot/output/release-image/ShinobiMate-Portable" $taskApp -Recurse
}
New-Item -ItemType Directory -Force $taskProfile | Out-Null
if(-not(Test-Path "$taskProfile/settings.properties")){
 Set-Content "$taskProfile/settings.properties" "Language=en-GB`nOnboardingComplete=true`nRestorePreviousCharacters=true`nPreviousCharacters=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nMaxCharacters=5`nScaling=1`n" -Encoding ascii
}
if((Get-Content "$taskApp/app/ShinobiMate.cfg" -Raw) -notmatch 'app.userDataDir'){
 Add-Content "$taskApp/app/ShinobiMate.cfg" "`njava-options=-Dapp.userDataDir=$taskProfile`n"
}
if((Get-TargetProcesses).Count -ne 0){throw 'Fixture still running; close it gracefully before repeating this check'}
Set-Content "$taskQa/results.txt" 'Lifecycle QA; real native launcher and bundled JVM; test agent is not shipped.'
& "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp "$taskRoot/target/ShinobiMate.jar" -d "$taskQa/classes" "$PSScriptRoot/LifecycleAgent.java" "$PSScriptRoot/AttachBetaProbe.java"
Assert-Result ($LASTEXITCODE -eq 0) 'test agent compilation'
Set-Content "$taskQa/agent-manifest.mf" "Agent-Class: LifecycleAgent`n" -Encoding ascii
& "$Jdk/bin/jar.exe" cfm "$taskQa/agent.jar" "$taskQa/agent-manifest.mf" -C "$taskQa/classes" .
Copy-Item "$taskRoot/target/ShinobiMate.jar" "$taskApp/app/ShinobiMate.jar" -Force
New-Item -ItemType Directory -Force "$taskProfile/packs" | Out-Null
Set-Content "$taskProfile/packs/preservation-sentinel.txt" 'Keep imported user data'
$taskSentinelHash=(Get-FileHash "$taskProfile/packs/preservation-sentinel.txt").Hash
$taskFirst=Start-Fixture
Invoke-Agent five final-five.txt
Assert-Result ((Get-Content "$taskQa/final-five.txt" -Raw) -match 'count=5' ) 'five native mascots and tray fixture'
$taskSecond=Start-Process -FilePath $taskExe -WindowStyle Hidden -PassThru
Assert-Result ($taskSecond.WaitForExit(15000)) 'second launch returns promptly'
Assert-Result ((Get-TargetProcesses).Count -eq 2) 'one launcher and one JVM after duplicate launch'
Invoke-Agent five final-tray.txt
Invoke-Agent combat final-combat.txt
Assert-Result ((Get-Content "$taskQa/final-combat.txt" -Raw) -match 'combat=true') 'combat active before shutdown'
# Invalid authentication must never close the running app.
$taskEndpoint=[IO.File]::ReadAllLines("$taskProfile/instance.activation")
$taskClient=[Net.Sockets.TcpClient]::new('127.0.0.1',[int]$taskEndpoint[0])
try{$taskBytes=[Text.Encoding]::UTF8.GetBytes(('0'*36)+' STOP');$taskStream=$taskClient.GetStream();$taskStream.ReadTimeout=2000;$taskStream.Write($taskBytes,0,$taskBytes.Length);Assert-Result ($taskStream.ReadByte() -ne 1) 'invalid shutdown token rejected'}finally{$taskClient.Dispose()}
Assert-Result (-not $taskFirst.HasExited) 'invalid token leaves app alive'
& "$env:SystemRoot/System32/WindowsPowerShell/v1.0/powershell.exe" -NoProfile -NonInteractive -ExecutionPolicy Bypass -File "$PSScriptRoot/installer/request-shutdown.ps1" -ApplicationDir $taskApp -ProfileDir $taskProfile -LogPath "$taskQa/final-shutdown.log"
Assert-Result ($LASTEXITCODE -eq 0) 'cooperative shutdown helper releases Restart Manager file owners'
Assert-Result ($taskFirst.WaitForExit(5000)) 'launcher exits after JVM shutdown'
Assert-Result ((Get-TargetProcesses).Count -eq 0) 'no target processes remain'
Assert-Result (-not(Test-Path "$taskProfile/instance.activation")) 'control socket endpoint removed'
foreach($taskFile in @('app/ShinobiMate.jar','ShinobiMate.exe','runtime/bin/server/jvm.dll')){
 $taskHandle=[IO.File]::Open((Join-Path $taskApp $taskFile),'Open','Read','None');$taskHandle.Dispose()
 Assert-Result $true "exclusive access released: $taskFile"
}
$taskLock=[IO.File]::Open("$taskProfile/instance.lock",'Open','ReadWrite','None');$taskLock.Dispose()
Assert-Result $true 'single-instance file lock released'
Assert-Result ((Get-FileHash "$taskProfile/packs/preservation-sentinel.txt").Hash -eq $taskSentinelHash) 'user pack data preserved'
$taskRestart=Start-Fixture
Invoke-Agent five final-restart.txt
Assert-Result ((Get-Content "$taskQa/final-restart.txt" -Raw) -match 'count=5') 'five characters restored on restart'
Invoke-Agent exit final-exit.txt
Assert-Result ($taskRestart.WaitForExit(10000)) 'same Exit API as tray closes native process'
Assert-Result ((Get-TargetProcesses).Count -eq 0) 'restart and Exit leave no independent JVM'
Get-Content "$taskQa/results.txt"
