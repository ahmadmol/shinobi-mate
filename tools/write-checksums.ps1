$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskDist=Join-Path $taskRoot 'dist'
$taskArtifacts=Get-ChildItem -LiteralPath $taskDist -File | Where-Object {$_.Name -match '^ShinobiMate-(Setup-.*\.exe|Portable-.*\.zip)$'} | Sort-Object Name
$taskLines=$taskArtifacts | ForEach-Object {"$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())  $($_.Name)"}
[IO.File]::WriteAllLines((Join-Path $taskDist 'checksums.txt'),[string[]]$taskLines,[Text.UTF8Encoding]::new($false))
$taskLines
