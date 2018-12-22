package bms.player.beatoraja.ir;

import java.io.File;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class IRConnectionManager {

	private static Class[] irconnections;


	/**
	 * 利用可能な全てのIRConnectionの名称を返す
	 * 
	 * @return IRConnectionの名称
	 */
	public static String[] getAllAvailableIRConnectionName() {
		Class[] irclass = getAllAvailableIRConnection();
		String[] names = new String[irclass.length];
		for (int i = 0; i < names.length; i++) {
			try {
				names[i] = irclass[i].getField("NAME").get(null).toString();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return names;
	}

	/**
	 * 名称に対応したIRConnectionインスタンスを返す
	 * 
	 * @param name
	 *            IRCOnnectionの名称
	 * @return 対応するIRConnectionインスタンス。存在しない場合はnull
	 */
	public static IRConnection getIRConnection(String name) {
		Class irclass = getIRConnectionClass(name);
		if(irclass != null) {
			try {
				return (IRConnection) irclass.newInstance();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Class getIRConnectionClass(String name) {
		if (name == null || name.length() == 0) {
			return null;
		}
		Class[] irclass = getAllAvailableIRConnection();
		for (int i = 0; i < irclass.length; i++) {
			try {
				if (name.equals(irclass[i].getField("NAME").get(null).toString())) {
					return irclass[i];
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static Class[] getAllAvailableIRConnection() {
		if(irconnections != null) {
			return irconnections;
		}
		List<Class> classes = new ArrayList();

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Enumeration<URL> urls = cl.getResources("bms/player/beatoraja/ir");
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (url.getProtocol().equals("jar")) {
					JarURLConnection jarUrlConnection = (JarURLConnection) url.openConnection();
					JarFile jarFile = null;
					try {
						jarFile = jarUrlConnection.getJarFile();
						Enumeration<JarEntry> jarEnum = jarFile.entries();

						while (jarEnum.hasMoreElements()) {
							JarEntry jarEntry = jarEnum.nextElement();
							String path = jarEntry.getName();
							if (path.startsWith("bms/player/beatoraja/ir/") && path.endsWith(".class")) {
								Class c = cl.loadClass("bms.player.beatoraja.ir."
										+ path.substring(path.lastIndexOf("/") + 1, path.length() - 6));
								for (Class inf : c.getInterfaces()) {
									if (inf == IRConnection.class) {
										for (Field f : c.getFields()) {
											if (f.getName().equals("NAME")) {
												Object irname = c.getField("NAME").get(null);
												classes.add(c);
											}
										}
										break;
									}
								}
							}
						}
					} finally {
						if (jarFile != null) {
							jarFile.close();
						}
					}
				}
				if (url.getProtocol().equals("file")) {
					File dir = new File(url.getPath());
					for (String path : dir.list()) {
						if (path.endsWith(".class")) {
							Class c = cl.loadClass("bms.player.beatoraja.ir." + path.substring(0, path.length() - 6));
							for (Class inf : c.getInterfaces()) {
								if (inf == IRConnection.class) {
									for (Field f : c.getFields()) {
										if (f.getName().equals("NAME")) {
											Object irname = c.getField("NAME").get(null);
											classes.add(c);
										}
									}
									break;
								}
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		irconnections = classes.toArray(new Class[classes.size()]);
		return irconnections;
	}

	/**
	 * IRのオームURLを取得する
	 * @param name IR名
	 * @return IRのホームURL。存在しない場合はnull
	 */
	public static String getHomeURL(String name) {
		Class irclass = getIRConnectionClass(name);
		if(irclass != null) {
			try {
				Object result = irclass.getField("HOME").get(null);
				if(result != null) {
					return result.toString();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
