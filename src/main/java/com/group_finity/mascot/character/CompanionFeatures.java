package com.group_finity.mascot.character;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.image.*;
import com.group_finity.mascot.pack.*;
import com.group_finity.mascot.mood.MoodState;
import com.group_finity.mascot.interaction.InteractionState;
import com.group_finity.mascot.settings.RuntimeSettings;
import java.awt.*;
import java.nio.file.*;
import java.util.*;

/** Per-instance presentation/interaction adapter. Pack data chooses poses, never character names. */
public final class CompanionFeatures {
 private final Mascot mascot;
 private final MoodState mood=new MoodState(now());
 private final InteractionState interaction=new InteractionState();
 private final Map<String,ImagePair> images=new HashMap<>();
 private PackManifest pack;private Point previous;
 private int stationary;private long nextPlayful;
 private String temporaryPose,idlePose;private volatile long temporaryUntil,idleUntil,nextIdle=now()+60000;
 private final Random random=new Random();
 public CompanionFeatures(Mascot mascot){this.mascot=mascot;reload();}
 public static long now(){return System.nanoTime()/1_000_000;}
 public MoodState mood(){return mood;}
 public PackManifest pack(){return pack;}
 public void reload(){
  images.clear();idleUntil=temporaryUntil=0;
  try{pack=PackManifest.read(com.group_finity.mascot.AppPaths.pack(mascot.getImageSet()),mascot.getImageSet());}
  catch(Exception ex){pack=null;}
 }
 private void interrupted(){idleUntil=temporaryUntil=0;nextIdle=now()+60000;}
 public void hover(boolean entered){
  interaction.hover(entered);
  if(entered){interrupted();mood.interact(MoodState.Event.HOVER,now());}
 }
 public void click(){interrupted();interaction.click(now());mood.interact(MoodState.Event.CLICK,now());com.group_finity.mascot.progression.ProgressionService.interact(mascot.getImageSet());mascot.playAudio("grunt");}
 public void drag(){interrupted();interaction.drag(now());mood.interact(MoodState.Event.DRAG,now());com.group_finity.mascot.progression.ProgressionService.interact(mascot.getImageSet());}
 public void combatResult(boolean win){mood.interact(win?MoodState.Event.WIN:MoodState.Event.LOSS,now());}
 public void focusComplete(){
  mood.interact(MoodState.Event.WAKE,now());
  if(pack!=null){temporaryPose=pack.moodPoses().get("EXCITED");temporaryUntil=now()+2400;}
 }
 public boolean greet(){
  if(pack==null||pack.socialPoses().isEmpty())return false;
  temporaryPose=pack.socialPoses().get(random.nextInt(pack.socialPoses().size()));temporaryUntil=now()+2400;return true;
 }
 public boolean holdBehavior(){
  return resting()&&(com.group_finity.mascot.focus.FocusService.session().quiet()||
    (RuntimeSettings.number("IdleActivityLevel",1,0,2)>0&&now()<idleUntil)||now()<temporaryUntil);
 }
 public boolean resting(){return stationary>=10&&!mascot.isDragging()&&!mascot.isPaused()&&
    (mascot.getEnvironment().getFloor().isOn(mascot.getAnchor())||mascot.getEnvironment().getActiveIE().getTopBorder().isOn(mascot.getAnchor()));}
 public void tick(boolean fighting){
  long now=now();mood.update(now);
  Point anchor=mascot.getAnchor();stationary=previous!=null&&previous.equals(anchor)?stationary+1:0;previous=new Point(anchor);
  if(fighting||pack==null||!resting())return;
  boolean quiet=com.group_finity.mascot.focus.FocusService.session().quiet();
  int level=(int)RuntimeSettings.number("IdleActivityLevel",1,0,2);
  if(!quiet&&level>0&&now>=nextIdle&&mood.idleMillis(now)>=(level==2?30000:60000)&&!pack.idleActivities().isEmpty()){
   var poses=java.util.List.copyOf(pack.idleActivities().values());idlePose=poses.get(random.nextInt(poses.size()));idleUntil=now+6000;nextIdle=now+(level==2?30000:60000);
  }
  var reaction=interaction.reaction(now);
  boolean mouse=RuntimeSettings.flag("MouseInteractions",true),near=reaction!=InteractionState.Reaction.NONE;
  if(mouse&&near&&RuntimeSettings.flag("CursorTracking",true))mascot.setLookRight(mascot.getEnvironment().getCursor().getX()>anchor.x);
  String pose=null;
  if(mouse&&near) {
   pose=pack.interactionPoses().get(reaction.name());
   if(reaction==InteractionState.Reaction.HOVER&&mascot.getEnvironment().getCursor().getY()<anchor.y-120)
    pose=pack.interactionPoses().getOrDefault("LOOK_UP",pose);
   if(!quiet&&RuntimeSettings.flag("PlayfulCursor",false)&&now>=nextPlayful&&reaction==InteractionState.Reaction.HOVER){
    nextPlayful=now+35000;Rectangle area=mascot.getEnvironment().getWorkArea().toRectangle();
    int direction=mascot.getEnvironment().getCursor().getX()>anchor.x?-1:1;
    int half=mascot.getImage()==null?64:mascot.getImage().size().width/2;
    if(area.width>half*2)mascot.setAnchor(new Point(Math.max(area.x+half,Math.min(area.x+area.width-half,anchor.x+direction*16)),anchor.y));
   }
  }
  if(pose==null&&now<temporaryUntil)pose=temporaryPose;
  if(pose==null&&now<idleUntil&&level>0)pose=idlePose;
  if(pose==null&&quiet)pose=pack.idleActivities().get("relax");
  if(pose==null&&RuntimeSettings.flag("MoodEnabled",true)&&mood.mood()!=MoodState.Mood.NORMAL)pose=pack.moodPoses().get(mood.mood().name());
  if(pose!=null)showPose(pose);
 }
 public void showPose(String relative) {
  if(pack==null||mascot.getImage()==null)return;
  try {
   ImagePair pair=images.get(relative);
   if(pair==null){
    PackPaths.inside(packRoot(),relative);
    double scale=RuntimeSettings.number("Scaling",1,.5,3),opacity=RuntimeSettings.number("Opacity",1,.2,1);
    String key="/"+mascot.getImageSet()+"/"+relative;
    ImagePairLoader.load(key,null,new Point(64,128),scale,ImagePairLoader.Filter.BICUBIC,opacity);
    ImagePair source=ImagePairs.getImagePair(key);
    var left=source.getImage(false);var right=source.getImage(true);
    int x=left.size().width/2,y=left.size().height;
    pair=new ImagePair(new MascotImage(left.image(),new Point(x,y),left.size()),new MascotImage(right.image(),new Point(x,y),right.size()));images.put(relative,pair);
   }
   mascot.setImage(pair.getImage(mascot.isLookRight()));
  }catch(Exception ex){/* Optional expression cannot stop a working legacy behavior. */}
 }
 private Path packRoot(){return com.group_finity.mascot.AppPaths.pack(mascot.getImageSet());}
}
