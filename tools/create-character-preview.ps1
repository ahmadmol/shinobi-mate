$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$out=Join-Path $root 'output/character-art-refresh'
Add-Type -AssemblyName System.Drawing
$chars=@('AstaBlackClover','NarutoShippuden','SasukeShippuden')
$selected=@('Preview','Stand','Smile','Sit','CreepExtend','GrabWall','GrabCeiling','DragWince','HandSign')
$board=[Drawing.Bitmap]::new(1152,570)
$g=[Drawing.Graphics]::FromImage($board)
$g.Clear([Drawing.Color]::FromArgb(24,30,41))
$font=[Drawing.Font]::new('Segoe UI',10)
for($r=0;$r -lt 3;$r++) {
 $g.DrawString($chars[$r],$font,[Drawing.Brushes]::White,4,($r*190+4))
 for($c=0;$c -lt $selected.Count;$c++) {
  $x=$c*128;$y=$r*190+27
  $g.FillRectangle([Drawing.Brushes]::LightGray,$x,$y,128,128)
  $image=[Drawing.Image]::FromFile((Join-Path $root "img/$($chars[$r])/$($selected[$c]).png"))
  $g.DrawImageUnscaled($image,$x,$y);$image.Dispose()
  $g.DrawString($selected[$c],$font,[Drawing.Brushes]::White,$x+3,$y+132)
 }
}
$board.Save((Join-Path $out 'overview.png'),[Drawing.Imaging.ImageFormat]::Png)
$g.Dispose();$board.Dispose();$font.Dispose()
foreach($char in $chars) {
 $files=Get-ChildItem -LiteralPath (Join-Path $root "img/$char") -Filter *.png | Sort-Object Name
 $board=[Drawing.Bitmap]::new(1024,1240);$g=[Drawing.Graphics]::FromImage($board)
 $g.Clear([Drawing.Color]::FromArgb(24,30,41));$font=[Drawing.Font]::new('Segoe UI',9)
 $g.DrawString($char,$font,[Drawing.Brushes]::White,5,5)
 for($i=0;$i -lt $files.Count;$i++) {
  $x=($i%8)*128;$y=[Math]::Floor($i/8)*150+30
  $g.FillRectangle([Drawing.Brushes]::LightGray,$x,$y,128,128)
  $image=[Drawing.Image]::FromFile($files[$i].FullName);$g.DrawImageUnscaled($image,$x,$y);$image.Dispose()
  $g.DrawString($files[$i].BaseName,$font,[Drawing.Brushes]::White,$x+2,$y+129)
 }
 $board.Save((Join-Path $out "$char-frames.png"),[Drawing.Imaging.ImageFormat]::Png)
 $g.Dispose();$board.Dispose();$font.Dispose()
}
$payload=@{}
foreach($char in $chars) {
 $images=@{}
 foreach($file in Get-ChildItem -LiteralPath (Join-Path $root "img/$char") -Filter *.png) {
  $images[$file.BaseName]='data:image/png;base64,'+[Convert]::ToBase64String([IO.File]::ReadAllBytes($file.FullName))
 }
 $payload[$char]=$images
}
$json=$payload | ConvertTo-Json -Depth 4 -Compress
$html=@'
<!doctype html><html lang="ar" dir="rtl"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>ShinobiMate — معاينة الشخصيات</title>
<style>
*{box-sizing:border-box}body{margin:0;background:#151c29;color:#eef3fb;font:16px/1.7 "Segoe UI",sans-serif}main{max-width:1000px;margin:40px auto;padding:24px}h1{font-size:28px;margin:0}p{color:#b4c3d7}nav{display:flex;flex-wrap:wrap;gap:8px;margin:24px 0}button,select{font:inherit;border:1px solid #455269;background:#202d42;color:#fff;border-radius:10px;padding:7px 16px;cursor:pointer}button.active{background:#5745bb;border-color:#9180f0}.cards{display:grid;grid-template-columns:repeat(3,1fr);gap:18px}article{border:1px solid #34415a;background:#202a3c;border-radius:16px;padding:20px;text-align:center}h2{font-size:16px;direction:ltr}.stage{height:210px;position:relative;background:linear-gradient(45deg,#27354c 25%,transparent 25%,transparent 75%,#27354c 75%),linear-gradient(45deg,#27354c 25%,#233048 25%,#233048 75%,#27354c 75%);background-size:24px 24px;background-position:0 0,12px 12px;border-radius:12px;overflow:hidden}.sprite{position:absolute;width:128px;height:128px;left:50%;bottom:18px;transform:translateX(-50%)}.name{direction:ltr;font-size:12px;color:#c6d5ec;margin:12px}.choices{display:flex;gap:12px;align-items:center;flex-wrap:wrap}footer{font-size:13px;color:#adbed6;margin-top:20px}@media(max-width:700px){.cards{grid-template-columns:1fr}}
</style><main><h1>ShinobiMate</h1><p>أستا، ناروتو وساسكي — رسوم chibi بحجم 128×128 وخلفية شفافة.</p><nav id="actions"></nav><div class="choices"><label>الخلفية <select id="background"><option value="checker">مربعات</option><option value="#f4f1ea">فاتحة</option><option value="#111827">داكنة</option></select></label><label>الاتجاه <select id="direction"><option value="1">الأصلي</option><option value="-1">معكوس</option></select></label><button id="pause">إيقاف الحركة</button></div><br><div class="cards" id="cards"></div><footer>هذه معاينة للرسوم. نتائج اختبار محرك البرنامج محفوظة في REPORT.ar.md.</footer></main>
<script>
const assets=__ASSETS__;
const clips={"المعاينة":["Preview"],"وقوف وتعابير":["Stand","Stand","Blink","Smile","MouseSurprise"],"المشي":["WalkLeft","WalkPass1","WalkRight","WalkPass2"],"الجلوس":["Sit","SitLookUp","SitHeadTurn1","SitHeadTurn3","SitHeadTurn2","SitHeadTurn4"],"الساقان":["SitLegsDown","SitDangle1","SitLegsDown","SitDangle2"],"القفز":["JumpStart","NinjaJump","NinjaJump","NinjaJump","BounceCompress","Smile"],"الزحف":["CreepContract","CreepExtend"],"الجدار":["GrabWall","ClimbWallExtend","ClimbWallContract"],"السقف":["GrabCeiling","ClimbCeiling1","ClimbCeiling2"],"السحب":["DragWince","Resist1","Resist2","StruggleHard1","StruggleHard2"],"وضعية خاصة":["HandSign","HandSign","Smile"],"العناق":["hug1","hug2","hug3","hug4"]};
let clip="المعاينة",index=0,paused=false;
for(const character of Object.keys(assets)){const card=document.createElement('article');card.innerHTML=`<h2>${character}</h2><div class="stage"><img class="sprite" alt="${character}"></div><div class="name"></div>`;card.dataset.character=character;document.querySelector('#cards').append(card)}
for(const name of Object.keys(clips)){const button=document.createElement('button');button.textContent=name;button.onclick=()=>{clip=name;index=0;document.querySelectorAll('nav button').forEach(b=>b.classList.toggle('active',b===button));render()};document.querySelector('#actions').append(button)}
document.querySelector('nav button').classList.add('active');
function render(){const file=clips[clip][index%clips[clip].length];for(const card of document.querySelectorAll('article')){const sprite=card.querySelector('img');sprite.src=assets[card.dataset.character][file];sprite.style.transform=`translateX(-50%) scaleX(${document.querySelector('#direction').value})`;sprite.style.bottom=clip==="السقف"?'auto':(clip==="القفز"&&file==="NinjaJump"?'36px':'18px');sprite.style.top=clip==="السقف"?'0':'auto';card.querySelector('.name').textContent=file+'.png'}}
document.querySelector('#pause').onclick=()=>{paused=!paused;document.querySelector('#pause').textContent=paused?'تشغيل الحركة':'إيقاف الحركة'};
document.querySelector('#direction').onchange=render;document.querySelector('#background').onchange=e=>{for(const stage of document.querySelectorAll('.stage'))stage.style.background=e.target.value==='checker'?'':e.target.value};
render();setInterval(()=>{if(!paused){index++;render()}},180);
</script></html>
'@
[IO.File]::WriteAllText((Join-Path $out 'preview.html'),$html.Replace('__ASSETS__',$json),[Text.UTF8Encoding]::new($false))
Write-Output 'Created overview, complete contact sheets, and self-contained animated preview.'
