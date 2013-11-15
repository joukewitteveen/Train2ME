package nl.joukewitteveen.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;

public class ToneUtil {
	public static final ToneType INFO = new ToneType(95, 250, 100),
			ALARM = new ToneType(100, 375, 100),
			WARNING = new ToneType(100, 250, 100);
	static Timer timer = new Timer();
	private static long notBefore;
	static class ToneType {
		int note, duration, volume;

		public ToneType(int note, int duration, int volume) {
			this.note = note;
			this.duration = duration;
			this.volume = volume;
		}
	}

	public static void setBlocked(boolean block) {
		notBefore = block ? Long.MAX_VALUE : 0;
	}

	public static boolean play(ToneType type) {
		try {
			Manager.playTone(type.note, type.duration, type.volume);
			return true;
		} catch (MediaException e) {
			return false;
		}
	}

	public static synchronized void playBlocking(final ToneType type, final int repeat, long between, long block) {
		long now = (new Date()).getTime();
		if(now < notBefore) {
			return;
		}
		notBefore = now + block;
		timer.scheduleAtFixedRate(new TimerTask() {
				private int remaining = repeat;

				public void run() {
					play(type);
					if(--remaining <= 0) {
						cancel();
					}
				}
			}, 0L, between);
	}
}
