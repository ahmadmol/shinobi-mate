param([string]$Jdk=$env:JAVA_HOME,[switch]$SkipBuild)
$ErrorActionPreference='Stop'
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$taskStamp=Get-Date -Format 'yyyyMMdd-HHmmss-fff'
Push-Location $taskRoot
try {
 $env:JAVA_HOME=$Jdk
 New-Item -ItemType Directory -Force output | Out-Null
 if(-not $SkipBuild){
  & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package > output/portable-build.log 2>&1
  if($LASTEXITCODE -ne 0){throw 'Maven build failed; see output/portable-build.log'}
 }
 if(-not(Test-Path target/ShinobiMate.jar)){throw 'No Maven application JAR'}
 $taskMetadata=ConvertFrom-StringData ([IO.File]::ReadAllText((Join-Path $taskRoot 'target/classes/build-info.properties')))
 $taskVersion=$taskMetadata.version
 $taskToolRoot=Join-Path $taskRoot "output/packaging-tool-$taskStamp"
 $taskPackageRoot=Join-Path $taskRoot "output/package-build-$taskStamp"
 $taskInput=Join-Path $taskRoot 'output/packaging-input'
 New-Item -ItemType Directory -Force "$taskToolRoot/app","$taskToolRoot/classes",$taskPackageRoot,$taskInput | Out-Null
 Copy-Item -LiteralPath target/ShinobiMate.jar -Destination "$taskInput/ShinobiMate.jar" -Force
 Copy-Item -LiteralPath Shimeji-ee/Shimeji-ee.exe -Destination "$taskToolRoot/Shimeji-ee.exe"
 New-Item -ItemType Junction -Path "$taskToolRoot/runtime" -Target (Join-Path $taskRoot 'Shimeji-ee/runtime') | Out-Null
 & "$Jdk/bin/javac.exe" --release 21 -d "$taskToolRoot/classes" tools/BuildPortableTool.java
 if($LASTEXITCODE -ne 0){throw 'Packaging helper compilation failed'}
 & "$Jdk/bin/jar.exe" --create --file "$taskToolRoot/app/packager.jar" -C "$taskToolRoot/classes" .
 if($LASTEXITCODE -ne 0){throw 'Packaging helper JAR failed'}
 $taskToolConfig=@'
[Application]
app.classpath=$APPDIR\packager.jar
app.mainclass=BuildPortableTool
[JavaOptions]
java-options=--add-modules=jdk.jpackage
java-options=-Dpackaging.workspace=<ROOT>
java-options=-Dpackaging.destination=<DEST>
'@
 [IO.File]::WriteAllText("$taskToolRoot/app/Shimeji-ee.cfg",$taskToolConfig.Replace('<ROOT>',$taskRoot).Replace('<DEST>',$taskPackageRoot),[Text.UTF8Encoding]::new($false))
 $taskToolProcess=Start-Process -FilePath "$taskToolRoot/Shimeji-ee.exe" -WorkingDirectory $taskToolRoot -WindowStyle Hidden -PassThru
 if(-not $taskToolProcess.WaitForExit(120000)){throw 'jpackage helper timed out; its process was left intact for diagnosis'}
 if($taskToolProcess.ExitCode -ne 0){throw 'jpackage failed; see output/portable-package-tool.log'}
 $taskBuilt=Join-Path $taskPackageRoot 'ShinobiMate'
 if(-not(Test-Path "$taskBuilt/ShinobiMate.exe")){throw 'jpackage did not create the launcher'}
 New-Item -ItemType Directory -Force "$taskBuilt/conf","$taskBuilt/img" | Out-Null
 Copy-Item -LiteralPath audio -Destination "$taskBuilt/audio" -Recurse
 Get-ChildItem -LiteralPath conf -File | Where-Object {$_.Name -match '^(language.*\.properties|schema.*\.properties|logging\.properties|theme\.properties|Mascot\.xsd|icon\.ico)$'} |
  Copy-Item -Destination "$taskBuilt/conf"
 foreach($taskPack in Get-ChildItem -LiteralPath img -Directory){
  if($taskPack.Name -match '^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$' -and (Test-Path "$($taskPack.FullName)/conf/actions.xml") -and (Test-Path "$($taskPack.FullName)/conf/behaviors.xml")){
   Copy-Item -LiteralPath $taskPack.FullName -Destination "$taskBuilt/img" -Recurse
  }
 }
 Copy-Item -LiteralPath img/icon.ico -Destination "$taskBuilt/img/icon.ico"
 Copy-Item -LiteralPath img/icon.png -Destination "$taskBuilt/img/icon.png"
 Copy-Item -LiteralPath conf/ShinobiMate.ico -Destination "$taskBuilt/ShinobiMate.ico"
 Copy-Item -LiteralPath LICENSE -Destination "$taskBuilt/LICENSE"
 Copy-Item -LiteralPath NOTICE.md -Destination "$taskBuilt/NOTICE.md"
 Copy-Item -LiteralPath target/classes/build-info.properties -Destination "$taskBuilt/app/build-info.properties"
 & (Join-Path $PSScriptRoot 'collect-licenses.ps1') -Destination "$taskBuilt/licenses"

 $taskReadme=@'
ShinobiMate Portable <VERSION>

استخرج المجلد كاملًا، ثم شغّل ShinobiMate.exe. Java مضمّنة.
Extract the entire folder, then run ShinobiMate.exe. Java is included.

First launch: language, first character, interaction guide.
Manage companions and settings from the tray. Combat starts only from a character's right-click menu.
User settings, messages, progress, imported packs and logs: %LOCALAPPDATA%\ShinobiMate.
Updates preserve this directory. Startup is optional. This is a private Friends Beta.
بياناتك محفوظة في مجلد المستخدم، ولا تُحذف عند تحديث البرنامج.
'@
 [IO.File]::WriteAllText("$taskBuilt/README.txt",$taskReadme.Replace('<VERSION>',$taskVersion),[Text.UTF8Encoding]::new($false))
 $taskForbidden=Get-ChildItem -LiteralPath $taskBuilt -Recurse -File | Where-Object {
  $taskRelative=[IO.Path]::GetRelativePath($taskBuilt,$_.FullName)
  $taskRelative -match '(^|[\\/])(src|target|output|logs|\.git|\.idea|test|tests)([\\/]|$)' -or $_.Extension -in @('.java','.class','.ps1','.log','.smp')
 }
 if($taskForbidden){throw 'Forbidden development/test files in portable staging'}
 $taskDist=Join-Path $taskRoot 'dist';New-Item -ItemType Directory -Force $taskDist | Out-Null
 $taskFinal=[IO.Path]::GetFullPath((Join-Path $taskRoot 'output/release-image/ShinobiMate-Portable'))
 $taskZip=[IO.Path]::GetFullPath((Join-Path $taskDist "ShinobiMate-Portable-$taskVersion.zip"))
 $taskBackup=[IO.Path]::GetFullPath((Join-Path $taskRoot "output/packaging-backups/$taskStamp"))
 foreach($taskChecked in @($taskBuilt,$taskFinal,$taskZip,$taskBackup)){
  if(-not $taskChecked.StartsWith($taskRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw "Packaging path escaped workspace: $taskChecked"}
 }
 New-Item -ItemType Directory -Force (Split-Path $taskFinal -Parent) | Out-Null
 if((Test-Path $taskFinal)-or(Test-Path $taskZip)){
  New-Item -ItemType Directory -Force $taskBackup | Out-Null
  if(Test-Path $taskFinal){Move-Item -LiteralPath $taskFinal -Destination "$taskBackup/ShinobiMate-Portable"}
  if(Test-Path $taskZip){Move-Item -LiteralPath $taskZip -Destination "$taskBackup/ShinobiMate-Portable.zip"}
 }
 foreach($taskOldName in @('ShinobiMate-Portable','ShinobiMate-Portable.zip')){
  $taskOld=[IO.Path]::GetFullPath((Join-Path $taskDist $taskOldName))
  if(-not $taskOld.StartsWith($taskDist+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Invalid old distribution path'}
  if(Test-Path $taskOld){New-Item -ItemType Directory -Force "$taskBackup/old-dist" | Out-Null;Move-Item -LiteralPath $taskOld -Destination "$taskBackup/old-dist/$taskOldName"}
 }
 Move-Item -LiteralPath $taskBuilt -Destination $taskFinal
 [IO.Compression.ZipFile]::CreateFromDirectory($taskFinal,$taskZip,[IO.Compression.CompressionLevel]::Optimal,$true)
 $taskInfo=[ordered]@{date=(Get-Date -Format o);version=$taskVersion;runtime=(Get-Content "$taskFinal/runtime/release" -TotalCount 1);
  jarSHA256=(Get-FileHash "$taskFinal/app/ShinobiMate.jar").Hash;zipSHA256=(Get-FileHash $taskZip).Hash;zipBytes=(Get-Item $taskZip).Length;
  files=(Get-ChildItem $taskFinal -Recurse -File).Count}
 $taskInfo | ConvertTo-Json | Set-Content output/portable-package-info.json -Encoding utf8
 Write-Output "PASS portable: $taskZip"
}finally{Pop-Location}
