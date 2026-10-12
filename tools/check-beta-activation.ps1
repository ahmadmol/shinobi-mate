param([string]$Jdk=$env:JAVA_HOME,[ValidateSet('ar','en-GB')][string]$Language='ar')
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskTemp=Join-Path $env:TEMP "ShinobiMate-activation-$(Get-Date -Format yyyyMMddHHmmssfff)"
New-Item -ItemType Directory -Force $taskTemp,"$taskRoot/output/beta-activation-classes" | Out-Null
Copy-Item -LiteralPath "$taskRoot/output/release-image/ShinobiMate-Portable" -Destination "$taskTemp/Application" -Recurse
$taskApp=Join-Path $taskTemp Application
& "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp "$taskApp/app/ShinobiMate.jar" -d "$taskRoot/output/beta-activation-classes" "$PSScriptRoot/BetaActivationCheck.java"
if($LASTEXITCODE -ne 0){throw 'Activation harness compile failed'}
& "$Jdk/bin/jar.exe" --create --file "$taskApp/app/activation-tests.jar" -C "$taskRoot/output/beta-activation-classes" .
if($LASTEXITCODE -ne 0){throw 'Activation harness JAR failed'}
$taskProfile=Join-Path $taskTemp Profile;New-Item -ItemType Directory -Force $taskProfile | Out-Null
[IO.File]::WriteAllText("$taskProfile/settings.properties","Language=$Language`nOnboardingComplete=true`nAlwaysShowShimejiChooser=true`nActiveShimeji=NarutoShippuden`nScaling=3`nMute=false`n",[Text.UTF8Encoding]::new($false))
$taskConfig=[IO.File]::ReadAllText("$taskApp/app/ShinobiMate.cfg")+"`njava-options=-Dapp.userDataDir=$taskProfile`n"
Copy-Item "$taskApp/ShinobiMate.exe" "$taskApp/ActivationProbe.exe"
[IO.File]::WriteAllText("$taskApp/app/ActivationProbe.cfg",$taskConfig,[Text.UTF8Encoding]::new($false))
[IO.File]::WriteAllText("$taskApp/app/ShinobiMate.cfg",$taskConfig.Replace('app.classpath=$APPDIR\ShinobiMate.jar','app.classpath=$APPDIR\ShinobiMate.jar;$APPDIR\activation-tests.jar').Replace('app.mainclass=com.group_finity.mascot.Main','app.mainclass=BetaActivationCheck'),[Text.UTF8Encoding]::new($false))
$taskSavedPath=$env:PATH;$taskSavedJava=$env:JAVA_HOME
try {
 $env:JAVA_HOME=$null;$env:PATH="$env:SystemRoot/System32;$env:SystemRoot"
 $taskProcess=Start-Process -FilePath "$taskApp/ShinobiMate.exe" -WorkingDirectory $env:TEMP -WindowStyle Hidden -PassThru
 if(-not $taskProcess.WaitForExit(95000)){throw "Activation test timed out; owned process $($taskProcess.Id)"}
 Copy-Item "$taskApp/activation-result.txt" "$taskRoot/output/beta-feedback-activation-$Language.txt" -Force
 Get-Content "$taskApp/activation-result.txt" -Tail 8
 if($taskProcess.ExitCode -ne 0){throw 'Native activation check failed'}
 "Fixture: $taskTemp" | Add-Content "$taskRoot/output/beta-feedback-activation-$Language.txt"
}finally{$env:PATH=$taskSavedPath;$env:JAVA_HOME=$taskSavedJava}
