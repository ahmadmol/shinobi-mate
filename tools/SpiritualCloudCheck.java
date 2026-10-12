import com.group_finity.mascot.cloud.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** Reproducible checks against the built JAR, including exact source equality and Arabic rendering. */
public class SpiritualCloudCheck {
    static int checks;
    static void require(boolean condition, String reason) {
        checks++;
        if (!condition) throw new AssertionError(reason);
    }
    static void states(List<String> messages) {
        CloudState a = new CloudState(messages, new Random(10));
        CloudState b = new CloudState(messages, new Random(20));
        require(a.visibility() == CloudState.Visibility.HIDDEN, "initial state");
        a.mouseEnter(1000); a.update(1180);
        require(a.visibility() == CloudState.Visibility.HOVER_VISIBLE && a.opacity() == 1, "hover fade in");
        a.mouseExit(); a.update(1360);
        require(a.visibility() == CloudState.Visibility.HIDDEN && a.opacity() == 0, "exit fade out");
        a.mouseEnter(2000); a.click(2020); a.mouseExit(); a.update(2200);
        require(a.visibility() == CloudState.Visibility.PINNED && a.opacity() == 1, "pin survives exit");
        require(b.visibility() == CloudState.Visibility.HIDDEN, "independent same-pack instance");
        String old = a.message(); a.update(7999);
        require(a.message().equals(old), "no premature message change");
        a.update(8000); a.update(8180); a.update(8360);
        require(!a.message().equals(old) && a.opacity() == 1, "six seconds plus fade transition");
        for (int i=0; i<500; i++) {
            old = a.message();
            long time = 15000L + i*6200L;
            a.update(time); a.update(time+180); a.update(time+360);
            require(!a.message().equals(old), "immediate repetition");
        }
        a.click(4_000_000); a.update(4_000_180); a.mouseEnter(4_000_200);
        require(a.visibility() == CloudState.Visibility.HIDDEN, "second click remains hidden under pointer");
        a.mouseExit(); a.mouseEnter(4_000_300);
        require(a.visibility() == CloudState.Visibility.HOVER_VISIBLE, "real re-enter re-enables hover");
        require(!new CloudState(List.of(), new Random()).isEnabled(), "missing pack data disables feature");
        require(!new CloudState(List.of(messages.getFirst()), new Random()).isEnabled(), "singleton does not violate repetition");
        ClickGesture gesture = new ClickGesture();
        gesture.press(new Point(100,100)); gesture.move(new Point(103,102));
        require(gesture.release(new Point(101,101)), "tiny jitter still a click");
        gesture.press(new Point(100,100)); gesture.move(new Point(106,100));
        require(!gesture.release(new Point(100,100)), "drag returning to press location remains a drag");
        gesture.press(new Point(100,100));
        require(!gesture.release(new Point(100,107)), "fast press-release displacement is a drag");
    }
    static void placement() {
        List<Rectangle> monitors = List.of(new Rectangle(-1920,-200,1920,1080),
                new Rectangle(0,0,1920,1040), new Rectangle(2400,200,1280,720));
        Random random = new Random(0);
        for (int i=0;i<10000;i++) {
            Rectangle mascot = new Rectangle(random.nextInt(6000)-2100,random.nextInt(1600)-400,128,128);
            Rectangle monitor = CloudPlacement.monitorFor(mascot,monitors);
            Rectangle cloud = CloudPlacement.place(mascot,new Dimension(160+random.nextInt(181),84+random.nextInt(60)),monitor);
            require(monitor.contains(cloud),"cloud escaped a monitor: "+cloud);
        }
        Rectangle top = new Rectangle(500,0,128,128);
        require(CloudPlacement.place(top,new Dimension(300,110),monitors.get(1)).y==136,"top edge fallback below");
        require(CloudPlacement.monitorFor(new Rectangle(-1000,100,128,128),monitors).equals(monitors.getFirst()),"negative coordinates");
        require(CloudPlacement.monitorFor(new Rectangle(2400,220,128,128),monitors).equals(monitors.get(2)),"monitor across gap");
    }
    @SuppressWarnings("unchecked")
    static void render(List<String> messages) throws Exception {
        BufferedImage gallery = new BufferedImage(1780,660,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gallery.createGraphics();g.setColor(new Color(235,244,249));g.fillRect(0,0,1780,660);
        Field field = CloudPanel.class.getDeclaredField("lines");field.setAccessible(true);
        int index=0;
        for(String message:messages) {
            CloudPanel panel = new CloudPanel();panel.setMessage(message,1920);panel.setOpacity(1);
            Dimension size = panel.getPreferredSize();panel.setSize(size);
            require(size.width>=160 && size.width<=340,"variable width");
            List<TextLayout> lines=(List<TextLayout>)field.get(panel);
            require(!lines.isEmpty() && lines.size()<=3,"supplied message wraps to more than three lines");
            require(!panel.getComponentOrientation().isLeftToRight(),"RTL orientation");
            require(panel.getAccessibleContext().getAccessibleName().equals(message),"accessible text changed");
            for(TextLayout line:lines) {
                require(!line.isLeftToRight(),"RTL text layout");
                require(line.getVisibleAdvance()<=size.width-72+1,"line exceeds text area: "+message);
            }
            BufferedImage image=new BufferedImage(size.width,size.height,BufferedImage.TYPE_INT_ARGB);
            Graphics2D draw=image.createGraphics();panel.paint(draw);draw.dispose();
            require((image.getRGB(0,0)>>>24)==0,"cloud corners are transparent");
            require((image.getRGB(size.width/2,size.height/2)>>>24)>230,"soft white center");
            // Twenty representative source entries spanning all sections and lengths.
            if(index%5==0) { int sample=index/5;g.drawImage(image, sample%5*356+8,sample/5*165+12,null); }
            index++;
        }
        g.dispose();ImageIO.write(gallery,"png",Path.of("output","spiritual-cloud-gallery.png").toFile());
    }
    public static void main(String[] args) throws Exception {
        try {
            String source=Files.readString(Path.of("art/spiritual-cloud/shinobimate_spiritual_messages_100.md"),StandardCharsets.UTF_8);
            Matcher match=Pattern.compile("(?m)^(\\d+)\\. \\*\\*\\[[^\\]]+\\]\\*\\* (.+)\\r?$").matcher(source);
            List<String> expected=new ArrayList<>();while(match.find())expected.add(match.group(2).replaceAll("\\r$",""));
            require(expected.size()==100,"source count");
            Set<String> union=new HashSet<>();
            String[] packs={"NarutoShippuden","SasukeShippuden","GojoSatoru","SukunaRyomen","AstaBlackClover"};
            for(int pack=0;pack<5;pack++) {
                List<String> loaded=CloudMessages.load(packs[pack]);require(loaded.size()==20,"pack count "+packs[pack]);
                for(int i=0;i<20;i++) {
                    require(loaded.get(i).equals(expected.get(pack+5*i)),"verbatim source equality "+packs[pack]+" message "+(i+1));
                    require(union.add(loaded.get(i)),"duplicate across packs");
                }
            }
            require(CloudMessages.load("AbsentFuturePack").isEmpty(),"dynamic absent pack");
            Path temporary=Files.createTempDirectory("shinobimate-cloud-check-");
            Path file=temporary.resolve("conf/cloud_messages_ar.properties");Files.createDirectories(file.getParent());
            Files.writeString(file,"message.2="+expected.get(1)+"\nmessage.1="+expected.get(0)+"\n",StandardCharsets.UTF_8);
            require(CloudMessages.load(file).equals(expected.subList(0,2)),"arbitrary future pack, numeric order, UTF-8");
            Files.delete(file);Files.delete(file.getParent());Files.delete(temporary);
            states(expected.subList(0,20));placement();
            SwingUtilities.invokeAndWait(()->{try {render(expected);}catch(Exception e){throw new RuntimeException(e);}});
            System.out.println("PASS "+checks+" assertions: 100 exact source texts, 5 disjoint packs, hover/pin, 6s/fade, no repetitions, drag threshold, 10000 monitor placements, all 100 Arabic layouts.");
            System.exit(0);
        } catch(Throwable error) {error.printStackTrace();System.exit(1);}
    }
}
