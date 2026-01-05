// ============================================================================
// File Name: CabinV2.java
// Program Name: Ultimate Cinematic Cozy Cabin (or Cozy Cabin 2.0 for short)
// Author: Bernard
// CoAuthor: ChatGPT
// Last Updated: December 17th 2025
//
// Disclaimer:
// ChatGPT helped create the image, audio, and animations
// Everything has been hard coded by yours truly
// Thank you for reading and enjoy the code
//
// Features:
//   • Multi-layer fireplace with realistic flicker and glow
//   • Floor and window reflections of fire
//   • Drifting snow with gusts and depth
//   • Steam curls from cocoa mug
//   • Soft shadows under objects
//   • Animations synced with play/pause audio
//   • Heavy beginner-friendly comments
//
// Required Assets:
//   • CabinSnowWindow.png
//   • FireplaceLoop.wav
// ============================================================================

// import section
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

// Main class
public class CabinV2 {

    // Main string
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CabinV2().startProgram());
    }

    // Start of the program
    public void startProgram() {

        // Image frame
        JFrame frame = new JFrame("Cozy Cabin 2.0");
        frame.setSize(1375, 725);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Image
        Image cabinImage = new ImageIcon("CabinSnowWindow.png").getImage();

        // Animation panel
        AnimatedCabinPanel animatedPanel = new AnimatedCabinPanel(cabinImage);
        frame.setLayout(new BorderLayout());
        frame.add(animatedPanel, BorderLayout.CENTER);

        // Audio setup
        Clip audioClip = null;
        try {
            File audioFile = new File("FireplaceLoop.wav");
            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(stream);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.out.println("Error loading audio: " + e.getMessage());
        }
        Clip finalAudioClip = audioClip;

        // Buttons
        JButton playButton = new JButton("Play Cabin Sound");
        JButton stopButton = new JButton("Stop Cabin Sound");

        // Play button
        playButton.addActionListener(e -> {
            if (finalAudioClip != null) {
                finalAudioClip.stop();
                finalAudioClip.setFramePosition(0);
                finalAudioClip.loop(Clip.LOOP_CONTINUOUSLY);
                finalAudioClip.start();
            }
            animatedPanel.playAnimation();
        });

        // Stop button
        stopButton.addActionListener(e -> {
            if (finalAudioClip != null) finalAudioClip.stop();
            animatedPanel.stopAnimation();
        });

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Visibility
        frame.setVisible(true);
    }
}

// ============================================================================
// AnimatedCabinPanel: handles all particle animations, lights, shadows, and drawing
// ============================================================================
class AnimatedCabinPanel extends JPanel {

    // Cabin panel
    private final Image backgroundImage;
    private final Timer timer;
    private final Random rand = new Random();

    // Animation regions
    private final Rectangle FIRE_REGION = new Rectangle(5, 145, 250, 260);
    private final Rectangle WINDOW_REGION = new Rectangle(600, 100, 500, 200);
    private final Rectangle STEAM_REGION = new Rectangle(1025, 265, 120, 150);
    private final Rectangle FLOOR_REFLECTION = new Rectangle(FIRE_REGION.x, FIRE_REGION.y+FIRE_REGION.height-30, FIRE_REGION.width, 60);

    // Particle lists for animation
    private final ArrayList<FireParticle> fireParticles = new ArrayList<>();
    private final ArrayList<FireGlow> fireGlows = new ArrayList<>();
    private final ArrayList<Snowflake> snowflakes = new ArrayList<>();
    private final ArrayList<SteamParticle> steamParticles = new ArrayList<>();
    private final ArrayList<Reflection> floorReflections = new ArrayList<>();
    private final ArrayList<Reflection> windowReflections = new ArrayList<>();
    private final ArrayList<AmbientLight> ambientLights = new ArrayList<>();
    private final ArrayList<Shadow> shadows = new ArrayList<>();

    // Load animations
    public AnimatedCabinPanel(Image background) {
        this.backgroundImage = background;
        setDoubleBuffered(true);

        // Initialize all particle systems
        for (int i = 0; i < 80; i++) fireParticles.add(createFireParticle());
        for (int i = 0; i < 15; i++) fireGlows.add(createFireGlow());
        for (int i = 0; i < 150; i++) snowflakes.add(createSnowflake());
        for (int i = 0; i < 20; i++) steamParticles.add(createSteamParticle());
        for (int i = 0; i < 8; i++) floorReflections.add(createFloorReflection());
        for (int i = 0; i < 6; i++) windowReflections.add(createWindowReflection());
        for (int i = 0; i < 4; i++) ambientLights.add(createAmbientLight());
        for (int i = 0; i < 5; i++) shadows.add(createShadow());

        // Timer for animation ~33 FPS
        timer = new Timer(30, e -> {
            updateFire();
            updateFireGlow();
            updateSnow();
            updateSteam();
            updateFloorReflections();
            updateWindowReflections();
            updateAmbientLights();
            updateShadows();
            repaint();
        });
    }

    // ===================== FIRE PARTICLES =====================
    private class FireParticle { float x, y, size, speedY; Color color; }
    private FireParticle createFireParticle() {
        FireParticle p = new FireParticle();
        p.x = FIRE_REGION.x + rand.nextInt(FIRE_REGION.width);
        p.y = FIRE_REGION.y + rand.nextInt(FIRE_REGION.height);
        p.size = 10 + rand.nextInt(20);
        p.speedY = 0.5f + rand.nextFloat() * 1.5f;
        p.color = new Color(255, 100 + rand.nextInt(155), 0, 150 + rand.nextInt(100));
        return p;
    }
    // Update fire particles
    private void updateFire() {
        for (FireParticle p : fireParticles) {
            p.y -= p.speedY;
            p.x += rand.nextFloat() * 2 - 1;
            p.size *= 0.98;
            if (p.y < FIRE_REGION.y || p.size < 2) {
                p.x = FIRE_REGION.x + rand.nextInt(FIRE_REGION.width);
                p.y = FIRE_REGION.y + FIRE_REGION.height;
                p.size = 10 + rand.nextInt(20);
                p.speedY = 0.5f + rand.nextFloat() * 1.5f;
                p.color = new Color(255, 100 + rand.nextInt(155), 0, 150 + rand.nextInt(100));
            }
        }
    }

    // ===================== FIRE GLOW =====================
    private class FireGlow { float x, y, radius, alpha, growth; Color color; }
    private FireGlow createFireGlow() {
        FireGlow f = new FireGlow();
        f.x = FIRE_REGION.x + FIRE_REGION.width/2 + rand.nextInt(50)-25;
        f.y = FIRE_REGION.y + FIRE_REGION.height/2 + rand.nextInt(50)-25;
        f.radius = 50 + rand.nextInt(30);
        f.alpha = 0.2f + rand.nextFloat()*0.3f;
        f.color = new Color(255, 150+rand.nextInt(100), 0);
        f.growth = 0.5f + rand.nextFloat();
        return f;
    }
    // Update fire glow
    private void updateFireGlow() {
        for (FireGlow f : fireGlows) {
            f.radius += f.growth;
            f.alpha -= 0.005;
            if (f.alpha <= 0) fireGlows.set(fireGlows.indexOf(f), createFireGlow());
        }
    }

    // ===================== SNOW =====================
    private class Snowflake { float x, y, speedY, drift; int size; }
    private Snowflake createSnowflake() {
        Snowflake s = new Snowflake();
        s.x = WINDOW_REGION.x + rand.nextInt(WINDOW_REGION.width);
        s.y = WINDOW_REGION.y + rand.nextInt(WINDOW_REGION.height);
        s.speedY = 0.5f + rand.nextFloat()*1.5f;
        s.drift = -0.5f + rand.nextFloat();
        s.size = 2 + rand.nextInt(5);
        return s;
    }
    // Update snow
    private void updateSnow() {
        for (Snowflake s : snowflakes) {
            s.y += s.speedY;
            s.x += Math.sin(System.currentTimeMillis()*0.001 + s.x) * s.drift;
            if (rand.nextFloat() < 0.005) s.x += rand.nextInt(15)-7;
            if (s.y > WINDOW_REGION.y + WINDOW_REGION.height) {
                s.y = WINDOW_REGION.y;
                s.x = WINDOW_REGION.x + rand.nextInt(WINDOW_REGION.width);
            }
        }
    }

    // ===================== STEAM =====================
    private class SteamParticle { float x, y, alpha, riseSpeed, drift, curveOffset; }
    private SteamParticle createSteamParticle() {
        SteamParticle p = new SteamParticle();
        p.x = STEAM_REGION.x + STEAM_REGION.width/2f + rand.nextInt(20)-10;
        p.y = STEAM_REGION.y + STEAM_REGION.height;
        p.alpha = 0.1f + rand.nextFloat()*0.9f;
        p.riseSpeed = 0.5f + rand.nextFloat()*1.0f;
        p.drift = -0.2f + rand.nextFloat()*0.4f;
        p.curveOffset = rand.nextFloat()*0.5f;
        return p;
    }
    // Update steam
    private void updateSteam() {
        for (SteamParticle p : steamParticles) {
            p.y -= p.riseSpeed;
            p.x += Math.sin(p.curveOffset + p.y*0.05f) * p.drift;
            p.alpha -= 0.01f;
            if (p.alpha <= 0 || p.y < STEAM_REGION.y) resetSteam(p);
        }
    }
    // Reset steam
    private void resetSteam(SteamParticle p) {
        p.x = STEAM_REGION.x + STEAM_REGION.width/2f;
        p.y = STEAM_REGION.y + STEAM_REGION.height;
        p.alpha = 0.5f + rand.nextFloat()*0.5f;
        p.riseSpeed = 0.5f + rand.nextFloat();
        p.drift = -0.2f + rand.nextFloat()*0.4f;
        p.curveOffset = rand.nextFloat()*0.5f;
    }

    // ===================== REFLECTIONS =====================
    private class Reflection { float x, y, radius, alpha, growth; Color color; }
    private Reflection createFloorReflection() {
        Reflection r = new Reflection();
        r.x = FLOOR_REFLECTION.x + rand.nextInt(FLOOR_REFLECTION.width);
        r.y = FLOOR_REFLECTION.y + rand.nextInt(FLOOR_REFLECTION.height);
        r.radius = 30 + rand.nextInt(20);
        r.alpha = 0.1f + rand.nextFloat()*0.2f;
        r.growth = 0.3f + rand.nextFloat()*0.5f;
        r.color = new Color(255, 100+rand.nextInt(100), 0, 100);
        return r;
    }
    // Create window reflection
    private Reflection createWindowReflection() {
        Reflection r = new Reflection();
        r.x = WINDOW_REGION.x + rand.nextInt(WINDOW_REGION.width);
        r.y = WINDOW_REGION.y + rand.nextInt(WINDOW_REGION.height);
        r.radius = 20 + rand.nextInt(15);
        r.alpha = 0.05f + rand.nextFloat()*0.15f;
        r.growth = 0.2f + rand.nextFloat()*0.3f;
        r.color = new Color(255, 150+rand.nextInt(100), 0, 80);
        return r;
    }
    // Update floor reflection
    private void updateFloorReflections() {
        for (Reflection r : floorReflections) {
            r.alpha -= 0.002;
            r.radius += r.growth * 0.2;
            if (r.alpha <= 0) floorReflections.set(floorReflections.indexOf(r), createFloorReflection());
        }
    }
    // Update window reflection
    private void updateWindowReflections() {
        for (Reflection r : windowReflections) {
            r.alpha -= 0.001;
            r.radius += r.growth * 0.1;
            if (r.alpha <= 0) windowReflections.set(windowReflections.indexOf(r), createWindowReflection());
        }
    }

    // ===================== AMBIENT LIGHTS =====================
    private class AmbientLight { float x, y, radius, alpha, growth; Color color; }
    private AmbientLight createAmbientLight() {
        AmbientLight l = new AmbientLight();
        l.x = FIRE_REGION.x + rand.nextInt(FIRE_REGION.width*2);
        l.y = FIRE_REGION.y + FIRE_REGION.height/2 + rand.nextInt(50);
        l.radius = 50 + rand.nextInt(40);
        l.alpha = 0.05f + rand.nextFloat()*0.15f;
        l.growth = 0.3f + rand.nextFloat()*0.3f;
        l.color = new Color(255, 200+rand.nextInt(55), 150+rand.nextInt(55), 80);
        return l;
    }
    // Update ambient lights
    private void updateAmbientLights() {
        for (AmbientLight l : ambientLights) {
            l.radius += l.growth;
            l.alpha -= 0.002;
            if (l.alpha <= 0) ambientLights.set(ambientLights.indexOf(l), createAmbientLight());
        }
    }

    // ===================== SOFT SHADOWS =====================
    private class Shadow { float x, y, width, height, alpha; }
    private Shadow createShadow() {
        Shadow s = new Shadow();
        s.x = FIRE_REGION.x + rand.nextInt(FIRE_REGION.width);
        s.y = FIRE_REGION.y + FIRE_REGION.height - 10 + rand.nextInt(10);
        s.width = 30 + rand.nextInt(40);
        s.height = 5 + rand.nextInt(5);
        s.alpha = 0.1f + rand.nextFloat()*0.2f;
        return s;
    }
    // Update shadows
    private void updateShadows() {
        for (Shadow s : shadows) {
            s.alpha -= 0.001;
            if (s.alpha <= 0) shadows.set(shadows.indexOf(s), createShadow());
        }
    }

    // ===================== PAINT COMPONENT =====================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);

        // Floor shadows
        for (Shadow s : shadows) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, s.alpha));
            g2.setColor(new Color(0,0,0,100));
            g2.fillOval((int)s.x, (int)s.y, (int)s.width, (int)s.height);
        }

        // Floor reflections
        for (Reflection r : floorReflections) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, r.alpha));
            g2.setColor(r.color);
            g2.fillOval((int)(r.x - r.radius/2), (int)(r.y - r.radius/4), (int)r.radius, (int)(r.radius/2));
        }

        // Window reflections
        for (Reflection r : windowReflections) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, r.alpha));
            g2.setColor(r.color);
            g2.fillOval((int)(r.x - r.radius/2), (int)(r.y - r.radius/2), (int)r.radius, (int)r.radius);
        }

        // Ambient lights
        for (AmbientLight l : ambientLights) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, l.alpha));
            g2.setColor(l.color);
            g2.fillOval((int)(l.x - l.radius/2), (int)(l.y - l.radius/2), (int)l.radius, (int)l.radius);
        }

        // Fire glow
        for (FireGlow f : fireGlows) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.alpha));
            g2.setColor(f.color);
            g2.fillOval((int)(f.x - f.radius/2), (int)(f.y - f.radius/2), (int)f.radius, (int)f.radius);
        }

        // Fire particles
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        for (FireParticle p : fireParticles) {
            g2.setColor(p.color);
            g2.fillOval((int)p.x, (int)p.y, (int)p.size, (int)p.size);
        }

        // Snow animation
        g2.setColor(Color.white);
        for (Snowflake s : snowflakes) g2.fillOval((int)s.x, (int)s.y, s.size, s.size);

        // Steam animation
        for (SteamParticle p : steamParticles) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha));
            g2.setColor(new Color(230,230,230));
            g2.fillOval((int)p.x,(int)p.y,20,30);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
    }

    // Animation stop and play buttons
    public void playAnimation() { timer.start(); }
    public void stopAnimation() { timer.stop(); repaint(); }
}
