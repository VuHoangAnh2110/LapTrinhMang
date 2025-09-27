package client.fx.utils;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Audio management system for game sounds and background music
 * Supports sound effects, background music, and volume control
 */
public class AudioPlayer {
    private static AudioPlayer instance;
    
    // Audio configuration
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private double masterVolume = 0.8;
    private double soundVolume = 1.0;
    private double musicVolume = 0.6;
    
    // Audio storage
    private Map<String, AudioClip> soundEffects;
    private Map<String, MediaPlayer> musicPlayers;
    private MediaPlayer currentBackgroundMusic;
    
    // Sound effect names
    public static final String SOUND_ATTACK = "attack";
    public static final String SOUND_HIT = "hit";
    public static final String SOUND_DEATH = "death";
    public static final String SOUND_RESPAWN = "respawn";
    public static final String SOUND_VICTORY = "victory";
    public static final String SOUND_DEFEAT = "defeat";
    public static final String SOUND_BUTTON_CLICK = "button_click";
    public static final String SOUND_JOIN_ROOM = "join_room";
    public static final String SOUND_LEAVE_ROOM = "leave_room";
    public static final String SOUND_GAME_START = "game_start";
    public static final String SOUND_COUNTDOWN = "countdown";
    
    // Music names
    public static final String MUSIC_BACKGROUND = "background";
    public static final String MUSIC_MENU = "menu";
    public static final String MUSIC_GAME = "game";
    public static final String MUSIC_VICTORY = "victory_music";
    
    private AudioPlayer() {
        this.soundEffects = new ConcurrentHashMap<>();
        this.musicPlayers = new ConcurrentHashMap<>();
        loadAudioFiles();
    }
    
    public static synchronized AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }
    
    private void loadAudioFiles() {
        try {
            // Load sound effects (using short audio clips for quick playback)
            loadSoundEffect(SOUND_ATTACK, "/sounds/attack.wav");
            loadSoundEffect(SOUND_HIT, "/sounds/hit.wav");
            loadSoundEffect(SOUND_DEATH, "/sounds/death.wav");
            loadSoundEffect(SOUND_RESPAWN, "/sounds/respawn.wav");
            loadSoundEffect(SOUND_VICTORY, "/sounds/victory.wav");
            loadSoundEffect(SOUND_DEFEAT, "/sounds/defeat.wav");
            loadSoundEffect(SOUND_BUTTON_CLICK, "/sounds/button_click.wav");
            loadSoundEffect(SOUND_JOIN_ROOM, "/sounds/join_room.wav");
            loadSoundEffect(SOUND_LEAVE_ROOM, "/sounds/leave_room.wav");
            loadSoundEffect(SOUND_GAME_START, "/sounds/game_start.wav");
            loadSoundEffect(SOUND_COUNTDOWN, "/sounds/countdown.wav");
            
            // Load background music (using MediaPlayer for longer tracks)
            loadMusic(MUSIC_BACKGROUND, "/sounds/background.mp3");
            loadMusic(MUSIC_MENU, "/sounds/menu.mp3");
            loadMusic(MUSIC_GAME, "/sounds/game.mp3");
            loadMusic(MUSIC_VICTORY, "/sounds/victory_music.mp3");
            
        } catch (Exception e) {
            System.err.println("Error loading audio files: " + e.getMessage());
        }
    }
    
    private void loadSoundEffect(String name, String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toString());
                soundEffects.put(name, clip);
                System.out.println("Loaded sound effect: " + name);
            } else {
                System.err.println("Sound effect not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading sound effect " + name + ": " + e.getMessage());
        }
    }
    
    private void loadMusic(String name, String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                Media media = new Media(resource.toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setCycleCount(MediaPlayer.INDEFINITE); // Loop by default
                musicPlayers.put(name, player);
                System.out.println("Loaded music: " + name);
            } else {
                System.err.println("Music file not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading music " + name + ": " + e.getMessage());
        }
    }
    
    // Sound effect methods
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        AudioClip clip = soundEffects.get(soundName);
        if (clip != null) {
            try {
                clip.setVolume(masterVolume * soundVolume);
                clip.play();
            } catch (Exception e) {
                System.err.println("Error playing sound " + soundName + ": " + e.getMessage());
            }
        }
    }
    
    public void playSoundWithVolume(String soundName, double volume) {
        if (!soundEnabled) return;
        
        AudioClip clip = soundEffects.get(soundName);
        if (clip != null) {
            try {
                clip.setVolume(masterVolume * soundVolume * volume);
                clip.play();
            } catch (Exception e) {
                System.err.println("Error playing sound " + soundName + ": " + e.getMessage());
            }
        }
    }
    
    // Background music methods
    public void playMusic(String musicName) {
        if (!musicEnabled) return;
        
        stopCurrentMusic();
        
        MediaPlayer player = musicPlayers.get(musicName);
        if (player != null) {
            try {
                player.setVolume(masterVolume * musicVolume);
                player.seek(Duration.ZERO);
                player.play();
                currentBackgroundMusic = player;
            } catch (Exception e) {
                System.err.println("Error playing music " + musicName + ": " + e.getMessage());
            }
        }
    }
    
    public void stopCurrentMusic() {
        if (currentBackgroundMusic != null) {
            try {
                currentBackgroundMusic.stop();
            } catch (Exception e) {
                System.err.println("Error stopping current music: " + e.getMessage());
            }
        }
    }
    
    public void pauseCurrentMusic() {
        if (currentBackgroundMusic != null && currentBackgroundMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            try {
                currentBackgroundMusic.pause();
            } catch (Exception e) {
                System.err.println("Error pausing current music: " + e.getMessage());
            }
        }
    }
    
    public void resumeCurrentMusic() {
        if (currentBackgroundMusic != null && currentBackgroundMusic.getStatus() == MediaPlayer.Status.PAUSED) {
            try {
                currentBackgroundMusic.play();
            } catch (Exception e) {
                System.err.println("Error resuming current music: " + e.getMessage());
            }
        }
    }
    
    // Game-specific audio methods
    public void playAttackSound() {
        playSound(SOUND_ATTACK);
    }
    
    public void playHitSound() {
        playSound(SOUND_HIT);
    }
    
    public void playDeathSound() {
        playSound(SOUND_DEATH);
    }
    
    public void playRespawnSound() {
        playSound(SOUND_RESPAWN);
    }
    
    public void playVictorySound() {
        playSound(SOUND_VICTORY);
    }
    
    public void playDefeatSound() {
        playSound(SOUND_DEFEAT);
    }
    
    public void playButtonClickSound() {
        playSoundWithVolume(SOUND_BUTTON_CLICK, 0.5);
    }
    
    public void playGameStartSound() {
        playSound(SOUND_GAME_START);
    }
    
    public void playCountdownSound() {
        playSound(SOUND_COUNTDOWN);
    }
    
    public void startMenuMusic() {
        playMusic(MUSIC_MENU);
    }
    
    public void startGameMusic() {
        playMusic(MUSIC_GAME);
    }
    
    public void startVictoryMusic() {
        playMusic(MUSIC_VICTORY);
    }
    
    // Volume control methods
    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
        updateAllVolumes();
    }
    
    public void setSoundVolume(double volume) {
        this.soundVolume = Math.max(0.0, Math.min(1.0, volume));
        updateSoundVolumes();
    }
    
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        updateMusicVolumes();
    }
    
    private void updateAllVolumes() {
        updateSoundVolumes();
        updateMusicVolumes();
    }
    
    private void updateSoundVolumes() {
        // Sound effects volume is applied when played
        // No need to update existing clips
    }
    
    private void updateMusicVolumes() {
        if (currentBackgroundMusic != null) {
            currentBackgroundMusic.setVolume(masterVolume * musicVolume);
        }
    }
    
    // Enable/Disable methods
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopCurrentMusic();
        }
    }
    
    public void setAllAudioEnabled(boolean enabled) {
        setSoundEnabled(enabled);
        setMusicEnabled(enabled);
    }
    
    // Fade effects
    public void fadeOutCurrentMusic(double durationSeconds) {
        if (currentBackgroundMusic != null) {
            // Simple fade implementation
            double currentVolume = currentBackgroundMusic.getVolume();
            double steps = 20;
            double volumeStep = currentVolume / steps;
            double timeStep = (durationSeconds * 1000) / steps;
            
            new Thread(() -> {
                try {
                    for (int i = 0; i < steps; i++) {
                        double newVolume = currentVolume - (volumeStep * (i + 1));
                        currentBackgroundMusic.setVolume(Math.max(0, newVolume));
                        Thread.sleep((long) timeStep);
                    }
                    stopCurrentMusic();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    public void fadeInMusic(String musicName, double durationSeconds) {
        if (!musicEnabled) return;
        
        MediaPlayer player = musicPlayers.get(musicName);
        if (player != null) {
            stopCurrentMusic();
            currentBackgroundMusic = player;
            
            double targetVolume = masterVolume * musicVolume;
            double steps = 20;
            double volumeStep = targetVolume / steps;
            double timeStep = (durationSeconds * 1000) / steps;
            
            player.setVolume(0);
            player.seek(Duration.ZERO);
            player.play();
            
            new Thread(() -> {
                try {
                    for (int i = 0; i < steps; i++) {
                        double newVolume = volumeStep * (i + 1);
                        player.setVolume(Math.min(targetVolume, newVolume));
                        Thread.sleep((long) timeStep);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    // Cleanup method
    public void shutdown() {
        stopCurrentMusic();
        
        for (MediaPlayer player : musicPlayers.values()) {
            try {
                player.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing music player: " + e.getMessage());
            }
        }
        
        soundEffects.clear();
        musicPlayers.clear();
    }
    
    // Getters
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public double getMasterVolume() { return masterVolume; }
    public double getSoundVolume() { return soundVolume; }
    public double getMusicVolume() { return musicVolume; }
    
    public boolean isPlayingMusic() {
        return currentBackgroundMusic != null && 
               currentBackgroundMusic.getStatus() == MediaPlayer.Status.PLAYING;
    }
    
    public String getCurrentMusicName() {
        if (currentBackgroundMusic == null) return null;
        
        for (Map.Entry<String, MediaPlayer> entry : musicPlayers.entrySet()) {
            if (entry.getValue() == currentBackgroundMusic) {
                return entry.getKey();
            }
        }
        return null;
    }
}