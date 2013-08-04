package nl.joukewitteveen.providers;

import java.util.*;

import nl.joukewitteveen.util.StringUtil;
import nl.joukewitteveen.trainer.*;
import nl.joukewitteveen.Provider;

public class Text extends Provider {
	private static Calendar calendar = Calendar.getInstance();
	private String text;

	public Text(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
		text = args.hasMoreElements() ? (String) args.nextElement() : null;
	}

	public void run() {
		if(text != null) {
			region.writeText(text, true);
		} else {
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
}
