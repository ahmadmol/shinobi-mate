param([Parameter(Mandatory)][string]$Character, [Parameter(Mandatory)][string]$Atlas, [string[]]$FlipPoses=@())
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
if ($Character -notmatch '^[A-Za-z0-9_-]+$') { throw 'Invalid character directory name' }
Add-Type -AssemblyName System.Drawing
if (-not ('CombatAtlasExport' -as [type])) {
Add-Type -ReferencedAssemblies System.Drawing @'
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Collections.Generic;
public static class CombatAtlasExport {
 public static void Effect(string path,bool flash,int frame) {
  using(var im=new Bitmap(96,96,PixelFormat.Format32bppArgb))using(var g=Graphics.FromImage(im)) {
   g.SmoothingMode=SmoothingMode.AntiAlias;
   using(var pen=new Pen(Color.FromArgb(220-frame*50,flash?255:110,flash?245:200,255),3)) {
    int r=flash?22+frame*5:34-frame*5;
    if(flash)for(int i=0;i<8;i++){double a=i*Math.PI/4;g.DrawLine(pen,48+(int)(8*Math.Cos(a)),48+(int)(8*Math.Sin(a)),48+(int)(r*Math.Cos(a)),48+(int)(r*Math.Sin(a)));}
    else {g.DrawArc(pen,48-r,48-r,r*2,r*2,frame*30,280);g.DrawArc(pen,40-r,40-r,(r+8)*2,(r+8)*2,180-frame*25,120);}
   }
   im.Save(path,ImageFormat.Png);
  }
 }
 public static Rectangle[] Components(Bitmap atlas) {
  int w=atlas.Width,h=atlas.Height;bool[] seen=new bool[w*h];var parts=new List<Tuple<int,Rectangle>>();
  for(int start=0;start<w*h;start++) {
   if(seen[start]||atlas.GetPixel(start%w,start/w).A<=12)continue;
   var queue=new Queue<int>();queue.Enqueue(start);seen[start]=true;
   int l=w,t=h,r=0,b=0,count=0;
   while(queue.Count>0) {
    int p=queue.Dequeue(),x=p%w,y=p/w;count++;l=Math.Min(l,x);t=Math.Min(t,y);r=Math.Max(r,x);b=Math.Max(b,y);
    for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++) {
     int xx=x+dx,yy=y+dy;if(xx<0||yy<0||xx>=w||yy>=h)continue;int q=yy*w+xx;
     if(!seen[q]&&atlas.GetPixel(xx,yy).A>12){seen[q]=true;queue.Enqueue(q);}
    }
   }
   parts.Add(Tuple.Create(count,Rectangle.FromLTRB(l,t,r+1,b+1)));
  }
  parts.Sort((a,b)=>b.Item1.CompareTo(a.Item1));
  if(parts.Count<16||parts[15].Item1<1000)throw new Exception("Expected 16 connected character figures");
  var output=new Rectangle[16];
  for(int i=0;i<16;i++) {
   var part=parts[i];
   Rectangle r=part.Item2;int column=Math.Min(3,(r.Left+r.Width/2)/(w/4)),row=Math.Min(3,(r.Top+r.Height/2)/(h/4));
   int index=row*4+column;if(output[index].Width>0)throw new Exception("Ambiguous atlas grid; review crop metadata");output[index]=r;
  }
  return output;
 }
 public static Rectangle Bounds(Bitmap atlas,Rectangle cell) {
  int l=cell.Right,t=cell.Bottom,r=cell.Left-1,b=cell.Top-1;
  for(int y=cell.Top;y<cell.Bottom;y++)for(int x=cell.Left;x<cell.Right;x++)
   if(atlas.GetPixel(x,y).A>12){l=Math.Min(l,x);r=Math.Max(r,x);t=Math.Min(t,y);b=Math.Max(b,y);}
  if(r<l||b<t)throw new Exception("Empty combat sprite cell");
  return Rectangle.FromLTRB(l,t,r+1,b+1);
 }
 public static Bitmap Export(Bitmap atlas,Rectangle crop,double scale) {
  scale=Math.Min(scale,Math.Min(122.0/crop.Width,122.0/crop.Height));
  int w=Math.Max(1,(int)Math.Round(crop.Width*scale)),h=Math.Max(1,(int)Math.Round(crop.Height*scale));
  var result=new Bitmap(128,128,PixelFormat.Format32bppArgb);
  using(var g=Graphics.FromImage(result)) {
   g.CompositingMode=CompositingMode.SourceCopy;g.InterpolationMode=InterpolationMode.HighQualityBicubic;
   g.PixelOffsetMode=PixelOffsetMode.HighQuality;
   using(var attributes=new ImageAttributes()) {
    attributes.SetWrapMode(WrapMode.TileFlipXY);
    g.DrawImage(atlas,new Rectangle((128-w)/2,128-h,w,h),crop.X,crop.Y,crop.Width,crop.Height,GraphicsUnit.Pixel,attributes);
   }
  }
  return result;
 }
}
'@
}
$taskArt = Join-Path $taskRoot "art/combat-polish/$Character"
$taskDestination = Join-Path $taskRoot "img/$Character/combat/poses"
New-Item -ItemType Directory -Force $taskArt,$taskDestination | Out-Null
Copy-Item -LiteralPath $Atlas -Destination (Join-Path $taskArt 'atlas.png')
$taskImage = [Drawing.Bitmap]::new((Join-Path $taskArt 'atlas.png'))
$taskNames = @('stance','melee-start','melee-active','melee-recovery','ranged-start','ranged-active','special-start','special-active',
    'special-recovery','dodge-start','dodge-active','block','hit','knockback','ko','victory')
try {
 $taskCellWidth = [int]($taskImage.Width/4)
 $taskCellHeight = [int]($taskImage.Height/4)
 $taskCrops = [CombatAtlasExport]::Components($taskImage)
 $taskReference = $taskCrops[0]
 $taskScale = 112.0/$taskReference.Height
 # One common scale across poses avoids character size pulsing during sword swings.
 foreach ($taskBox in $taskCrops) {$taskScale=[Math]::Min($taskScale,[Math]::Min(122.0/$taskBox.Width,122.0/$taskBox.Height))}
 $taskFrames = @()
 for ($taskIndex=0;$taskIndex -lt 16;$taskIndex++) {
  $taskColumn=$taskIndex%4;$taskRow=[int][Math]::Floor($taskIndex/4)
  $taskCrop=$taskCrops[$taskIndex]
  $taskSprite=[CombatAtlasExport]::Export($taskImage,$taskCrop,$taskScale)
  if ($taskNames[$taskIndex] -in $FlipPoses) {$taskSprite.RotateFlip([Drawing.RotateFlipType]::RotateNoneFlipX)}
  try {$taskSprite.Save((Join-Path $taskDestination ($taskNames[$taskIndex]+'.png')),[Drawing.Imaging.ImageFormat]::Png)} finally {$taskSprite.Dispose()}
  $taskFrames+=@{name=$taskNames[$taskIndex];column=$taskColumn;row=$taskRow;crop=@($taskCrop.X,$taskCrop.Y,$taskCrop.Width,$taskCrop.Height);scale=$taskScale;flipped=($taskNames[$taskIndex] -in $FlipPoses)}
 }
 $taskFrames | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $taskArt 'frames.json') -Encoding utf8
} finally {$taskImage.Dispose()}
$taskProfile = Join-Path $taskRoot "img/$Character/conf/combat.xml"
$taskBackup = Join-Path $taskRoot "output/product-baseline/$Character-combat.xml"
New-Item -ItemType Directory -Force (Split-Path $taskBackup -Parent) | Out-Null
if (-not (Test-Path -LiteralPath $taskBackup)) {Copy-Item -LiteralPath $taskProfile -Destination $taskBackup}
[xml]$taskXml = Get-Content -LiteralPath $taskProfile -Raw
foreach($taskEffectId in @('charge','hitflash')) {
 $taskEffect=$taskXml.combat.effects.effect | Where-Object id -eq $taskEffectId | Select-Object -First 1
 if(-not $taskEffect) {$taskEffect=$taskXml.CreateElement('effect');$taskEffect.SetAttribute('id',$taskEffectId);$taskEffect.SetAttribute('frameTicks','1');[void]$taskXml.combat.effects.AppendChild($taskEffect)}
 while($taskEffect.HasChildNodes){[void]$taskEffect.RemoveChild($taskEffect.FirstChild)}
 for($taskF=0;$taskF -lt 4;$taskF++) {
  $taskEffectPath="combat/vfx/$taskEffectId$taskF.png"
  [CombatAtlasExport]::Effect((Join-Path $taskRoot "img/$Character/$taskEffectPath"),($taskEffectId -eq 'hitflash'),$taskF)
  $taskEffectFrame=$taskXml.CreateElement('frame');$taskEffectFrame.SetAttribute('image',$taskEffectPath);[void]$taskEffect.AppendChild($taskEffectFrame)
 }
}
$taskAnimationSets = @{
 idle=@('stance');approach=@('stance','dodge-start');dodge=@('dodge-start','dodge-active','dodge-active');
 block=@('block');hit=@('hit');knockback=@('knockback');recover=@('melee-recovery','stance');ko=@('ko');victory=@('victory');
 charge=@('special-start');'heavy-charge'=@('special-start');melee=@('melee-active');sword=@('melee-active');
 ranged=@('ranged-active');dash=@('special-active');aerial=@('dodge-active','melee-active');
 'melee-start'=@('melee-start');'ranged-start'=@('ranged-start');'melee-recovery'=@('melee-recovery');
 'ranged-recovery'=@('melee-recovery');'special-recovery'=@('special-recovery');'special-active'=@('special-active')
}
foreach ($taskAnimationId in $taskAnimationSets.Keys) {
 $taskNode=$taskXml.combat.animations.animation | Where-Object id -eq $taskAnimationId | Select-Object -First 1
 if (-not $taskNode) {$taskNode=$taskXml.CreateElement('animation');$taskNode.SetAttribute('id',$taskAnimationId);[void]$taskXml.combat.animations.AppendChild($taskNode)}
 while ($taskNode.HasChildNodes) {[void]$taskNode.RemoveChild($taskNode.FirstChild)}
 foreach ($taskPose in $taskAnimationSets[$taskAnimationId]) {
  $taskFrame=$taskXml.CreateElement('frame');$taskFrame.SetAttribute('image',"combat/poses/$taskPose.png");$taskFrame.SetAttribute('ticks','3');[void]$taskNode.AppendChild($taskFrame)
 }
}
foreach ($taskAttack in $taskXml.combat.attacks.attack) {
 if ($taskAttack.kind -eq 'special') {$taskAttack.SetAttribute('animation','special-active');$taskAttack.SetAttribute('chargeAnimation','charge');$taskAttack.SetAttribute('recoveryAnimation','special-recovery')}
 elseif ($taskAttack.projectileSpeed -ne '0') {$taskAttack.SetAttribute('chargeAnimation','ranged-start');$taskAttack.SetAttribute('recoveryAnimation','ranged-recovery')}
 else {$taskAttack.SetAttribute('chargeAnimation','melee-start');$taskAttack.SetAttribute('recoveryAnimation','melee-recovery')}
 $taskAttack.SetAttribute('chargeEffect','charge')
}
$taskWriter=[Xml.XmlWriter]::Create($taskProfile,[Xml.XmlWriterSettings]@{Indent=$true;Encoding=[Text.UTF8Encoding]::new($false)})
try {$taskXml.Save($taskWriter)} finally {$taskWriter.Dispose()}
$taskPortable = Join-Path $taskRoot "Shimeji-ee/img/$Character"
Copy-Item -LiteralPath (Join-Path $taskRoot "img/$Character/combat") -Destination $taskPortable -Recurse -Force
Copy-Item -LiteralPath $taskProfile -Destination (Join-Path $taskPortable 'conf/combat.xml') -Force
Write-Output "PASS ${Character}: 16 dedicated combat poses exported and profile staged"
