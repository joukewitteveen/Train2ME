package nl.joukewitteveen.providers.movement;

import java.io.*;
import java.util.*;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.location.QualifiedCoordinates;

import nl.joukewitteveen.util.*;

public class GPX implements CommandListener {
	private static boolean activeSegment = false;
	private static Calendar calendar = Calendar.getInstance();
	private static PrintStream writer = null;

	public GPX() {
		if(writer == null) {
			open();
		}
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private static synchronized void open() {
		FileConnection file;
		try {
			do {
				file = (FileConnection) Connector.open(Settings.Values.completePath() + now() + ".gpx");
			} while(file.exists());
			file.create();
			AppLog.log("Created " + file.getName());
		} catch (Exception e) {
			AppLog.log("> " + e.getMessage());
			return;
		}
		try {
			writer = new PrintStream(file.openOutputStream());
		} catch (Exception e) {
			AppLog.log("> " + e.getMessage());
			try {
				file.delete();
			} catch (Exception f) {
				AppLog.log(">> " + f.getMessage());
			}
			return;
		}
		writer.println("<?xml version='1.0' ?>");
		writer.println("<gpx xmlns='http://www.topografix.com/GPX/1/1' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd' version='1.1' creator='Train2ME-" + Settings.Values.version + "'>");
		writer.println("<trk>");
	}

	private static synchronized void close() {
		writer.println("</trk>");
		writer.println("</gpx>");
		writer.close();
	}

	private static String now() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.YEAR) + "-" + StringUtil.twoDigits(calendar.get(Calendar.MONTH)) + "-" + StringUtil.twoDigits(calendar.get(Calendar.DAY_OF_MONTH)) + "_"
				+ StringUtil.twoDigits(calendar.get(Calendar.HOUR_OF_DAY)) + StringUtil.twoDigits(calendar.get(Calendar.MINUTE)) + StringUtil.twoDigits(calendar.get(Calendar.SECOND));
	}
	
	private static String dateTime(long time) {
		calendar.setTime(new Date(time));
		return calendar.get(Calendar.YEAR) + "-" + StringUtil.twoDigits(calendar.get(Calendar.MONTH)) + "-" + StringUtil.twoDigits(calendar.get(Calendar.DAY_OF_MONTH)) + "T"
				+ StringUtil.twoDigits(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + StringUtil.twoDigits(calendar.get(Calendar.MINUTE)) + ":" + StringUtil.twoDigits(calendar.get(Calendar.SECOND)) + "." + StringUtil.twoDigits(calendar.get(Calendar.MILLISECOND) / 10) + "Z";
	}

	public static synchronized void startSegment() {
		if(writer == null || activeSegment == true) {
			return;
		}
		writer.println("\t<trkseg>");
		activeSegment = true;
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
			writer.println("\t\t</trkpt>");
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		if(displayable == null) {
			GPX.stopSegment();
			GPX.close();
		} else if(command != null) {
			String label = command.getLabel();
			if(label.equals("Pause")) {
				GPX.stopSegment();
			} else if(label.equals("Resume")) {
				GPX.startSegment();
			}
		}
	}
}
