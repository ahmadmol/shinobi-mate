import os
import re

replacements = [
    # BehaviorBuilder
    ("private final List nextBehaviorBuilders = new ArrayList();", "private final List<BehaviorBuilder> nextBehaviorBuilders = new ArrayList<>();"),
    ("public List getNextBehaviorBuilders()", "public List<BehaviorBuilder> getNextBehaviorBuilders()"),
    ("private final List conditions = new ArrayList();", "private final List<String> conditions = new ArrayList<>();"),
    ("public List getConditions()", "public List<String> getConditions()"),

    # ActionBuilder
    ("private final List actionRefs = new ArrayList();", "private final List<IActionBuilder> actionRefs = new ArrayList<>();"),
    ("private final List animationBuilders = new ArrayList();", "private final List<AnimationBuilder> animationBuilders = new ArrayList<>();"),
    ("public List getActionRefs()", "public List<IActionBuilder> getActionRefs()"),
    ("public List getAnimationBuilders()", "public List<AnimationBuilder> getAnimationBuilders()"),
    ("Class<? extends Action> cls = Class.forName(this.getClassName());", "Class<? extends Action> cls = Class.forName(this.getClassName()).asSubclass(Action.class);"),

    # ActionBase
    ("private final List animations = new ArrayList();", "private final List<Animation> animations = new ArrayList<>();"),
    ("public List getAnimations()", "public List<Animation> getAnimations()"),

    # Mascot
    ("private final List hotspots = new ArrayList();", "private final List<Hotspot> hotspots = new ArrayList<>();"),
    ("public List getHotspots()", "public List<Hotspot> getHotspots()"),

    # Environment & ComplexArea
    ("public Collection getScreens()", "public Collection<Area> getScreens()"),
    ("protected List screens = new ArrayList();", "protected List<Area> screens = new ArrayList<>();"),
    ("private final Map areas = new LinkedHashMap();", "private final Map<String, Area> areas = new LinkedHashMap<>();"),
    ("public Map getAreas()", "public Map<String, Area> getAreas()"),

    # VariableMap
    ("public class VariableMap extends AbstractMap implements Bindings {", "public class VariableMap extends java.util.HashMap<String, Object> implements javax.script.Bindings {"),

    # MascotPackage & PackageManager
    ("private List files;", "private List<String> files;"),
    ("private List conflicts = new ArrayList();", "private List<ConflictEntry> conflicts = new ArrayList<>();"),
    ("public List getConflicts()", "public List<ConflictEntry> getConflicts()"),

    # AnimationEditor
    ("private List changeListeners = new ArrayList();", "private List<Runnable> changeListeners = new ArrayList<>();"),
    ("private List poses = new ArrayList();", "private List<AnimationPose> poses = new ArrayList<>();"),
    ("public List getPoses()", "public List<AnimationPose> getPoses()"),
    ("private List actions = new ArrayList();", "private List<AnimationAction> actions = new ArrayList<>();"),
    ("public List getActions()", "public List<AnimationAction> getActions()"),
]

root_dir = r"D:/programm/Shimeji-ee_2.0.1_Windows_Portable/src/main/java/com/group_finity/mascot"

for dirpath, _, filenames in os.walk(root_dir):
    for fn in filenames:
        if fn.endswith(".java"):
            filepath = os.path.join(dirpath, fn)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()

            orig = content
            for search, repl in replacements:
                content = content.replace(search, repl)

            if content != orig:
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(content)
                print(f"Updated: {fn}")
