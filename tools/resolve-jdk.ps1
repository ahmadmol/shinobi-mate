param([string]$Jdk=$env:JAVA_HOME)
if([string]::IsNullOrWhiteSpace($Jdk)){
 $taskJavac=Get-Command javac.exe -ErrorAction SilentlyContinue
 if($taskJavac){$Jdk=Split-Path (Split-Path $taskJavac.Source -Parent) -Parent}
}
if([string]::IsNullOrWhiteSpace($Jdk)-or -not(Test-Path -LiteralPath (Join-Path $Jdk 'bin/javac.exe'))){
 throw 'A development JDK 21+ is required. Set JAVA_HOME or pass -Jdk <your JDK directory>. End users use the bundled runtime.'
}
[IO.Path]::GetFullPath($Jdk)
