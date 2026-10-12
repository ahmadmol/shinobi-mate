$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$report=Join-Path $root 'art/combat-polish/REPORT-0.9.2-ar.md'
$text=[System.Collections.Generic.List[string]]::new()
foreach($character in @('NarutoShippuden','SasukeShippuden','GojoSatoru','SukunaRyomen','AstaBlackClover')) {
 [xml]$xml=Get-Content -LiteralPath (Join-Path $root "img/$character/conf/combat.xml") -Raw
 $text.Add("`n### $character`n")
 $text.Add('| الهجمة | النوع/السلاح | damage / strikes | مدى min..max | startup/active/recovery ms | cooldown ms | knockback | startup / release / impact صوت | VFX | hitbox x,y,w,h |')
 $text.Add('|---|---|---|---|---|---|---|---|---|---|')
 foreach($attack in $xml.combat.attacks.attack) {
  $strikes=@($attack.strike | Where-Object {$_})
  $damage=if($strikes.Count){($strikes | ForEach-Object {"$($_.damage)@+$([int]$_.at*40)ms/$([int]$_.duration*40)ms(kb=$($_.knockback))"}) -join ', '}else{$attack.damage}
  $hit=$attack.hitbox
  $text.Add("| $($attack.id) | $($attack.kind)/$($attack.weapon) | $damage | $($attack.minRange)..$($attack.range) | $([int]$attack.startup*40)/$([int]$attack.active*40)/$([int]$attack.recovery*40) | $([int]$attack.cooldown*40) | $($attack.knockback) | $($attack.startupSound)/$($attack.attackSound)/$($attack.impactSound) | $($attack.effect) | $($hit.x),$($hit.y),$($hit.width),$($hit.height) |")
 }
 $text.Add("`nالمقذوفات (speed بوحدة px/tick قبل scale):`n")
 $text.Add('| الهجمة | السلاح | speed | lifetime ticks | damage لكل مقذوف | العدد لكل activation | clone |')
 $text.Add('|---|---|---|---|---|---|---|')
 foreach($attack in $xml.combat.attacks.attack | Where-Object {[double]$_.projectileSpeed -gt 0}) {
  $count=[Math]::Max(1,@($attack.strike | Where-Object {$_}).Count)
  $life=[Math]::Min(250,[Math]::Ceiling([double]$attack.range/[double]$attack.projectileSpeed)+2)
  $damage=if(@($attack.strike | Where-Object {$_}).Count){(@($attack.strike) | ForEach-Object {$_.damage}) -join '/'}else{$attack.damage}
  $text.Add("| $($attack.id) | $($attack.weapon) | $($attack.projectileSpeed) | $life | $damage | $count | $($attack.clone) |")
 }
 $personality=($xml.combat.personality.Attributes | ForEach-Object {"$($_.Name)=$($_.Value)"}) -join '; '
 $text.Add("`nAI/defense: $personality`n")
 $effects=($xml.SelectNodes('//effect') | ForEach-Object {$_.id} | Where-Object {$_} | Sort-Object -Unique) -join ', '
 $text.Add("VFX IDs: $effects`n")
 $frames=(Get-ChildItem -LiteralPath (Join-Path $root "img/$character/combat/polish") -Filter '*.png' | Select-Object -ExpandProperty BaseName) -join ', '
 $text.Add("الإطارات الجديدة الـ16: $frames`n")
}
$body=Get-Content -LiteralPath $report -Raw
$marker='<!-- GENERATED-VALUES -->'
$body=$body.Split(@($marker),[System.StringSplitOptions]::None)[0].TrimEnd()
[System.IO.File]::WriteAllText($report,$body+"`n`n$marker`n"+($text -join "`n"),[System.Text.UTF8Encoding]::new($false))
$logs=@('CombatCheck.log','CombatPolishCheck.log','CombatVisualCheck.log','AudioCheck.log','settings-check.log','stability-check.log')
$out=[System.Collections.Generic.List[string]]::new()
$out.Add('Final successful checks; assembled from each final check log (no production rebuild after packaging).')
foreach($log in $logs){$out.Add("`n$log");$out.Add((Get-Content -LiteralPath (Join-Path $root "output/combat-polish/$log") -Tail 5) -join "`n")}
[System.IO.File]::WriteAllText((Join-Path $root 'output/combat-polish/suite-final.log'),($out -join "`n"),[System.Text.UTF8Encoding]::new($false))
Write-Output "Wrote $report"
