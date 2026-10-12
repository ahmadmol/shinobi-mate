$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskLines=[Collections.Generic.List[string]]::new()
foreach($taskCharacter in @('NarutoShippuden','SasukeShippuden')){
 [xml]$taskXml=Get-Content -LiteralPath (Join-Path $taskRoot "img/$taskCharacter/conf/combat.xml") -Raw
 $taskLines.Add("`n### $taskCharacter`n")
 $taskLines.Add('| الهجمة | السلاح | damage / نوافذ strikes | min..max range | startup/active/recovery ms | cooldown ms | knockback | VFX / impact | صوت startup/release/impact |')
 $taskLines.Add('|---|---|---|---|---|---|---|---|---|')
 foreach($taskAttack in $taskXml.combat.attacks.attack){
  $taskStrikes=@($taskAttack.strike | Where-Object {$_})
  $taskDamage=if($taskStrikes.Count){($taskStrikes | ForEach-Object {"$($_.damage)@+$([int]$_.at*40)ms/$([int]$_.duration*40)ms(kb=$($_.knockback))"}) -join ', '}else{$taskAttack.damage}
  $taskImpact=if($taskAttack.impactEffect){$taskAttack.impactEffect}else{'impact'}
  $taskLines.Add("| $($taskAttack.id) | $($taskAttack.weapon) | $taskDamage | $($taskAttack.minRange)..$($taskAttack.range) | $([int]$taskAttack.startup*40)/$([int]$taskAttack.active*40)/$([int]$taskAttack.recovery*40) | $([int]$taskAttack.cooldown*40) | $($taskAttack.knockback) | $($taskAttack.effect)/$taskImpact | $($taskAttack.startupSound)/$($taskAttack.attackSound)/$($taskAttack.impactSound) |")
 }
 $taskLines.Add("`nالمرساة الجديدة:288×288، القدم(144,288). الإطارات الـ24:`n")
 $taskFrames=(Get-ChildItem -LiteralPath (Join-Path $taskRoot "img/$taskCharacter/combat/ninja093") -Filter '*.png' | Select-Object -ExpandProperty BaseName) -join ', '
 $taskLines.Add($taskFrames)
}
$taskReport=Join-Path $taskRoot 'art/ninja-polish/REPORT-0.9.3-ar.md'
$taskBody=Get-Content -LiteralPath $taskReport -Raw
$taskMarker='<!-- GENERATED VALUES -->'
$taskBody=$taskBody.Split(@($taskMarker),[StringSplitOptions]::None)[0].TrimEnd()
[IO.File]::WriteAllText($taskReport,$taskBody+"`n`n$taskMarker`n"+($taskLines -join "`n"),[Text.UTF8Encoding]::new($false))
Write-Output 'Wrote final attack values'
