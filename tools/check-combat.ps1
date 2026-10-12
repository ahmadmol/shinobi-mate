param([string]$Jdk = $env:JAVA_HOME, [switch]$Desktop)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$Jdk=& (Join-Path $PSScriptRoot 'resolve-jdk.ps1') -Jdk $Jdk
$taskJava = Join-Path $Jdk 'bin/java.exe'
$taskJavac = Join-Path $Jdk 'bin/javac.exe'
if (-not (Test-Path -LiteralPath $taskJavac)) { throw 'Set -Jdk to a JDK 21 or newer.' }
$env:JAVA_HOME = $Jdk
Push-Location $taskRoot
try {
    New-Item -ItemType Directory -Force output | Out-Null
    & tools/maven/apache-maven-3.9.9/bin/mvn.cmd clean package 2>&1 | Tee-Object output/combat-build.log
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }
    New-Item -ItemType Directory -Force output/combat-check | Out-Null
    & $taskJavac -cp target/ShinobiMate.jar -d output/combat-check tools/PolishEngineCheck.java tools/CombatCheck.java tools/CombatDesktopCheck.java
    if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed.' }
    & $taskJava --enable-native-access=ALL-UNNAMED -cp 'target/ShinobiMate.jar;output/combat-check' CombatCheck 2>&1 | Tee-Object output/combat-check.log
    if ($LASTEXITCODE -ne 0) { throw 'Combat checks failed.' }
    if ($Desktop) {
        New-Item -ItemType Directory -Force output/native-combat/conf | Out-Null
        Copy-Item -LiteralPath conf -Destination output/native-combat -Recurse -Force
        $taskImageLink = Join-Path $taskRoot 'output/native-combat/img'
        if (-not (Test-Path -LiteralPath $taskImageLink)) { New-Item -ItemType Junction -Path $taskImageLink -Target (Join-Path $taskRoot 'img') | Out-Null }
        New-Item -ItemType Directory -Force output/native-combat/profile | Out-Null
        [IO.File]::WriteAllText((Join-Path $taskRoot 'output/native-combat/profile/settings.properties'),
            "ActiveShimeji=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nRestorePreviousCharacters=true`nPreviousCharacters=NarutoShippuden/SasukeShippuden/GojoSatoru/SukunaRyomen/AstaBlackClover`nAlwaysShowShimejiChooser=false`nOnboardingComplete=true`nLanguage=ar`nMenuDPI=96`n",[Text.UTF8Encoding]::new($false))
        Push-Location output/native-combat
        try {
            $taskClasspath = (Join-Path $taskRoot 'target/ShinobiMate.jar') + ';' + (Join-Path $taskRoot 'output/combat-check')
            & $taskJava --enable-native-access=ALL-UNNAMED "-Dapp.userDataDir=$taskRoot/output/native-combat/profile" -cp $taskClasspath CombatDesktopCheck 2>&1 | Tee-Object ../combat-desktop-check.log
            if ($LASTEXITCODE -ne 0) { throw 'Native desktop checks failed.' }
        } finally { Pop-Location }
    }
} finally { Pop-Location }
