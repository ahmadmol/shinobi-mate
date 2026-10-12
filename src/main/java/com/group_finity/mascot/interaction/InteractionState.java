package com.group_finity.mascot.interaction;
/** The existing click/drag detector supplies events; this state never starts another gesture. */
public final class InteractionState {
 public enum Reaction { NONE,HOVER,CLICK,DOUBLE_CLICK,DRAG }
 private boolean hover;private long lastClick=-1000,reactionUntil;
 private Reaction reaction=Reaction.NONE;
 public synchronized void hover(boolean value){hover=value;}
 public synchronized void click(long now){reaction=now-lastClick<=350?Reaction.DOUBLE_CLICK:Reaction.CLICK;lastClick=now;reactionUntil=now+1800;}
 public synchronized void drag(long now){reaction=Reaction.DRAG;reactionUntil=now+1800;}
 public synchronized Reaction reaction(long now){return now<reactionUntil?reaction:hover?Reaction.HOVER:Reaction.NONE;}
}
