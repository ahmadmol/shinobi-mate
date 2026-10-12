param([string]$Compiler='C:\Program Files (x86)\Inno Setup 6\ISCC.exe')
$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
if(-not(Test-Path -LiteralPath $Compiler)){throw 'Install official Inno Setup 6.7.3, including ISCC.exe. Expected path: C:\Program Files (x86)\Inno Setup 6\ISCC.exe. See tools/installer/README.md. No compiler is downloaded automatically.'}
$taskPortable=Join-Path $taskRoot 'output/release-image/ShinobiMate-Portable'
if(-not(Test-Path "$taskPortable/ShinobiMate.exe")){throw 'Run tools/build-portable.ps1 first'}
$taskMetadata=ConvertFrom-StringData ([IO.File]::ReadAllText((Join-Path $taskPortable 'app/build-info.properties')))
$taskNumeric=$taskMetadata.version.Split('-')[0]
& $Compiler "/DPortableRoot=$taskPortable" "/DOutputRoot=$taskRoot/dist" "/DAppRelease=$($taskMetadata.version)" "/DNumericVersion=$taskNumeric" "/DPublisher=$($taskMetadata.publisher)" (Join-Path $taskRoot 'tools/installer/ShinobiMate.iss')
if($LASTEXITCODE -ne 0){throw 'Installer compilation failed'}
& (Join-Path $PSScriptRoot 'write-checksums.ps1')
