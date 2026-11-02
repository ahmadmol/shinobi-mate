#ifndef PortableRoot
 #error Pass /DPortableRoot with the validated application image
#endif
#ifndef OutputRoot
 #error Pass /DOutputRoot
#endif
#ifndef AppRelease
 #error Pass /DAppRelease from Maven build metadata
#endif
#ifndef NumericVersion
 #error Pass /DNumericVersion from Maven build metadata
#endif
#ifndef Publisher
 #error Pass /DPublisher from Maven build metadata
#endif
#ifndef AppGuid
 #define AppGuid "{{DA056346-628F-44AB-9DB7-39E4D4B75D80}"
#endif
#ifndef AppProfile
 #define AppProfile "{localappdata}\ShinobiMate"
#endif
[Setup]
AppId={#AppGuid}
AppName=ShinobiMate
AppVersion={#AppRelease}
AppVerName=ShinobiMate {#AppRelease}
VersionInfoVersion={#NumericVersion}.0
AppPublisher={#Publisher}
DefaultDirName={localappdata}\Programs\ShinobiMate
DefaultGroupName=ShinobiMate
PrivilegesRequired=lowest
MinVersion=10.0
ArchitecturesAllowed=x64os
ArchitecturesInstallIn64BitMode=x64os
OutputDir={#OutputRoot}
OutputBaseFilename=ShinobiMate-Setup-{#AppRelease}
Compression=lzma2
SolidCompression=yes
UninstallDisplayName=ShinobiMate {#AppRelease}
UninstallDisplayIcon={app}\ShinobiMate.exe
SetupIconFile={#PortableRoot}\ShinobiMate.ico
; Custom cooperative protocol + read-only Restart Manager ownership checks below.
; Never let command-line /FORCECLOSEAPPLICATIONS terminate unrelated file owners.
CloseApplications=no
RestartApplications=no
SetupLogging=yes
UninstallLogging=yes
WizardStyle=modern
[Tasks]
Name: desktopicon; Description: "Create a desktop shortcut"; Flags: unchecked
Name: startup; Description: "Start with Windows"; Flags: unchecked
[Files]
Source: "{#PortableRoot}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
[Icons]
Name: "{group}\ShinobiMate"; Filename: "{app}\ShinobiMate.exe"; WorkingDir: "{app}"; IconFilename: "{app}\ShinobiMate.ico"
Name: "{userdesktop}\ShinobiMate"; Filename: "{app}\ShinobiMate.exe"; WorkingDir: "{app}"; IconFilename: "{app}\ShinobiMate.ico"; Tasks: desktopicon
Name: "{userstartup}\ShinobiMate"; Filename: "{app}\ShinobiMate.exe"; Parameters: "--background"; WorkingDir: "{app}"; IconFilename: "{app}\ShinobiMate.ico"; Tasks: startup
[Run]
Filename: "{app}\ShinobiMate.exe"; Description: "Launch ShinobiMate"; Flags: nowait postinstall skipifsilent
[UninstallDelete]
Type: files; Name: "{userstartup}\ShinobiMate.bat"; Check: OwnedStartupEntry('ShinobiMate.bat')
Type: files; Name: "{userstartup}\ShinobiMate.lnk"; Check: OwnedStartupEntry('ShinobiMate.lnk')
[Code]
var
 DeleteUserData: Boolean;
 MaintenanceHandle: THandle;

{ Another portable/installed copy can own the same per-user startup name. }
function OwnedStartupEntry(Name: String): Boolean;
var Path, Target: String; Text: AnsiString; Shell, Shortcut: Variant;
begin
 Result := False;
 Path := ExpandConstant('{userstartup}\') + Name;
 if not FileExists(Path) then exit;
 try
  if CompareText(ExtractFileExt(Name), '.lnk') = 0 then begin
   Shell := CreateOleObject('WScript.Shell');
   Shortcut := Shell.CreateShortcut(Path);
   Target := Shortcut.TargetPath;
   Result := CompareText(Target, ExpandConstant('{app}\ShinobiMate.exe')) = 0;
  end else if LoadStringFromFile(Path, Text) then begin
   Target := Lowercase(ExpandConstant('{app}\ShinobiMate.exe'));
   Result := Pos('"' + Target + '"', Lowercase(String(Text))) > 0;
  end;
 except
  Log('Could not verify startup ownership; preserving ' + Path);
 end;
end;

function CreateMutex(Security: Integer; InitialOwner: Boolean; Name: String): THandle;
 external 'CreateMutexW@kernel32.dll stdcall';
function CloseHandle(Handle: THandle): Boolean;
 external 'CloseHandle@kernel32.dll stdcall';

procedure ReleaseMaintenance();
begin
 if MaintenanceHandle <> 0 then begin
  CloseHandle(MaintenanceHandle); MaintenanceHandle := 0;
 end;
end;

type
 TSocketAddress = record
  Family, Port: Word;
  Address: LongWord;
  Padding: array[0..7] of Byte;
 end;
 TSocketData = array[0..511] of Byte;

function WSAStartup(Version: Word; var Data: TSocketData): Integer;
 external 'WSAStartup@ws2_32.dll stdcall';
function WSACleanup(): Integer;
 external 'WSACleanup@ws2_32.dll stdcall';
function Socket(Family, Kind, Protocol: Integer): THandle;
 external 'socket@ws2_32.dll stdcall';
function ConnectSocket(Handle: THandle; var Address: TSocketAddress; Size: Integer): Integer;
 external 'connect@ws2_32.dll stdcall';
function SocketOption(Handle: THandle; Level, Option: Integer; var Value: Integer; Size: Integer): Integer;
 external 'setsockopt@ws2_32.dll stdcall';
function SendSocket(Handle: THandle; Data: AnsiString; Size, Flags: Integer): Integer;
 external 'send@ws2_32.dll stdcall';
function ReceiveSocket(Handle: THandle; var Data: Byte; Size, Flags: Integer): Integer;
 external 'recv@ws2_32.dll stdcall';
function CloseSocket(Handle: THandle): Integer;
 external 'closesocket@ws2_32.dll stdcall';
function HostToNetworkShort(Value: Word): Word;
 external 'htons@ws2_32.dll stdcall';
function OpenResource(Name: String; Access, Share, Security, Disposition, Flags: LongWord; Template: THandle): THandle;
 external 'CreateFileW@kernel32.dll stdcall';

procedure RequestApplicationStop();
var Endpoint, Request: AnsiString; Nonce: String; Port, Split, I, Timeout: Integer;
 Data: TSocketData; Address: TSocketAddress; Handle: THandle; Ack: Byte;
begin
 if not LoadStringFromFile(ExpandConstant('{#AppProfile}\instance.activation'), Endpoint) then exit;
 if Length(Endpoint) > 128 then exit;
 Split := Pos(#10, String(Endpoint));
 if Split = 0 then exit;
 Port := StrToIntDef(Trim(Copy(String(Endpoint), 1, Split-1)), 0);
 Nonce := Trim(Copy(String(Endpoint), Split+1, Length(Endpoint)));
 if (Port < 1) or (Port > 65535) or (Length(Nonce) <> 36) then exit;
 for I := 1 to Length(Nonce) do if Pos(Nonce[I], '0123456789abcdef-') = 0 then exit;
 if WSAStartup($0202, Data) <> 0 then exit;
 try
  Handle := Socket(2, 1, 6);
  if Handle = THandle(-1) then exit;
  try
   Timeout := 1500;
   SocketOption(Handle, $FFFF, $1005, Timeout, 4);
   SocketOption(Handle, $FFFF, $1006, Timeout, 4);
   Address.Family := 2; Address.Port := HostToNetworkShort(Port);
   Address.Address := $0100007F; { 127.0.0.1 only; never a configurable remote host }
   if ConnectSocket(Handle, Address, 16) <> 0 then exit;
   Request := AnsiString(Nonce + ' STOP');
   if SendSocket(Handle, Request, Length(Request), 0) <> Length(Request) then exit;
   if ReceiveSocket(Handle, Ack, 1, 0) = 1 then Log('Authenticated shutdown acknowledgement=' + IntToStr(Ack));
  finally CloseSocket(Handle); end;
 finally WSACleanup(); end;
end;

function IsOwnUninstaller(Name: String): Boolean;
var I: Integer; Extension: String;
begin
 Result := False;
 if (Length(Name) <> 12) or (CompareText(Copy(Name,1,5), 'unins') <> 0) then exit;
 for I := 6 to 8 do if Pos(Name[I], '0123456789') = 0 then exit;
 Extension := Lowercase(Copy(Name,9,4));
 Result := (Extension = '.exe') or (Extension = '.dat') or (Extension = '.msg');
end;

function ResourcesReleased(Directory: String): Boolean;
var Entry: TFindRec; Path: String; Handle: THandle;
begin
 Result := True;
 if not DirExists(Directory) then exit;
 if not FindFirst(AddBackslash(Directory) + '*', Entry) then begin Result := False; exit; end;
 try
  repeat
   if (Entry.Name <> '.') and (Entry.Name <> '..') then begin
    Path := AddBackslash(Directory) + Entry.Name;
    { Do not follow junctions/symlinks outside the installation. }
    if (Entry.Attributes and $400) <> 0 then begin Log('Blocked reparse point: ' + Path); Result := False; exit; end;
    if (Entry.Attributes and FILE_ATTRIBUTE_DIRECTORY) <> 0 then begin
     if not ResourcesReleased(Path) then begin Result := False; exit; end;
    end else if not IsOwnUninstaller(Entry.Name) then begin
     { Request exclusive read/write access, but never write. This also detects
       loaded image sections. Inno manages its own uninstaller separately. }
     Handle := OpenResource(Path, $C0000000, 0, 0, 3, $80, 0);
     if Handle = THandle(-1) then begin Log('Resource busy or inaccessible: ' + Path); Result := False; exit; end;
     CloseHandle(Handle);
    end;
   end;
  until not FindNext(Entry);
 finally FindClose(Entry); end;
end;

function StopApplication(): Boolean;
var Attempt: Integer;
begin
 if MaintenanceHandle = 0 then
  MaintenanceHandle := CreateMutex(0, False, 'Local\ShinobiMate.Maintenance');
 if MaintenanceHandle = 0 then begin Result := False; exit; end;
 Result := False;
 RequestApplicationStop();
 for Attempt := 0 to 40 do begin
  if ResourcesReleased(ExpandConstant('{app}')) then begin Result := True; break; end;
  if Attempt < 40 then Sleep(500);
 end;
 Log('Native shutdown/resource verification success=' + IntToStr(Ord(Result)));
 if not Result then ReleaseMaintenance();
end;

function PrepareToInstall(var NeedsRestart: Boolean): String;
begin
 Result := '';
 if not StopApplication() then
  Result := 'ShinobiMate or another application still holds installation files. Close ShinobiMate using Tray > Exit, then click Retry. Versions 0.9.2/0.9.3 require manual Exit. No process was killed. See the Setup log for the blocked path.';
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
 if CurStep = ssPostInstall then ReleaseMaintenance();
end;

procedure DeinitializeSetup();
begin
 ReleaseMaintenance();
end;

procedure DeinitializeUninstall();
begin
 ReleaseMaintenance();
end;

function InitializeUninstall(): Boolean;
var
 Dialog: TSetupForm;
 Choice: TNewCheckBox;
 KeepButton, CancelButton: TNewButton;
begin
 DeleteUserData := False;
 Result := True;
 if UninstallSilent then begin
  Result := StopApplication();
  exit;
 end;
 Dialog := CreateCustomForm(460, 150, False, False);
 try
  Dialog.Caption := 'Uninstall ShinobiMate';
  Dialog.ClientWidth := ScaleX(460);
  Dialog.ClientHeight := ScaleY(150);
  Choice := TNewCheckBox.Create(Dialog);
  Choice.Parent := Dialog;
  Choice.Left := ScaleX(20); Choice.Top := ScaleY(24);
  Choice.Width := ScaleX(420); Choice.Height := ScaleY(42);
  Choice.Caption := 'Delete ShinobiMate user data (settings, packs, progress and logs)';
  Choice.Checked := False;
  KeepButton := TNewButton.Create(Dialog); KeepButton.Parent := Dialog;
  KeepButton.Left := ScaleX(230); KeepButton.Top := ScaleY(100);
  KeepButton.Width := ScaleX(100); KeepButton.Caption := 'Continue';
  KeepButton.ModalResult := mrOk; KeepButton.Default := True;
  CancelButton := TNewButton.Create(Dialog); CancelButton.Parent := Dialog;
  CancelButton.Left := ScaleX(340); CancelButton.Top := ScaleY(100);
  CancelButton.Width := ScaleX(100); CancelButton.Caption := 'Cancel';
  CancelButton.ModalResult := mrCancel; CancelButton.Cancel := True;
  Result := Dialog.ShowModal() = mrOk;
  DeleteUserData := Result and Choice.Checked;
 finally
  Dialog.Free();
 end;
 if Result then begin
  Result := StopApplication();
  if not Result then MsgBox('ShinobiMate is still running or files are locked. Use Tray > Exit and run Uninstall again. Your files have not been removed. See the uninstall log for the blocked path.', mbError, MB_OK);
 end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var Profile: String;
begin
 if (CurUninstallStep = usPostUninstall) and DeleteUserData then begin
  Profile := ExpandConstant('{#AppProfile}');
  { Fixed profile target only. DelTree never traverses reparse points. }
  if not DelTree(Profile, True, True, True) then
   MsgBox('Some user data could not be removed. Close ShinobiMate and remove: ' + Profile, mbInformation, MB_OK);
 end;
end;
