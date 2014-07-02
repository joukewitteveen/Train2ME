// Support for Polar WearLink+ with Bluetooth

package nl.joukewitteveen.sensor;

import javax.bluetooth.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;

import java.io.*;
import java.util.Vector;

import nl.joukewitteveen.trainer.Dashboard;
import nl.joukewitteveen.util.AppLog;

public class HeartRate implements DiscoveryListener, CommandListener {
	public static final int NO_BEAT = -1;
	private static final byte BEAT_DETECT = 0x10;
	private static DiscoveryAgent agent;
	private static Vector monitors = new Vector();
	private volatile StreamConnection link;
	public interface HRMonitor {
		public void heartRateUpdate(int heartRate);
	}
	private class DeviceAudit implements Runnable {
		private RemoteDevice device;

		DeviceAudit(RemoteDevice btDevice) {
			this.device = btDevice;
		}

		public void run() {
			try {
				String name = device.getFriendlyName(false);
				if(name == null || name.length() == 0) {
					return;
				} else if(name.equals("Polar iWL")) {
					if(connect(device.getBluetoothAddress())) {
						agent.cancelInquiry(HeartRate.this);
						listen(link.openInputStream());
					}
				} else {
					AppLog.log("Discovered: " + name);
				}
			} catch(Exception e) {
				AppLog.log("Connecting failed");
				AppLog.log("> " + e.getMessage());
			}
		}
	}

	public static synchronized HeartRate initialize() throws BluetoothStateException {
		if(agent != null) {
			return null;
		}
		agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
		HeartRate hr = new HeartRate();
		agent.startInquiry(DiscoveryAgent.GIAC, hr);
		RemoteDevice[] devices;
		int[] lists = { DiscoveryAgent.PREKNOWN, DiscoveryAgent.CACHED };
		for(int i = 0; i < lists.length; i++) {
			devices = agent.retrieveDevices(lists[i]);
			if(devices == null) {
				continue;
			}
			for(int j = 0; j < devices.length; j++) {
				hr.deviceDiscovered(devices[j], null);
			}
		}
		return hr;
	}

	public static void addMonitor(HRMonitor monitor) {
		monitors.addElement(monitor);
	}

	public static boolean removeMonitor(HRMonitor monitor) {
		return monitors.removeElement(monitor);
	}

	private static void sendUpdate(int hr) {
		HRMonitor[] persistentMonitors = new HRMonitor[monitors.size()];
		monitors.copyInto(persistentMonitors);
		for(int i = 0; i < persistentMonitors.length; i++) {
			persistentMonitors[i].heartRateUpdate(hr);
		}
	}

	private static void listen(InputStream stream) {
		int length;
		byte buffer[];
		try {
			while(true) {
				if(stream.read() != 0xFE) {
					AppLog.log("Misaligned packet");
					continue;
				}
				length = stream.read();
				if(length % 2 != 0 || length < 6 || length > 14 || stream.read() != 0xFF - length) {
					AppLog.log("Invalid packet");
					continue;
				}
				buffer = new byte[length - 3];
				if(stream.read(buffer) < 3) {
					AppLog.log("Buffer underrun");
					continue;
				}
				if((buffer[1] & BEAT_DETECT) == 0) {
					sendUpdate(NO_BEAT);
				} else {
					sendUpdate(buffer[2] & 0xFF);
				}
			}
		} catch(Exception e) {
		}
	}

	private synchronized boolean connect(String address) throws IOException {
		if(link != null) {
			return false;
		}
		AppLog.log("Connecting to Wearlink at " + address);
		link = (StreamConnection) Connector.open("btspp://" + address + ":1");
		return true;
	}

	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		new Thread(new DeviceAudit(btDevice)).start();
	}

	public void inquiryCompleted(int discType) {
		if(link == null && discType != DiscoveryListener.INQUIRY_TERMINATED) {
			AppLog.log("Restarting BT device inquiry");
			try {
				agent.startInquiry(DiscoveryAgent.GIAC, this);
			} catch(BluetoothStateException e) {
				AppLog.log("Restart failed");
			}
		}
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
	}

	public void serviceSearchCompleted(int transID, int respCode) {
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == Dashboard.exitCommand) {
			try {
				link.close();
			} catch (Exception e) {
			}
		}
	}
}
