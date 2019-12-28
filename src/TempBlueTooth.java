import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.swing.DefaultComboBoxModel;

public class TempBlueTooth implements ActionListener {
	boolean scanFinished = false;
	public RemoteDevice hc06device;
	public String hc05Url;

	public static OutputStream os;
	public static InputStream is;
	public static TempUI ui;

	public static List<RemoteDevice> devicelist = new ArrayList<RemoteDevice>();

	public static void main(String[] args) {
		try {
			ui = new TempUI();
			new TempBlueTooth().go();
		} catch (Exception ex) {
			Logger.getLogger(TempBlueTooth.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace();
			ui.stat.setText("블루투스 탐색에 실패했습니다.");
		}
	}

	private void go() throws Exception {
		// scan for all devices:
		scanFinished = false;
		ui.stat.setText("연결된 블루투스 장비를 모두 탐색합니다...");
		LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {
			@Override
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
				try {
					String name = btDevice.getFriendlyName(false);
					System.out.format("%s (%s)\n", name, btDevice.getBluetoothAddress());
					ui.devicecombo.addItem(name);
					devicelist.add(btDevice);
//					if (name.matches("JAVA")) {
//						hc05device = btDevice;
//						ui.stat.setText("접속하려는 장치의 블루투스를 확인했습니다.");
//					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void inquiryCompleted(int discType) {
				scanFinished = true;
				ui.stat.setText("아래 목록에서 기기를 선택하세요.");
				ui.devicecombo.setEnabled(true);
				ui.devicecombo.addActionListener(new TempBlueTooth());
			}

			@Override
			public void serviceSearchCompleted(int transID, int respCode) {

			}

			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

			}
		});
//		while (!scanFinished) {
//			// this is easier to understand (for me) as the thread stuff examples from
//			// bluecove
//			Thread.sleep(500);
//		}
		// search for services:

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					hc06device = devicelist.get(ui.devicecombo.getSelectedIndex());
					ui.remove(ui.devicecombo);
					ui.revalidate();
					ui.repaint();
					
					ui.stat.setText(hc06device.getFriendlyName(false) + "에 접속을 시도합니다.");
					UUID uuid = new UUID(0x1101); // scan for btspp://... services (as HC-05 offers it)
					UUID[] searchUuidSet = new UUID[] { uuid };
					int[] attrIDs = new int[] { 0x0100 // service name
					};
					scanFinished = false;
					LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, hc06device,
							new DiscoveryListener() {
								@Override
								public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {

								}

								@Override
								public void inquiryCompleted(int discType) {
								}

								@Override
								public void serviceSearchCompleted(int transID, int respCode) {
									scanFinished = true;
								}

								@Override
								public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

									for (int i = 0; i < servRecord.length; i++) {
										hc05Url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
										if (hc05Url != null) {
											break; // take the first one
										}
									}
								}
							});

					while (!scanFinished) {
						Thread.sleep(500);
					}

					System.out.println(hc06device.getBluetoothAddress());
					System.out.println(hc05Url);
					
					ui.stat.setText(hc06device.getFriendlyName(false) + "에 접속했습니다.");
					// if you know your hc05Url this is all you need:
					StreamConnection streamConnection = (StreamConnection) Connector.open(hc05Url);
					System.out.println(streamConnection == null);
					os = streamConnection.openOutputStream();
					is = streamConnection.openInputStream();
					os.write(new String(LocalDevice.getLocalDevice().getFriendlyName() + " Connected").getBytes()); // 아무런 값이나
																													// 보내줘야 아두이노
																													// 블루투스가 반응함
					os.flush();

					StringBuilder sb = new StringBuilder();
					Thread th = new Thread(new Runnable() {

						@Override
						public void run() {
							while (true) {
								try {
									sb.append((char) is.read());
									if (sb.charAt(sb.length() - 1) == '\n') {
										System.out.print(sb.toString());
										ui.hu.setText(sb.toString().split(",")[0]);
										if (Integer.parseInt(ui.hu.getText().trim()) > 70) {
											ui.hu.setForeground(new Color(204, 0, 0));
										} else {
											ui.hu.setForeground(Color.white);
										}

										ui.temp.setText(sb.toString().split(",")[1]);
										if (Integer.parseInt(ui.temp.getText().trim()) > 35) {
											ui.temp.setForeground(new Color(204, 0, 0));
										} else {
											ui.temp.setForeground(Color.white);
										}

										sb.delete(0, sb.length());
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
					});
					th.setName("블루투스 수신");
					th.start();

					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							try {
								th.interrupt();
								streamConnection.close();
								os.close();
								is.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (Exception e1) {
					Logger.getLogger(TempBlueTooth.class.getName()).log(Level.SEVERE, null, e1);
					e1.printStackTrace();
					ui.stat.setText("블루투스 연결에 실패했습니다.");
				}
				
			}
		});
		th.setName("블루투스 연결");
		th.start();
		
	}
}