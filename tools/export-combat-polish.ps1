param([Parameter(Mandatory)][string]$AtlasMap)
$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
Add-Type -AssemblyName System.Drawing
if(-not ('CombatAtlasExport' -as [type])) {
 $taskSource=Get-Content -LiteralPath (Join-Path $PSScriptRoot 'export-combat-atlas.ps1') -Raw
 $taskCode=[regex]::Match($taskSource,"(?s)Add-Type -ReferencedAssemblies System.Drawing @'\r?\n(.*?)\r?\n'@").Groups[1].Value
 if(-not $taskCode){throw 'Atlas exporter unavailable'}
 Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition $taskCode
}
$taskNames=@{
 NarutoShippuden=@('punch-start','punch-contact','punch-recover','kick-start','kick-contact','kick-recover','jump-start','jump-contact','rasengan-low','rasengan-charge','rasengan-contact','rasengan-recover','kunai-start','kunai-contact','clone-sign','new-victory');
 SasukeShippuden=@('sword-start','sword-contact','sword-recover','backswing-start','backswing-contact','kick-start','kick-contact','kick-recover','chidori-low','chidori-charge','chidori-contact','chidori-recover','shuriken-start','shuriken-contact','fire-start','fire-contact');
 GojoSatoru=@('punch-start','punch-contact','elbow-start','elbow-contact','kick-start','kick-contact','kick-recover','taunt','blue-start','blue-contact','red-start','red-contact','purple-low','purple-charge','purple-contact','purple-recover');
 SukunaRyomen=@('punch-start','punch-contact','slash-start','slash-contact','heavy-start','heavy-contact','heavy-recover','dash-contact','curse-start','curse-contact','multi-start','multi-contact','fire-start','fire-charge','fire-contact','new-victory');
 AstaBlackClover=@('sword-start','sword-contact','backswing-start','backswing-contact','heavy-start','heavy-swing','heavy-contact','heavy-recover','dash-start','dash-contact','jump-start','jump-contact','anti-start','anti-contact','parry','new-victory')
}
$taskMap=Get-Content -LiteralPath $AtlasMap -Raw | ConvertFrom-Json
foreach($taskCharacter in $taskNames.Keys) {
 $taskArt=Join-Path $taskRoot "art/combat-polish/$taskCharacter/0.9.2"
 $taskDestination=Join-Path $taskRoot "img/$taskCharacter/combat/polish"
 New-Item -ItemType Directory -Force $taskArt,$taskDestination | Out-Null
 Copy-Item -LiteralPath $taskMap.$taskCharacter -Destination (Join-Path $taskArt 'atlas.png')
 $taskImage=[Drawing.Bitmap]::new((Join-Path $taskArt 'atlas.png'))
 try {
  if($taskImage.GetPixel(0,0).A -gt 0){throw "Opaque backdrop: $taskCharacter"}
  $taskCrops=[CombatAtlasExport]::Components($taskImage)
  $taskScale=112.0/$taskCrops[0].Height
  foreach($taskBox in $taskCrops){$taskScale=[Math]::Min($taskScale,[Math]::Min(122.0/$taskBox.Width,122.0/$taskBox.Height))}
  $taskFrames=@()
  for($taskIndex=0;$taskIndex -lt 16;$taskIndex++) {
   $taskCrop=$taskCrops[$taskIndex];$taskName=$taskNames[$taskCharacter][$taskIndex]
   $taskSprite=[CombatAtlasExport]::Export($taskImage,$taskCrop,$taskScale)
   try{$taskSprite.Save((Join-Path $taskDestination "$taskName.png"),[Drawing.Imaging.ImageFormat]::Png)}finally{$taskSprite.Dispose()}
   $taskFrames+=@{name=$taskName;crop=@($taskCrop.X,$taskCrop.Y,$taskCrop.Width,$taskCrop.Height);scale=$taskScale;canvas=128;baseline=128}
  }
  $taskFrames|ConvertTo-Json -Depth 6|Set-Content (Join-Path $taskArt 'frames.json') -Encoding utf8
 }finally{$taskImage.Dispose()}
 Write-Output "PASS $taskCharacter : 16 additional transparent frames"
}
