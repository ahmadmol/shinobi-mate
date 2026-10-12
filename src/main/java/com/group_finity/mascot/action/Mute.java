package com.group_finity.mascot.action;

import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.sound.Sounds;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.sound.sampled.Clip;

public class Mute extends InstantAction {
   public static final String PARAMETER_SOUND = "Sound";
   private static final String DEFAULT_SOUND = null;

   public Mute(ResourceBundle schema, VariableMap params) {
      super(schema, params);
   }

   protected void apply() throws VariableException {
      String name=this.getSound();
      if(name==null)com.group_finity.mascot.audio.AudioManager.global().stopAll();
      else Sounds.stop(this.getMascot().getImageSet(),name);
   }

   private String getSound() throws VariableException {
      return (String)this.eval(this.getSchema().getString("Sound"), String.class, DEFAULT_SOUND);
   }
}
