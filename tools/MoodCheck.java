import com.group_finity.mascot.mood.*;
import com.group_finity.mascot.interaction.*;
import com.group_finity.mascot.cloud.ClickGesture;
import java.awt.Point;
public class MoodCheck extends CombatCheck {
 public static void main(String[] args){
  try{
   MoodState a=new MoodState(0),b=new MoodState(0);
   a.update(180000);require(a.mood()==MoodState.Mood.BORED,"idle not bored");
   a.update(600000);require(a.mood()==MoodState.Mood.SLEEPY,"long idle not sleepy");
   a.interact(MoodState.Event.CLICK,600001);require(a.mood()==MoodState.Mood.EXCITED,"wake reaction");
   require(b.mood()==MoodState.Mood.NORMAL,"instance mood leaked");
   a.interact(MoodState.Event.DRAG,610000);a.interact(MoodState.Event.DRAG,611000);a.interact(MoodState.Event.DRAG,612000);
   require(a.mood()==MoodState.Mood.ANNOYED,"repeated drags not counted");
   a.update(620000);require(a.mood()==MoodState.Mood.ANNOYED,"mood changed too quickly");
   a.interact(MoodState.Event.WIN,621000);require(a.mood()==MoodState.Mood.EXCITED,"win mood");
   a.interact(MoodState.Event.LOSS,622000);require(a.mood()==MoodState.Mood.BORED,"loss mood");
   a.update(680000);require(a.mood()==MoodState.Mood.NORMAL,"held mood did not settle");
   InteractionState mouse=new InteractionState();mouse.hover(true);require(mouse.reaction(0)==InteractionState.Reaction.HOVER,"hover");
   mouse.click(1000);require(mouse.reaction(1001)==InteractionState.Reaction.CLICK,"single click");
   mouse.click(1200);require(mouse.reaction(1201)==InteractionState.Reaction.DOUBLE_CLICK,"double click");
   mouse.hover(false);require(mouse.reaction(4000)==InteractionState.Reaction.NONE,"reaction did not expire");
   mouse.drag(5000);require(mouse.reaction(5001)==InteractionState.Reaction.DRAG,"drag reaction");
   ClickGesture gesture=new ClickGesture();gesture.press(new Point(0,0));require(gesture.release(new Point(5,0)),"jitter misclassified");
   gesture.press(new Point(0,0));require(!gesture.release(new Point(6,0)),"drag misclassified");
   integration();System.out.println("PASS per-instance moods, idle/wake, dwell, drag bursts, win/loss, hover/click/double/drag and combat/cloud arbitration; "+checks+" assertions");System.exit(0);
  }catch(Throwable t){t.printStackTrace();System.exit(1);}
 }
}
