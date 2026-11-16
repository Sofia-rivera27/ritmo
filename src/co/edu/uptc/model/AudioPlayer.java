package co.edu.uptc.model;

import java.io.File;
import javax.sound.sampled.*;

/**
 * Clase encargada de reproducir y controlar archivos de audio. Permite
 * reproducir, pausar, continuar, detener y cambiar el volumen de las canciones.
 *
 * @author SofiaValentinaG
 */

public class AudioPlayer implements Runnable {

	private Clip clip; // esta es la clase encargada del reproductor de audio
	private Song currentSong;
	private int volume = 50;

	/**
	 * Reproduce una cancion desde su archivo.
	 *
	 * @param song cancion que se desea reproducir
	 * @throws Exception si ocurre un error al cargar o reproducir el audio
	 */
	public void play(Song song) throws Exception {
		stop();

		File file = new File(song.getPathFile());
		AudioInputStream audio = AudioSystem.getAudioInputStream(file);

		clip = AudioSystem.getClip(); // crea el reproductor
		clip.open(audio); // carga el reproductor

		currentSong = song;
		clip.start();
	}

	/**
	 * Pausa la reproduccion de la cancion.
	 */
	public void pause() {
		if (clip != null) {
			clip.stop();
		}
	}

	/**
	 * Reanuda la reproduccin de la cancion.
	 */
	public void resume() {
		if (clip != null) {
			clip.start();
		}
	}

	/**
	 * Detiene la cancion y libera los recursos del reproductor.
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
	 * Alterna entre pausar y continuar la reproduccoón.
	 */
	public void togglePause() {
		if (isPlaying()) {
			pause();
		} else {
			resume();
		}
	}

	/**
	 * Modifica el volumen de la canción.
	 *
	 * @param volume nuevo volumen entre 0 y 100
	 */
	public void setVolume(int volume) {
		this.volume = volume;

		if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			// clase para el control del volumen
			FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

			if (volume == 0) {
				control.setValue(control.getMinimum());
			} else {
				// Convierte el volumen a decibeles
				float db = (float) (20 * Math.log10(volume / 100.0));
				control.setValue(db);
			}
		}
	}

	/**
	 * Obtiene el volumen actual.
	 *
	 * @return volumen actual de la cancion
	 */

	public int getVolume() {
		return volume;
	}

	/**
	 * Obtiene la posicion actual de reproduccin.
	 *
	 * @return progreso de la canción en segundos
	 */
	public int getProgress() {
		if (clip != null) {
			return (int) (clip.getMicrosecondPosition() / 1000000);
		}
		return 0;
	}

	/**
	 * Obtiene la duracion total de la canción.
	 *
	 * @return duracion de la canción en segundos
	 */

	public int getDuration() {
		if (clip != null) {
			return (int) (clip.getMicrosecondLength() / 1000000);
		}
		return 0;
	}

	/**
	 * Cambia la posicion de reproducción de la canción.
	 *
	 * @param seconds segundo al que se desea avanzar
	 */
	public void seek(int seconds) {
		if (clip != null) {
			clip.setMicrosecondPosition(seconds * 1000000L);
		}
	}

	/**
	 * Comprueba si la canción se está reproduciendo.
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
     * Establece una accióon para ejecutar cuando termine la canción.
     *
     * @param action acción que se desea ejecutar al finalizar
     */

	@Override
	public void run() {
		while (clip != null && clip.isOpen()) {
			try {
				Thread.sleep(1000); //sirve para pausar la reproduccion
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}