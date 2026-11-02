param([Parameter(Mandatory=$true)][string]$ApplicationDir,
      [Parameter(Mandatory=$true)][string]$LogPath,
      [string]$ProfileDir=(Join-Path $env:LOCALAPPDATA 'ShinobiMate'),
      [int]$TimeoutSeconds=20)
$ErrorActionPreference='Stop'
function Write-Diagnostic([string]$Message){Add-Content -LiteralPath $LogPath -Value ((Get-Date -Format o)+' '+$Message) -Encoding UTF8}
try {
 $ApplicationDir=[IO.Path]::GetFullPath($ApplicationDir).TrimEnd('\')
 Write-Diagnostic "Graceful shutdown requested for $ApplicationDir; timeout=$TimeoutSeconds seconds. No process termination is used."
 # Restart Manager is used only to enumerate file owners. RmShutdown is deliberately never called.
 Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
using System.Text;
public static class ShinobiFileOwners {
 [StructLayout(LayoutKind.Sequential)] public struct UniqueProcess { public uint Pid; public System.Runtime.InteropServices.ComTypes.FILETIME Started; }
 [StructLayout(LayoutKind.Sequential, CharSet=CharSet.Unicode)] public struct Owner {
  public UniqueProcess Process;
  [MarshalAs(UnmanagedType.ByValTStr,SizeConst=256)] public string Name;
  [MarshalAs(UnmanagedType.ByValTStr,SizeConst=64)] public string Service;
  public uint Type,Status,Session;
  [MarshalAs(UnmanagedType.Bool)] public bool Restartable;
 }
 [DllImport("rstrtmgr.dll",CharSet=CharSet.Unicode)] static extern int RmStartSession(out uint session,int flags,StringBuilder key);
 [DllImport("rstrtmgr.dll",CharSet=CharSet.Unicode)] static extern int RmRegisterResources(uint session,uint count,string[] files,uint processes,IntPtr processList,uint services,IntPtr serviceList);
 [DllImport("rstrtmgr.dll")] static extern int RmGetList(uint session,out uint needed,ref uint count,[In,Out] Owner[] owners,ref uint reasons);
 [DllImport("rstrtmgr.dll")] static extern int RmEndSession(uint session);
 public static Owner[] Find(string[] files) {
  uint session; int result=RmStartSession(out session,0,new StringBuilder(33));
  if(result!=0)throw new Exception("RmStartSession="+result);
  try {
   if(files.Length==0)return new Owner[0];
   result=RmRegisterResources(session,(uint)files.Length,files,0,IntPtr.Zero,0,IntPtr.Zero);
   if(result!=0)throw new Exception("RmRegisterResources="+result);
   for(int attempt=0;attempt<4;attempt++) {
    uint needed,count=0,reasons=0;
    result=RmGetList(session,out needed,ref count,null,ref reasons);
    if(result==0)return new Owner[0];
    if(result!=234)throw new Exception("RmGetList="+result);
    var owners=new Owner[needed];count=needed;
    result=RmGetList(session,out needed,ref count,owners,ref reasons);
    if(result==0){Array.Resize(ref owners,(int)count);return owners;}
    if(result!=234)throw new Exception("RmGetList="+result);
   }
   throw new Exception("File owner list kept changing");
  } finally {RmEndSession(session);}
 }
}
'@
 # Inno owns its uninstaller files and removes them itself; registering the running
 # uninstaller here would make every uninstall block on its own PID.
 $files=@(Get-ChildItem -LiteralPath $ApplicationDir -Recurse -File -ErrorAction SilentlyContinue | Where-Object {$_.Name -notmatch '^unins\d+\.(exe|dat|msg)$'} | ForEach-Object {$_.FullName})
 function Get-Blockers {
  $owners=@([ShinobiFileOwners]::Find([string[]]$files))
  $processes=@(Get-CimInstance Win32_Process -Filter "Name='ShinobiMate.exe'" | Where-Object {$_.ExecutablePath -eq (Join-Path $ApplicationDir 'ShinobiMate.exe')})
  foreach($p in $processes){Write-Diagnostic "Application PID=$($p.ProcessId) parent=$($p.ParentProcessId) path=$($p.ExecutablePath)"}
  foreach($owner in $owners){Write-Diagnostic "Restart Manager blocker PID=$($owner.Process.Pid) name=$($owner.Name) type=$($owner.Type)"}
  return ($owners.Count+$processes.Count)
 }
 if((Get-Blockers) -eq 0){Write-Diagnostic 'No file owners or running target launcher.';exit 0}
 $endpoint=Join-Path $ProfileDir 'instance.activation'
 if((Test-Path -LiteralPath $endpoint)-and (Get-Item -LiteralPath $endpoint).Length -le 128){
  $info=[IO.File]::ReadAllText($endpoint).Split([char]10)
  if($info.Count -eq 2 -and $info[1] -match '^[a-f0-9-]{36}$'){
   $client=New-Object Net.Sockets.TcpClient
   try {
    $pending=$client.BeginConnect('127.0.0.1',[int]$info[0],$null,$null)
    if(-not $pending.AsyncWaitHandle.WaitOne(1000)){throw 'Control connection timeout'}
    $client.EndConnect($pending);$stream=$client.GetStream();$stream.ReadTimeout=1500
    $request=[Text.Encoding]::UTF8.GetBytes($info[1]+' STOP');$stream.Write($request,0,$request.Length)
    Write-Diagnostic ('Shutdown acknowledgement='+$stream.ReadByte())
   }catch{Write-Diagnostic "Control request unavailable (legacy versions need Tray > Exit): $_"}
   finally{$client.Dispose()}
  }
 }
 $deadline=[DateTime]::UtcNow.AddSeconds([Math]::Min(60,[Math]::Max(1,$TimeoutSeconds)))
 do {
  Start-Sleep -Milliseconds 500
  if((Get-Blockers) -eq 0){Write-Diagnostic 'PASS all target processes and Restart Manager file owners released.';exit 0}
 }while([DateTime]::UtcNow -lt $deadline)
 Write-Diagnostic 'BLOCKED: close ShinobiMate using Tray > Exit (required for 0.9.2/0.9.3), then retry. No files were updated and no process was killed.'
 exit 10
}catch{Write-Diagnostic "ERROR: shutdown verification failed: $_";exit 20}
