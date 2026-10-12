package com.group_finity.mascot.character;
/** Offline future-store vocabulary only; no billing API, entitlement server or combat advantage. */
public record ContentAccess(String id,Kind kind,Access access,InstallStatus installation) {
 public enum Kind { CHARACTER,ANIMATION,THEME,COMBAT_PACK }
 public enum Access { FREE,PREMIUM,OWNED,LOCKED }
 public enum InstallStatus { AVAILABLE,INSTALLED }
 public boolean usable(){return installation==InstallStatus.INSTALLED&&(access==Access.FREE||access==Access.OWNED);}
}
