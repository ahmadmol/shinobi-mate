param([Parameter(Mandatory)][string]$Destination)
$ErrorActionPreference='Stop'
New-Item -ItemType Directory -Force $Destination | Out-Null
$taskRoot=Split-Path $PSScriptRoot -Parent
[xml]$taskPom=Get-Content -LiteralPath (Join-Path $taskRoot 'pom.xml') -Raw
$taskNamespace=[Xml.XmlNamespaceManager]::new($taskPom.NameTable);$taskNamespace.AddNamespace('m','http://maven.apache.org/POM/4.0.0')
foreach($taskDep in $taskPom.SelectNodes('/m:project/m:dependencies/m:dependency',$taskNamespace)){
 $taskVersion=[string]$taskDep.version
 if($taskVersion -match '^\$\{(.+)\}$'){$taskVersion=$taskPom.SelectSingleNode("/m:project/m:properties/m:$($Matches[1])",$taskNamespace).InnerText}
 $taskGroup=[string]$taskDep.groupId;$taskArtifact=[string]$taskDep.artifactId
 $taskRepository=if($env:MAVEN_REPO_LOCAL){$env:MAVEN_REPO_LOCAL}else{Join-Path $env:USERPROFILE '.m2/repository'}
 $taskDepDir=Join-Path $taskRepository "$($taskGroup.Replace('.','/'))/$taskArtifact/$taskVersion"
 $taskJar=Join-Path $taskDepDir "$taskArtifact-$taskVersion.jar"
 $taskOut=Join-Path $Destination "$taskArtifact-$taskVersion";New-Item -ItemType Directory -Force $taskOut | Out-Null
 Copy-Item -LiteralPath (Join-Path $taskDepDir "$taskArtifact-$taskVersion.pom") -Destination "$taskOut/dependency.pom"
 $taskArchive=[IO.Compression.ZipFile]::OpenRead($taskJar)
 try{
  foreach($taskEntry in $taskArchive.Entries){
   if($taskEntry.FullName -match '(?i)(license|notice|copying|al2\.0|lgpl2\.1)' -and $taskEntry.Length -gt 0){
    $taskName=$taskEntry.FullName.Replace('/','_').Replace('\','_')
    [IO.Compression.ZipFileExtensions]::ExtractToFile($taskEntry,(Join-Path $taskOut $taskName),$true)
   }
  }
 }finally{$taskArchive.Dispose()}
}
