param([Parameter(Mandatory=$true)][int]$TaskProcessId,[Parameter(Mandatory=$true)][string]$Destination)
$ErrorActionPreference='Stop'
$taskStarted=Get-Date
$taskRows=[Collections.Generic.List[object]]::new()
try {
 $taskProcess=Get-Process -Id $TaskProcessId
 while(-not $taskProcess.HasExited){
  $taskProcess.Refresh()
  $taskRows.Add([pscustomobject]@{utc=(Get-Date).ToUniversalTime().ToString('o');seconds=((Get-Date)-$taskStarted).TotalSeconds;
   cpuSeconds=$taskProcess.TotalProcessorTime.TotalSeconds;workingSetBytes=$taskProcess.WorkingSet64;
   privateBytes=$taskProcess.PrivateMemorySize64;handles=$taskProcess.HandleCount;osThreads=$taskProcess.Threads.Count})
  $taskRows | Export-Csv -LiteralPath $Destination -NoTypeInformation -Encoding utf8
  Start-Sleep -Seconds 15
 }
 Write-Output "PASS observed process $TaskProcessId exit; $($taskRows.Count) OS memory/CPU/thread/handle samples"
}catch{
 if($taskRows.Count -eq 0){throw}
 Write-Output "Process observation ended after $($taskRows.Count) samples: $($_.Exception.GetType().Name)"
}
