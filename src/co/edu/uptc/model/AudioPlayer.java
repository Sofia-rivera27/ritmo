package co.edu.uptc.model;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Reproduce y controla archivos de audio: reproducir, pausar, continuar, detener, adelantar y volumen.
 *
 * @author SofiaValentinaG
 */
public class AudioPlayer implements Runnable {

    private static final int MICROSECONDS_PER_SECOND = 1_000_000;
    private static final int MAX_VOLUME = 100;

    /** Clip de audio que reproduce la canción actual. */
    private Clip clip;
    private Song currentSong;
    private int volume = 50;

    /**
     * Reproduce una canción desde su archivo. Si ya hay una canción
     * sonando, primero la detiene.
     *
     * @param song canción que se desea reproducir
     * @throws Exception si ocurre un error al cargar o reproducir el audio
     */
    public void play(Song song) throws Exception {
        stop();

        File file = new File(song.getFilePath());
        AudioInputStream audio = AudioSystem.getAudioInputStream(file);

        clip = AudioSystem.getClip();
        clip.open(audio);

        currentSong = song;
        clip.start();
    }

    /**
     * Pausa la reproducción.
     */
    public void pause() {
        if (clip != null) {
            clip.stop();
        }
    }

    /**
     * Reanuda la reproducción.
     */
    public void resume() {
        if (clip != null) {
            clip.start();
        }
    }

    /**
     * Detiene la canción y libera los recursos del reproductor.
     */
    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        currentSong = null;
    }

    /**
     * Alterna entre pausar y continuar la reproducción.
     */
    public void togglePause() {
        if (isPlaying()) {
            pause();
        } else {
            resume();
        }
    }

    /**
     * Modifica el volumen.
     *
     * @param volume nuevo volumen entre 0 y 100
     */
    public void setVolume(int volume) {
        this.volume = volume;

        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            if (volume == 0) {
                gainControl.setValue(gainControl.getMinimum());
            } else {
                // Convierte el volumen de 0 a 100 en decibeles
                float decibels = (float) (20 * Math.log10(volume / (double) MAX_VOLUME));
                gainControl.setValue(decibels);
            }
        }
    }

    /**
     * Obtiene el volumen actual.
     *
     * @return volumen actual entre 0 y 100
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Obtiene la posición actual de reproducción.
     *
     * @return progreso de la canción en segundos
     */
    public int getProgress() {
        if (clip != null) {
            return (int) (clip.getMicrosecondPosition() / MICROSECONDS_PER_SECOND);
        }
        return 0;
    }

    /**
     * Obtiene la duración total de la canción.
     *
     * @return duración de la canción en segundos
     */
    public int getDuration() {
        if (clip != null) {
            return (int) (clip.getMicrosecondLength() / MICROSECONDS_PER_SECOND);
        }
        return 0;
    }

    /**
     * Cambia la posición de reproducción.
     *
     * @param seconds segundo al que se desea avanzar
     */
    public void seek(int seconds) {
        if (clip != null) {
            clip.setMicrosecondPosition((long) seconds * MICROSECONDS_PER_SECOND);
        }
    }

    /**
     * Comprueba si hay una canción reproduciéndose.
     *
     * @return true si está reproduciendo, false en caso contrario
     */
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    /**
     * Mantiene vivo el hilo mientras el clip esté abierto, revisando cada segundo.
     */
    @Override
    public void run() {
        while (clip != null && clip.isOpen()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}