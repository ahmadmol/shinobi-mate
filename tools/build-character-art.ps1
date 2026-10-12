param([string[]]$Characters = @('AstaBlackClover','NarutoShippuden','SasukeShippuden'), [switch]$CoreOnly)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Add-Type -AssemblyName System.Drawing
if (-not ('PetAtlas' -as [type])) {
Add-Type -ReferencedAssemblies System.Drawing @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Drawing2D;
using System.Collections.Generic;
public static class PetAtlas {
 public static Bitmap Extract(Bitmap atlas, int[] b, double scale, string mode) {
  int w=b[2]-b[0], h=b[3]-b[1];
  bool[] seen=new bool[w*h]; List<int> best=new List<int>();
  for(int start=0;start<w*h;start++) {
   if(seen[start] || atlas.GetPixel(b[0]+start%w,b[1]+start/w).A<20)continue;
   var component=new List<int>();var queue=new Queue<int>();queue.Enqueue(start);seen[start]=true;
   while(queue.Count>0) {
    int p=queue.Dequeue(),x=p%w,y=p/w;component.Add(p);
    for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++) {
     int xx=x+dx, yy=y+dy;if(xx<0||xx>=w||yy<0||yy>=h)continue;int q=yy*w+xx;
     if(!seen[q]&&atlas.GetPixel(b[0]+xx,b[1]+yy).A>=20){seen[q]=true;queue.Enqueue(q);}
    }
   }
   if(component.Count>best.Count)best=component;
  }
  if(best.Count<400)throw new Exception("Empty or invalid sprite crop");
  bool[] keep=new bool[w*h];int l=w,t=h,r=0,bottom=0;
  foreach(int p in best) {
   int x=p%w,y=p/w;l=Math.Min(l,x);r=Math.Max(r,x);t=Math.Min(t,y);bottom=Math.Max(bottom,y);
   for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)if(x+dx>=0&&x+dx<w&&y+dy>=0&&y+dy<h)keep[(y+dy)*w+x+dx]=true;
  }
  l=Math.Max(0,l-1);t=Math.Max(0,t-1);r=Math.Min(w-1,r+1);bottom=Math.Min(h-1,bottom+1);
  using(var crop=new Bitmap(r-l+1,bottom-t+1,PixelFormat.Format32bppArgb)) {
   for(int y=t;y<=bottom;y++)for(int x=l;x<=r;x++)if(keep[y*w+x])crop.SetPixel(x-l,y-t,atlas.GetPixel(b[0]+x,b[1]+y));
   int maxWidth=mode=="wall"?110:124;
   scale=Math.Min(scale,Math.Min((double)maxWidth/crop.Width,124.0/crop.Height));
   int dw=Math.Max(1,(int)Math.Round(crop.Width*scale)),dh=Math.Max(1,(int)Math.Round(crop.Height*scale));
   int destX=mode=="wall"?16:(128-dw)/2, destY=mode=="ceiling"?0:128-dh;
   var output=new Bitmap(128,128,PixelFormat.Format32bppArgb);
   using(var g=Graphics.FromImage(output)) {
    g.CompositingMode=CompositingMode.SourceCopy;g.InterpolationMode=InterpolationMode.HighQualityBicubic;
    g.PixelOffsetMode=PixelOffsetMode.HighQuality;g.CompositingQuality=CompositingQuality.HighQuality;
    using(var attributes=new ImageAttributes()) {
     attributes.SetWrapMode(WrapMode.TileFlipXY);
     g.DrawImage(crop,new Rectangle(destX,destY,dw,dh),0,0,crop.Width,crop.Height,GraphicsUnit.Pixel,attributes);
    }
   }
   return output;
  }
 }
}
'@
}
$core = @('Stand','Smile','Blink','MouseSurprise','HandSign','Preview',
 'WalkLeft','WalkPass1','WalkRight','WalkPass2','JumpStart','NinjaJump',
 'Falling','BounceCompress','BounceExtend','DragWince','Resist1','Resist2',
 'Sit','SitLookUp','SitHeadTurn1','SitHeadTurn2','SitLegsDown','SitLegsUp',
 'SitDangle1','SitDangle2','CreepContract','CreepExtend','GrabWall','ClimbWallExtend',
 'ClimbWallContract','GrabCeiling','ClimbCeiling1','ClimbCeiling2','hug1','hug2')
$utility = @('SitHeadTurn3','SitHeadTurn4','SitHeadTurn5','SitHeadTurn6',
 'StruggleHard1','StruggleHard2','StruggleHard3','StruggleHard4','hug3','hug4',
 'DivideTwist1','DivideTwist2','DivideTwist3','DivideSplit','DivideComplete',
 'PullUpPrepare','PullUpHalf','PullUpEffort','PullUpComplete','StandWithWindow',
 'WalkWithWindow1','WalkWithWindow2','ThrowWindow')
foreach ($character in $Characters) {
 $destination=Join-Path $root "img/$character"
 New-Item -ItemType Directory -Force -Path $destination | Out-Null
 $manifest=@()
 foreach ($kind in @('core','utility')) {
  if ($CoreOnly -and $kind -eq 'utility') {continue}
  $source=Join-Path $root "art/character-packs/$character-$kind.png"
  $meta=Get-Content -LiteralPath ([IO.Path]::ChangeExtension($source,'.crops.json')) -Raw | ConvertFrom-Json
  $names=if($kind -eq 'core'){$core}else{$utility}
  $expected=if($kind -eq 'core'){6}else{4}
  foreach($column in $meta.columns) {if($column.Count -ne $expected){throw "Review crops for $source"}}
  $atlas=[Drawing.Bitmap]::new($source)
  $reference=if($kind -eq 'core'){$meta.columns[0][0].box}else{$meta.columns[5][3].box}
  $scale=112.0/($reference[3]-$reference[1])
  for($i=0;$i -lt $names.Count;$i++) {
   $name=$names[$i];$col=$i%6;$row=[int][Math]::Floor($i/6)
   $box=[int[]]$meta.columns[$col][$row].box
   $mode=if($name -match '^(GrabWall|ClimbWall)'){'wall'}elseif($name -match '^(GrabCeiling|ClimbCeiling)'){'ceiling'}else{'floor'}
   $sprite=[PetAtlas]::Extract($atlas,$box,$scale,$mode)
   $sprite.Save((Join-Path $destination "$name.png"),[Drawing.Imaging.ImageFormat]::Png);$sprite.Dispose()
   $manifest+=@{file="$name.png";source="$character-$kind.png";row=$row;column=$col;crop=$box;scale=$scale;alignment=$mode}
  }
  $atlas.Dispose()
 }
 $manifest | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $root "art/character-packs/$character-frames.json") -Encoding utf8
 $runtime=Join-Path $root "Shimeji-ee/img/$character"
 New-Item -ItemType Directory -Force -Path $runtime | Out-Null
 Copy-Item -Path (Join-Path $destination '*.png') -Destination $runtime -Force
 Write-Output "$character : $($manifest.Count) frames exported and synchronized"
}
