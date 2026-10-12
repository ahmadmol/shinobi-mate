param([string]$Jdk=$env:JAVA_HOME,[switch]$SkipBuild,[string[]]$Checks=@())
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output/product-check,output/product-ui/conf,art/product-roadmap/screenshots | Out-Null
 if(-not $SkipBuild){
  & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package > output/product-build.log 2>&1
  if($LASTEXITCODE -ne 0){throw 'Maven failed; see output/product-build.log'}
  Write-Output 'PASS Maven clean package: BUILD SUCCESS'
 }
 Copy-Item -LiteralPath conf -Destination output/product-ui -Recurse -Force
 $taskImages=Join-Path $taskRoot 'output/product-ui/img'
 if(-not(Test-Path -LiteralPath $taskImages)){New-Item -ItemType Junction -Path $taskImages -Target (Join-Path $taskRoot 'img') | Out-Null}
 $taskDefaults="Language=ar`nActiveShimeji=NarutoShippuden`nAlwaysShowShimejiChooser=false`nOnboardingComplete=true`nMenuDPI=96`nMute=true`n"
 $taskProfile=Join-Path $taskRoot 'output/product-profile';New-Item -ItemType Directory -Force $taskProfile | Out-Null
 $taskSettings=Join-Path $taskProfile 'settings.properties'
 if(Test-Path -LiteralPath output/update-test.p12){Remove-Item -LiteralPath output/update-test.p12}
 & {
  & "$Jdk/bin/keytool.exe" -genkeypair -alias updatetest -storetype PKCS12 -keystore output/update-test.p12 -storepass test-password -keypass test-password -keyalg RSA -keysize 2048 -validity 1 -dname CN=localhost -ext SAN=dns:localhost -noprompt > output/update-keytool.log 2>&1
  if($LASTEXITCODE -ne 0){throw 'Local TLS fixture generation failed'}
 }
 $taskSources=@('PolishEngineCheck','CharacterArtEngineCheck','CombatCheck','CombatVisualCheck','SpiritualCloudCheck','SettingsCheck','CharacterCheck','PackCheck','MoodCheck','FocusCheck','ProgressionCheck','UpdateCheck','AudioCheck','PackImportCheck','StartupCheck','UiRefinementCheck','ProductionCheck','RuntimeStabilityCheck')
 $taskFiles=$taskSources | ForEach-Object {"tools/$_.java"}
 & "$Jdk/bin/javac.exe" --release 21 -encoding UTF-8 -cp target/ShinobiMate.jar -d output/product-check @taskFiles > output/product-check/compile.log 2>&1
 if($LASTEXITCODE -ne 0){throw 'Product checks did not compile; see output/product-check/compile.log'}
 $taskRunChecks=if($Checks.Count){$Checks}else{$taskSources}
 foreach($taskCheck in $taskRunChecks){
  if($taskCheck -notin $taskSources){throw "Unknown check: $taskCheck"}
  $taskRunProfile=Join-Path $taskProfile "$taskCheck-$(Get-Date -Format 'yyyyMMddHHmmssfff')";New-Item -ItemType Directory -Force $taskRunProfile | Out-Null
  $taskSettings=Join-Path $taskRunProfile 'settings.properties'
  [IO.File]::WriteAllText($taskSettings,$taskDefaults,[Text.UTF8Encoding]::new($false))
  & "$Jdk/bin/java.exe" --enable-native-access=ALL-UNNAMED "-Dapp.dir=$taskRoot/output/product-ui" "-Dapp.userDataDir=$taskRunProfile" -cp 'target/ShinobiMate.jar;output/product-check' $taskCheck > "output/product-check/$taskCheck.log" 2>&1
  if($LASTEXITCODE -ne 0){Get-Content "output/product-check/$taskCheck.log" -Tail 20;throw "$taskCheck failed"}
  Get-Content "output/product-check/$taskCheck.log" | Select-Object -Last 2
 }
 Write-Output "PASS $($taskRunChecks.Count) product checks; source settings unchanged"
}finally{Pop-Location}
