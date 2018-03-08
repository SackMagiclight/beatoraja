package bms.player.beatoraja.audio;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandleStream;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * libGDX Sound(OpenAL)サウンドドライバ
 *
 * @author exch
 */
public class GdxSoundDriver extends AbstractAudioDriver<Sound> {

	private SoundMixer mixer;

	private final boolean soundthread = false;

	private SoundInstance[] sounds = new SoundInstance[256];
	private int soundPos = 0;

	public GdxSoundDriver() {
		for (int i = 0; i < sounds.length; i++) {
			sounds[i] = new SoundInstance();
		}

		if(soundthread) {
			mixer = new SoundMixer();
			mixer.start();
		}
	}

	@Override
	protected Sound getKeySound(Path p) {
		String name = p.toString();
		final int index = name.lastIndexOf('.');
		if (index !=-1 ) {
			name = name.substring(0, index);
		}
		final Path wavfile = Paths.get(name + ".wav");

		if (Files.exists(wavfile)) {
			try {
				return Gdx.audio.newSound(new PCMHandleStream(wavfile));
			} catch (GdxRuntimeException e) {
				Logger.getGlobal().warning("音源(wav)ファイル読み込み失敗。" + e.getMessage());
//				e.printStackTrace();
			}
		}
		final Path flacfile = Paths.get(name + ".flac");
		if (Files.exists(flacfile)) {
			try {
				return Gdx.audio.newSound(new PCMHandleStream(flacfile));
			} catch (GdxRuntimeException e) {
				Logger.getGlobal().warning("音源(flac)ファイル読み込み失敗。" + e.getMessage());
//				e.printStackTrace();
			}
		}
		final Path oggfile = Paths.get(name + ".ogg");
		if (Files.exists(oggfile)) {
			try {
				return Gdx.audio.newSound(Gdx.files.internal(oggfile.toString()));
			} catch (GdxRuntimeException e) {
				Logger.getGlobal().warning("音源(ogg)ファイル読み込み失敗。" + e.getMessage());
				// e.printStackTrace();
			}
		}
		final Path mp3file = Paths.get(name + ".mp3");
		if (Files.exists(mp3file)) {
			try {
				return Gdx.audio.newSound(Gdx.files.internal(mp3file.toString()));
			} catch (GdxRuntimeException e) {
				Logger.getGlobal().warning("音源(mp3)ファイル読み込み失敗。" + e.getMessage());
				// e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected Sound getKeySound(final PCM pcm) {
		return Gdx.audio.newSound(new FileHandleStream("tempwav.wav") {
			@Override
			public InputStream read() {
				return pcm.getInputStream();
			}

			@Override
			public OutputStream write(boolean overwrite) {
				return null;
			}
		});
	}

	private Object lock = new Object();
	
	
	
	@Override
	protected void play(Sound pcm, int channel, float volume, float pitch) {
		if(soundthread) {
			mixer.put(pcm, channel, volume, getGlobalPitch() * pitch);
		} else {
			synchronized (lock) {
				sounds[soundPos].sound = pcm;
				sounds[soundPos].id = pcm.play(volume, getGlobalPitch() * pitch, 0);
				sounds[soundPos].channel = channel;
				soundPos = (soundPos + 1) % sounds.length;
			}
		}
	}

	@Override
	protected void play(AudioElement<Sound> id, float volume, boolean loop) {
		if(loop) {
			id.id = id.audio.loop(volume);
		} else {
			id.id = id.audio.play(volume);
		}		
	}
	
	@Override
	protected void setVolume(AudioElement<Sound> id, float volume) {
		id.audio.setVolume(id.id, volume);
	}
	
	@Override
	protected void stop(Sound id) {
		id.stop();
	}

	@Override
	protected void stop(Sound id, int channel) {
		if (soundthread) {
			mixer.stop(id, channel);
		} else {
			for (int i = 0; i < sounds.length; i++) {
				if (sounds[i].sound == id && sounds[i].channel == channel) {
					sounds[i].sound.stop(sounds[i].id);
					sounds[i].sound = null;
				}
			}
		}
	}

	@Override
	protected void disposeKeySound(Sound pcm) {
		pcm.dispose();
	}

	class SoundMixer extends Thread {

		private Sound[] sound = new Sound[256];
		private float[] volume = new float[256];
		private float[] pitch = new float[256];
		private int[] channels = new int[256];
		private long[] ids = new long[256];
		private int cpos;
		private int pos;

		public synchronized void put(Sound sound, int channel, float volume, float pitch) {
			this.sound[cpos] = sound;
			this.volume[cpos] = volume;
			this.pitch[cpos] = pitch;
			this.channels[cpos] = channel;
			cpos = (cpos + 1) % this.sound.length;
		}

		public synchronized void stop(Sound snd, int channel) {
			for (int i = 0; i < sound.length; i++) {
				if (sound[i] == snd && this.channels[i] == channel) {
					sound[i].stop(ids[i]);
					sound[i] = null;
				}
			}
		}

		public void run() {
			for(;;) {
				if(pos != cpos) {
					ids[pos] = sound[pos].play(this.volume[pos], getGlobalPitch() * this.pitch[pos], 0);
					pos = (pos + 1) % this.sound.length;
				} else {
					try {
						sleep(1);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	class SoundInstance {
		public Sound sound;
		public long id = -1;
		public int channel = -1;
	}
	
	private static class PCMHandleStream extends FileHandleStream {
		
		private final Path p;
		
		public PCMHandleStream(Path p) {
			super("tempwav.wav");
			this.p = p;
		}
		
		@Override
		public InputStream read() {
			try {
				PCM pcm = PCM.load(p);
				return pcm.getInputStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public OutputStream write(boolean overwrite) {
			return null;
		}
	}
}
