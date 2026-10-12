$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$taskRows=@{}
function Add-ProductFile([string]$taskRelative,[string]$taskChange,[string]$taskGroup){
 $taskAbsolute=[IO.Path]::GetFullPath((Join-Path $taskRoot $taskRelative))
 if(-not $taskAbsolute.StartsWith($taskRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Inventory path escaped workspace'}
 if(-not(Test-Path -LiteralPath $taskAbsolute -PathType Leaf)){throw "Inventory file missing: $taskRelative"}
 $taskPath=[IO.Path]::GetRelativePath($taskRoot,$taskAbsolute).Replace('\','/')
 $taskRows[$taskPath]=[PSCustomObject]@{Change=$taskChange;Group=$taskGroup;Path=$taskPath;AbsolutePath=$taskAbsolute;Bytes=(Get-Item -LiteralPath $taskAbsolute).Length}
}
foreach($taskModule in @('audio','character','focus','interaction','mood','pack','performance','progression','settings','ui','update')){
 foreach($taskFile in Get-ChildItem -LiteralPath "$taskRoot/src/main/java/com/group_finity/mascot/$taskModule" -File -Filter '*.java'){
  Add-ProductFile ([IO.Path]::GetRelativePath($taskRoot,$taskFile.FullName)) NEW SOURCE
 }
}
Add-ProductFile 'src/main/java/com/group_finity/mascot/cloud/CustomMessages.java' NEW SOURCE
foreach($taskSource in @('Main','Manager','Mascot','animation/Pose','config/ActionBuilder','script/Script','sound/Sounds','win/AutoStartManager','imagesetchooser/ImageSetChooser','packagemanager/MascotPackageManager','cloud/CloudMessages','cloud/CloudPanel','cloud/CloudState','cloud/SpiritualCloud','combat/CombatProfile','combat/CombatMatch','combat/CombatEngine','combat/CombatEffects')){
 Add-ProductFile "src/main/java/com/group_finity/mascot/$taskSource.java" MODIFIED SOURCE
}
foreach($taskTool in @('CombatVisualCheck.java','SettingsCheck.java','CharacterCheck.java','PackCheck.java','PackImportCheck.java','MoodCheck.java','FocusCheck.java','ProgressionCheck.java','UpdateCheck.java','AudioCheck.java','StartupCheck.java','UiRefinementCheck.java','BuildPortableTool.java','DistributionSmokeCheck.java','PerformanceCheck.java','export-combat-atlas.ps1','build-portable.ps1','build-installer.ps1','check-product.ps1','check-distribution.ps1','write-product-inventory.ps1','installer/README.md','installer/ShinobiMate.iss')){
 Add-ProductFile "tools/$taskTool" NEW TOOL
}
Add-ProductFile 'tools/CombatCheck.java' MODIFIED TEST
foreach($taskExisting in @('PolishEngineCheck.java','CharacterArtEngineCheck.java','CombatDesktopCheck.java','SpiritualCloudCheck.java','check-combat.ps1')){Add-ProductFile "tools/$taskExisting" REUSED TEST}
foreach($taskTree in @('img','Shimeji-ee/img')){foreach($taskPack in @('NarutoShippuden','SasukeShippuden','GojoSatoru','SukunaRyomen','AstaBlackClover')){
 Add-ProductFile "$taskTree/$taskPack/manifest.json" NEW PACK_DATA
 Add-ProductFile "$taskTree/$taskPack/conf/cloud_messages_en.properties" NEW PACK_DATA
 Add-ProductFile "$taskTree/$taskPack/conf/combat.xml" MODIFIED PACK_DATA
 foreach($taskFile in Get-ChildItem -LiteralPath "$taskRoot/$taskTree/$taskPack/combat/poses" -File){Add-ProductFile ([IO.Path]::GetRelativePath($taskRoot,$taskFile.FullName)) NEW COMBAT_ART}
 foreach($taskFile in Get-ChildItem -LiteralPath "$taskRoot/$taskTree/$taskPack/combat/vfx" -File | Where-Object {$_.Name -match '^(charge|hitflash)[0-3]\.png$'}){Add-ProductFile ([IO.Path]::GetRelativePath($taskRoot,$taskFile.FullName)) NEW COMBAT_ART}
}}
foreach($taskFile in Get-ChildItem -LiteralPath "$taskRoot/art/combat-polish","$taskRoot/art/product-roadmap" -Recurse -File){
 if($taskFile.Name -ne 'FILES.csv'){Add-ProductFile ([IO.Path]::GetRelativePath($taskRoot,$taskFile.FullName)) NEW ART_AND_REPORT}
}
Add-ProductFile README.md MODIFIED DOCUMENT
Add-ProductFile target/ShinobiMate.jar GENERATED BUILD
Add-ProductFile Shimeji-ee/app/Shimeji-ee.jar MODIFIED BUILD
Add-ProductFile dist/ShinobiMate-Portable.zip GENERATED DISTRIBUTION
foreach($taskFile in Get-ChildItem -LiteralPath "$taskRoot/dist/ShinobiMate-Portable" -Recurse -File){Add-ProductFile ([IO.Path]::GetRelativePath($taskRoot,$taskFile.FullName)) GENERATED DISTRIBUTION}
foreach($taskFile in Get-ChildItem -LiteralPath "$taskRoot/output/product-check" -File -Filter '*.log'){Add-ProductFile ([IO.Path]::GetRelativePath($taskRoot,$taskFile.FullName)) EVIDENCE TEST_LOG}
foreach($taskEvidence in @('product-build.log','portable-package-tool.log','portable-package-info.json','portable-smoke-result.txt','performance-1.json','performance-5.json','combat-desktop.png')){Add-ProductFile "output/$taskEvidence" EVIDENCE TEST_LOG}
$taskInventory=Join-Path $taskRoot 'art/product-roadmap/FILES.csv'
$taskRows['art/product-roadmap/FILES.csv']=[PSCustomObject]@{Change='NEW';Group='ART_AND_REPORT';Path='art/product-roadmap/FILES.csv';AbsolutePath=$taskInventory;Bytes=''}
$taskRows.Values | Sort-Object Path | Export-Csv -LiteralPath $taskInventory -NoTypeInformation -Encoding utf8
Write-Output "PASS exact product inventory: $($taskRows.Count) file paths"
