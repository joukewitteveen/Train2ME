package nl.joukewitteveen.provider;

import java.util.Enumeration;

import javax.microedition.lcdui.*;

import nl.joukewitteveen.trainer.*;
import nl.joukewitteveen.util.ToneUtil;

public class HeartRate extends Provider implements nl.joukewitteveen.sensor.HeartRate.HRMonitor, CommandListener {
	private int previousHR = -1, minimumHR, maximumHR;

	public HeartRate(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
		if(args.hasMoreElements()) {
			minimumHR = Integer.parseInt((String) args.nextElement());
		} else {
			minimumHR = Integer.MIN_VALUE;
		}
		if(args.hasMoreElements()) {
			maximumHR = Integer.parseInt((String) args.nextElement());
		} else {
			maximumHR = Integer.MAX_VALUE;
		}
		nl.joukewitteveen.sensor.HeartRate.addMonitor(this);
		parent.addCommandListener(this);
	}

	public void run() {
		nl.joukewitteveen.sensor.HeartRate hr;
		region.writeText("-");
		try {
			hr = nl.joukewitteveen.sensor.HeartRate.initialize();
		} catch (Exception e) {
			region.writeText("NA");
			return;
		}
		if(hr != null) {
			parent.addCommandListener(hr);
		}
	}

	public void heartRateUpdate(int heartRate) {
		if(heartRate != nl.joukewitteveen.sensor.HeartRate.NO_BEAT &&
				(heartRate < minimumHR || heartRate > maximumHR)) {
			ToneUtil.playBlocking(ToneUtil.WARNING, 3, 375, 6500);
		}
		if(heartRate == previousHR) {
			return;
		}
		previousHR = heartRate;
		region.writeText(heartRate == nl.joukewitteveen.sensor.HeartRate.NO_BEAT ? "--" : Integer.toString(heartRate));
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == Training.nextEpochCommand) {
			parent.removeCommandListener(this);
			nl.joukewitteveen.sensor.HeartRate.removeMonitor(this);
		}
	}
}
