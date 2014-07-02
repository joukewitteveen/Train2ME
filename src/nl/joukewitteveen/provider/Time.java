package nl.joukewitteveen.provider;

import java.util.*;

import nl.joukewitteveen.util.StringUtil;
import nl.joukewitteveen.trainer.*;

public class Time extends Provider {
	private static Calendar calendar = Calendar.getInstance();

	public Time(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
	}

	public void run() {
		while(true) {
			calendar.setTime(new Date());
			region.writeText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + StringUtil.twoDigits(calendar.get(Calendar.MINUTE)));
			try {
				Thread.sleep(60000);
			} catch(InterruptedException e) {
				break;
			}
		}
	}
}
