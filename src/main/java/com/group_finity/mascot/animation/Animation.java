package com.group_finity.mascot.animation;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.hotspot.Hotspot;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

public class Animation {
   private final Variable condition;
   private final Pose[] poses;
   private final Hotspot[] hotspots;
   private final boolean turn;

   public Animation(Variable condition, Pose[] poses, Hotspot[] hotspots, boolean turn) {
      if (poses.length == 0) {
         throw new IllegalArgumentException("poses.length==0");
      } else {
         this.condition = condition;
         this.poses = poses;
         this.hotspots = hotspots;
         this.turn = turn;
      }
   }

   public boolean isEffective(VariableMap variables) throws VariableException {
      return (Boolean)this.getCondition().get(variables);
   }

   public void init() {
      this.getCondition().init();
   }

   public void initFrame() {
      this.getCondition().initFrame();
   }

   public void next(Mascot mascot, int time) {
      Pose pose=this.getPoseAt(time);
      int local=Math.floorMod(time,this.getDuration());
      for(Pose candidate:poses){if(candidate==pose)break;local-=candidate.getDuration();}
      pose.next(mascot);
      if(local==0)pose.emitSound(mascot);
   }

   public Pose getPoseAt(int time) {
      time %= this.getDuration();

      for(Pose pose : this.getPoses()) {
         time -= pose.getDuration();
         if (time < 0) {
            return pose;
         }
      }

      return null;
   }

   public int getDuration() {
      int duration = 0;

      for(Pose pose : this.getPoses()) {
         duration += pose.getDuration();
      }

      return duration;
   }

   private Variable getCondition() {
      return this.condition;
   }

   private Pose[] getPoses() {
      return this.poses;
   }

   public Hotspot[] getHotspots() {
      return this.hotspots;
   }

   public boolean isTurn() {
      return this.turn;
   }
}
