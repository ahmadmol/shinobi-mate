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
[Setup]
AppId={{DA056346-628F-44AB-9DB7-39E4D4B75D80}
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
CloseApplications=yes
RestartApplications=no
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
Type: files; Name: "{userstartup}\ShinobiMate.bat"
Type: files; Name: "{userstartup}\ShinobiMate.lnk"
[Code]
var DeleteUserData: Boolean;

function InitializeUninstall(): Boolean;
var
 Dialog: TSetupForm;
 Choice: TNewCheckBox;
 KeepButton, CancelButton: TNewButton;
begin
 DeleteUserData := False;
 Result := True;
 if UninstallSilent then exit;
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
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var Profile: String;
begin
 if (CurUninstallStep = usPostUninstall) and DeleteUserData then begin
  Profile := ExpandConstant('{localappdata}\ShinobiMate');
  { Fixed profile target only. DelTree never traverses reparse points. }
  if not DelTree(Profile, True, True, True) then
   MsgBox('Some user data could not be removed. Close ShinobiMate and remove: ' + Profile, mbInformation, MB_OK);
 end;
end;
