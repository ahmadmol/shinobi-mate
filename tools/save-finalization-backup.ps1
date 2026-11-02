param([switch]$IncludePrivateAssets)
$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$taskOut=Join-Path $taskRoot 'output/finalization/backup'
New-Item -ItemType Directory -Force $taskOut | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$taskStamp=Get-Date -Format yyyyMMdd-HHmmss
$taskName=if($IncludePrivateAssets){"ShinobiMate-PRIVATE-development-$taskStamp.zip"}else{"ShinobiMate-source-$taskStamp.zip"}
$taskArchivePath=Join-Path $taskOut $taskName
$taskZip=[IO.Compression.ZipFile]::Open($taskArchivePath,[IO.Compression.ZipArchiveMode]::Create)
try {
 $taskFolders=@('src','tools','conf');if($IncludePrivateAssets){$taskFolders+=@('img','audio','art')}
 foreach($taskFolder in $taskFolders){
  foreach($taskFile in Get-ChildItem -LiteralPath (Join-Path $taskRoot $taskFolder) -Recurse -File){
   $taskRel=[IO.Path]::GetRelativePath($taskRoot,$taskFile.FullName).Replace('\','/')
   if($taskRel -match '^tools/maven/' -or $taskFile.Extension -in @('.class','.p12','.key','.log','.exe','.jar')){continue}
   if(-not $IncludePrivateAssets -and $taskFile.Extension -in @('.png','.jpg','.jpeg','.ico','.wav')){continue}
   [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($taskZip,$taskFile.FullName,$taskRel,[IO.Compression.CompressionLevel]::Optimal) | Out-Null
  }
 }
 foreach($taskName in @('pom.xml','README.md','LICENSE','NOTICE.md','.gitignore','SHINOBIMATE_FINAL_PRODUCTION_QA.md','FINAL_RELEASE_GATE_CHECKLIST.md')){
  if(-not(Test-Path -LiteralPath (Join-Path $taskRoot $taskName))){continue}
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($taskZip,(Join-Path $taskRoot $taskName),$taskName,[IO.Compression.CompressionLevel]::Optimal) | Out-Null
 }
 foreach($taskFile in Get-ChildItem -LiteralPath (Join-Path $taskRoot 'output/finalization') -File -Filter *.md){
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($taskZip,$taskFile.FullName,"handoff/$($taskFile.Name)",[IO.Compression.CompressionLevel]::Optimal) | Out-Null
 }
}finally{$taskZip.Dispose()}
Get-FileHash -LiteralPath $taskArchivePath
