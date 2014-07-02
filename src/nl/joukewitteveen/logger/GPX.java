package nl.joukewitteveen.logger;

import java.io.*;
import java.util.*;

import javax.microedition.lcdui.*;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.location.QualifiedCoordinates;

import nl.joukewitteveen.util.*;
import nl.joukewitteveen.trainer.*;
import nl.joukewitteveen.sensor.HeartRate;
import nl.joukewitteveen.sensor.HeartRate.HRMonitor;

public class GPX implements HRMonitor, CommandListener {
	private static final int HR_MAX_USE = 3;
	private static boolean activeSegment = false;
	private static boolean trkIsEmpty = true;
	private static int heartRate = -1;
	private static int heartRateUseCount = 0;
	private static Calendar calendar = Calendar.getInstance();
	private static FileConnection file;
	private static PrintStream writer = null;

	public GPX() {
		if(writer == null) {
			HeartRate.addMonitor(this);
			open();
		}
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private static synchronized void open() {
		try {
			do {
				file = (FileConnection) Connector.open(Settings.Values.completePath() + now() + ".gpx");
			} while(file.exists());
			AppLog.log("Creating " + file.getName());
			file.create();
		} catch (Exception e) {
			AppLog.log("> " + e.getMessage());
			return;
		}
		try {
			writer = new PrintStream(file.openOutputStream());
		} catch (Exception e) {
			AppLog.log("> " + e.getMessage());
			AppLog.log("Deleting " + file.getName());
			try {
				file.delete();
			} catch (Exception f) {
				AppLog.log(">> " + f.getMessage());
			}
			return;
		}
		writer.println("<?xml version='1.0' ?>");
		writer.println("<gpx xmlns='http://www.topografix.com/GPX/1/1' xmlns:gpxtpx='http://www.garmin.com/xmlschemas/TrackPointExtension/v2' creator='Train2ME-" + Settings.Values.version + "' version='1.1' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v2 http://www.garmin.com/xmlschemas/TrackPointExtensionv2.xsd'>");
		writer.println("<trk>");
	}

	private static synchronized void close() {
		if(trkIsEmpty) {
			AppLog.log("Deleting unused " + file.getName());
			try {
				file.delete();
			} catch (IOException e) {
				AppLog.log("> " + e.getMessage());
			}
		} else {
			writer.println("</trk>");
			writer.println("</gpx>");
			writer.close();
		}
	}

	private static String now() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.YEAR) + "-" + StringUtil.twoDigits(calendar.get(Calendar.MONTH) + 1) + "-" + StringUtil.twoDigits(calendar.get(Calendar.DAY_OF_MONTH)) + "_"
				+ StringUtil.twoDigits(calendar.get(Calendar.HOUR_OF_DAY)) + StringUtil.twoDigits(calendar.get(Calendar.MINUTE)) + StringUtil.twoDigits(calendar.get(Calendar.SECOND));
	}

	private static String dateTime(long time) {
		calendar.setTime(new Date(time));
		return calendar.get(Calendar.YEAR) + "-" + StringUtil.twoDigits(calendar.get(Calendar.MONTH) + 1) + "-" + StringUtil.twoDigits(calendar.get(Calendar.DAY_OF_MONTH)) + "T"
				+ StringUtil.twoDigits(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + StringUtil.twoDigits(calendar.get(Calendar.MINUTE)) + ":" + StringUtil.twoDigits(calendar.get(Calendar.SECOND)) + "." + StringUtil.twoDigits(calendar.get(Calendar.MILLISECOND) / 10) + "Z";
	}

	public static synchronized void startSegment() {
		if(writer == null || activeSegment == true) {
			return;
		}
		writer.println("\t<trkseg>");
		activeSegment = true;
		trkIsEmpty = false;
	}

	public static synchronized void stopSegment() {
		if(writer == null || activeSegment == false) {
			return;
		}
		writer.println("\t</trkseg>");
		writer.flush();
		activeSegment = false;
	}

	public static synchronized void log(QualifiedCoordinates position, long time) {
		if(activeSegment){
			writer.println("\t\t<trkpt lat='" + position.getLatitude() + "' lon='" + position.getLongitude() + "'>");
			if(position.getAltitude() != Float.NaN) {
				writer.println("\t\t\t<ele>" + position.getAltitude() + "</ele>");
			}
			writer.println("\t\t\t<time>" + dateTime(time) + "</time>");
			if(heartRate >= 0 && heartRateUseCount < HR_MAX_USE) {
				heartRateUseCount++;
				writer.println("\t\t\t<extensions><gpxtpx:TrackPointExtension>");
				writer.println("\t\t\t\t<gpxtpx:hr>" + heartRate + "</gpxtpx:hr>");
				writer.println("\t\t\t</gpxtpx:TrackPointExtension></extensions>");
			}
			writer.println("\t\t</trkpt>");
		}
	}

	public synchronized void heartRateUpdate(int _heartRate) {
		heartRate = _heartRate;
		heartRateUseCount = 0;
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == Dashboard.exitCommand) {
			GPX.stopSegment();
			GPX.close();
			HeartRate.removeMonitor(this);
		} else if(command == Training.pauseCommand) {
			GPX.stopSegment();
		} else if(command == Training.resumeCommand) {
			GPX.startSegment();
		}
	}
}
