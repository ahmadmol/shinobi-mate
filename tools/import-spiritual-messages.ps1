param(
    [Parameter(Mandatory=$true)][string]$Source,
    [string[]]$Characters = @('NarutoShippuden', 'SasukeShippuden', 'GojoSatoru', 'SukunaRyomen', 'AstaBlackClover')
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$sourceText = [IO.File]::ReadAllText((Resolve-Path -LiteralPath $Source), [Text.Encoding]::UTF8)
$entries = [regex]::Matches($sourceText, '(?m)^(\d+)\. \*\*\[([^\]]+)\]\*\* (.+)\r?$')
if ($entries.Count -ne 100 -or $Characters.Count -ne 5) { throw 'Expected exactly 100 source messages and five destination groups.' }
$messages = @()
for ($i = 0; $i -lt 100; $i++) {
    if ([int]$entries[$i].Groups[1].Value -ne ($i+1)) { throw 'Source numbering must be consecutive, 1 through 100.' }
    $messages += $entries[$i].Groups[3].Value.TrimEnd([char]13)
}
if (@($messages | Select-Object -Unique).Count -ne 100) { throw 'Source messages must be distinct.' }
$utf8 = [Text.UTF8Encoding]::new($false)
$archive = Join-Path $projectRoot 'art/spiritual-cloud'
[IO.Directory]::CreateDirectory($archive) | Out-Null
[IO.File]::WriteAllText((Join-Path $archive 'shinobimate_spiritual_messages_100.md'), $sourceText, $utf8)
$manifest = @()
for ($group = 0; $group -lt 5; $group++) {
    $character = $Characters[$group]
    if ($character -notmatch '^[A-Za-z0-9_-]+$') { throw 'Invalid character directory name.' }
    $pack = Join-Path $projectRoot "img/$character"
    $available = Test-Path -LiteralPath $pack -PathType Container
    $destination = if ($available) { Join-Path $pack 'conf' } else { Join-Path $archive "reserved/$character" }
    [IO.Directory]::CreateDirectory($destination) | Out-Null
    $lines = @('# UTF-8. Verbatim message text from shinobimate_spiritual_messages_100.md.'); $ids = @()
    for ($local = 0; $local -lt 20; $local++) {
        $sourceIndex = $group + 5*$local
        $ids += ($sourceIndex+1)
        # Java Properties escaping changes serialization, never the loaded message.
        $escaped = $messages[$sourceIndex].Replace('\','\\').Replace("`t",'\t').Replace("`n",'\n').Replace("`r",'\r')
        $lines += "message.$($local+1)=$escaped"
    }
    [IO.File]::WriteAllText((Join-Path $destination 'cloud_messages_ar.properties'), (($lines -join "`n")+"`n"), $utf8)
    $manifest += [ordered]@{ character=$character; sourceIds=$ids; count=20; installed=$available }
}
[IO.File]::WriteAllText((Join-Path $archive 'distribution.json'), ($manifest | ConvertTo-Json -Depth 5), $utf8)
$manifest | ForEach-Object { Write-Output "$($_.character): $($_.count) messages; installed=$($_.installed)" }
