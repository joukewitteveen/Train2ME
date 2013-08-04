package nl.joukewitteveen.providers.movement;

import java.util.*;

import javax.microedition.location.*;

import nl.joukewitteveen.util.AppLog;

public class Position {
	private static float totalDistance = 0;
	private static Coordinates previousPosition = null;
	private static Vector clients = new Vector();
	private static LocationProvider provider = null;
	private static final LocationListener listener = new LocationListener() {
		public void locationUpdated(LocationProvider provider, Location location) {
			Enumeration handlers = clients.elements();
			if(!location.isValid()) {
				while(handlers.hasMoreElements()) {
					((MovementHandler) handlers.nextElement()).movementUpdate(totalDistance, -1);
				}
				return;
			}
			QualifiedCoordinates position = location.getQualifiedCoordinates();
			if(position != null) {
				GPX.log(position, location.getTimestamp());
				if(previousPosition != null){
					totalDistance += position.distance(previousPosition);
				}
				previousPosition = (Coordinates) position;
			}
			while(handlers.hasMoreElements()) {
				((MovementHandler) handlers.nextElement()).movementUpdate(totalDistance, location.getSpeed());
			}
		}

		public void providerStateChanged(LocationProvider provider, int newState) {
			if(newState != LocationProvider.AVAILABLE) {
				Enumeration handlers = clients.elements();
				while(handlers.hasMoreElements()) {
					((MovementHandler) handlers.nextElement()).movementUpdate(Float.NaN, Float.NaN);
				}
			}
		}
	};

	public static float getTotalDistance() {
		return totalDistance;
	}

	public static synchronized boolean initialize() throws LocationException {
		if(provider != null)
			return false;
		Criteria criteria = new Criteria();
		criteria.setSpeedAndCourseRequired(true);
		criteria.setPreferredResponseTime(1000);
		provider = LocationProvider.getInstance(criteria);
		if(provider == null) {
			criteria.setPreferredResponseTime(Criteria.NO_REQUIREMENT);
			provider = LocationProvider.getInstance(criteria);
			if(provider == null) {
				throw new LocationException();
			}
			provider.setLocationListener(listener, -1, -1, -1);
			AppLog.log("Using GPS timing defaults");
		} else {
			provider.setLocationListener(listener, 1, 1, 1);
			AppLog.log("Using 1 second GPS timing");
		}
		return true;
	}

	public static synchronized void addHandler(MovementHandler handler) {
		clients.addElement(handler);
	}

	public static synchronized void removeHandler(MovementHandler handler) {
		clients.removeElement(handler);
	}
}
