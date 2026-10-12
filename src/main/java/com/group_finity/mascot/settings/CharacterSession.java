package com.group_finity.mascot.settings;

import com.group_finity.mascot.Manager;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.pack.PackPaths;
import java.util.*;

/** Installed packs, loaded resources and a running session are separate concepts. */
public final class CharacterSession {
    public static final String RESTORE="RestorePreviousCharacters", HISTORY="PreviousCharacters";
    private CharacterSession(){ }
    /** Keep duplicate IDs: two running instances should restore as two instances. */
    public static List<String> snapshot(Manager manager) {
        synchronized(manager.getMascots()) {
            Set<Mascot> instances=new LinkedHashSet<>(manager.getMascots());instances.addAll(manager.getAdded());
            return instances.stream().filter(m->m.getManager()==manager).map(Mascot::getImageSet).toList();
        }
    }
    public static List<String> restore(Properties settings,boolean firstRun) {
        if(firstRun||!Boolean.parseBoolean(settings.getProperty(RESTORE,"false")))return List.of();
        // Never interpret legacy ActiveShimeji (previously a loaded-pack list) as a restore request.
        int limit=(int)RuntimeSettings.number(settings,"MaxCharacters",20,1,100);
        List<String> ids=new ArrayList<>();
        for(String value:settings.getProperty(HISTORY,"").split("/")) {
            if(ids.size()>=limit)break;
            try{ids.add(PackPaths.id(value.trim()));}catch(IllegalArgumentException ignored){ }
        }
        return List.copyOf(ids);
    }
}
