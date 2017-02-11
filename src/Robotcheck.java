import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;

public class Robotcheck {
	public static final Map<String, BaseRobotRules> RobotMap = new HashMap<>();

	public static boolean crawlable(String url) throws MalformedURLException {

		System.out.println("In robot");
		BaseRobotRules rules = getRules(url);

		if (rules != null) {
			System.out.println(rules.isAllowed(url));
			return rules.isAllowed(url);
		}
		System.out.println(false);
		return false;
	}

	private static BaseRobotRules getRules(String url)
			throws MalformedURLException {
		URL urlstr = new URL(url);

		String host = urlstr.getHost();

		if (host == null) {
			return null;
		}

		if (RobotMap.containsKey(host)) {
			BaseRobotRules rules = RobotMap.get(host);
			return rules;
		} else {
			try {
				System.out.println("Fetch robot rules timeout");
				BaseRobotRules rules = checkRules(url);
				if (null != rules) {
					RobotMap.put(host, rules);
					return rules;
				}

			} catch (IOException e) {
				// System.out.println(e);
			} catch (URISyntaxException e) {
				// e.printStackTrace();
			}
		}

		return null;

	}

	private static BaseRobotRules checkRules(String url) throws IOException,
			URISyntaxException {

		URI uri = new URI(url);

		String getUrl = uri.getScheme() + "://" + uri.getAuthority() + "/"
				+ "robots.txt";

		URL urlObj = new URL(getUrl);

		HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

		if (conn == null) {
			return null;
		}

		conn.setConnectTimeout(7000);
		conn.setReadTimeout(30000);

		SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();

		return robotParser.parseContent(getUrl, readRobotTxt(getUrl, conn),
				"text/plain", "my-crawler");
	}

	private static byte[] readRobotTxt(String url, HttpURLConnection connection)
			throws IOException {

		connection.addRequestProperty("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

		connection.setRequestMethod("GET");

		DataInputStream inputStream = null;

		try {

			inputStream = new DataInputStream(connection.getInputStream());

			byte[] b = new byte[1024];
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

			int readStream = 0;
			while ((readStream = inputStream.read(b, 0, b.length)) != -1) {
				byteStream.write(b, 0, readStream);
			}

			return byteStream.toByteArray();

		} finally {
			if (null != inputStream) {
				inputStream.close();
			}
		}
	}

	public static void main(String[] args) throws MalformedURLException {
		System.out.println(crawlable("http://stackoverflow.com/questions/25947238/cannot-work-with-jackson"));
	}
}
