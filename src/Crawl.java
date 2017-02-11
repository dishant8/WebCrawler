import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawl {

	public static Document doc;
	// public static NormalizeURL n;
	public static Canonicalization c;
	static long start_time = 0;
	static long end_time = 0;
	public static HashMap<String, Set<String>> inlinks = new HashMap<>();

	public static Frontier p = new Frontier(
			"https://en.wikipedia.org/wiki/Data_structure", 1, 0);

	public static Frontier p1 = new Frontier(
			"https://en.wikipedia.org/wiki/Sparse_matrix", 1, 0);

	public static Frontier p2 = new Frontier(
			"http://www.geeksforgeeks.org/data-structures/", 1, 0);

	public static Frontier p3 = new Frontier(
			"https://msdn.microsoft.com/en-us/library/aa289148(v=vs.71).aspx",
			1, 0);

	public static Frontier p4 = new Frontier(
			"https://en.wikipedia.org/wiki/List_of_data_structures", 1, 0);

	public static HashMap<String, Frontier> PriorityMap = new HashMap<String, Frontier>();

	static Comparator<Frontier> compare = new Comparator<Frontier>() {
		@Override
		public int compare(Frontier o1, Frontier o2) {

			Integer d = o2.getInlink() - o1.getInlink();

			return d;
		}
	};

	public static PriorityQueue<Frontier> linklist = new PriorityQueue<Frontier>(
			compare);

	// public static HashMap<String, ArrayList<Integer>> URLMap = new
	// HashMap<String, ArrayList<Integer>>();
	public static void main(String[] args) throws IOException,
			URISyntaxException {
		linklist.add(p);
		PriorityMap.put(p.getUrl(), p);
		linklist.add(p1);
		PriorityMap.put(p1.getUrl(), p1);
		linklist.add(p2);
		PriorityMap.put(p2.getUrl(), p2);
		linklist.add(p3);
		PriorityMap.put(p3.getUrl(), p3);
		linklist.add(p4);
		PriorityMap.put(p4.getUrl(), p4);

		connection();
	}

	public static void connection() throws IOException, URISyntaxException {

		int count = 0;
		int filecount = 0;
		int filename = 0;
		Robotcheck robotobj = new Robotcheck();
		while (!linklist.isEmpty() && count < 20000) {
			try {

				HashSet<String> outLinkSet = new HashSet<String>();
				Frontier start = linklist.poll();
				String urlpoped = start.getUrl();

				System.out.println("URL" + urlpoped);

				if (!robotobj.crawlable(urlpoped)) {
					continue;
				}

				URL url = new URL(urlpoped);
				start_time = System.currentTimeMillis();

				URLConnection connection = url.openConnection();

				connection.setConnectTimeout(30000);

				connection.setReadTimeout(30000);

				connection
						.setRequestProperty(
								"User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");

				String content = connection.getContentType();

				if (content == null) {
					continue;
				}

				String[] split = content.split(" ");

				if (split[0].startsWith("text/html")) {

					InputStream input = connection.getInputStream();
					BufferedReader reader;
					if (split.length >= 2) {
						reader = new BufferedReader(new InputStreamReader(
								input, split[1].split("=")[1]));
					} else {
						reader = new BufferedReader(new InputStreamReader(
								input, "ISO-8859-1"));
					}

					String line;

					StringBuilder build = new StringBuilder();

					while ((line = reader.readLine()) != null) {
						build.append(line);
					}

					Document document = Jsoup.parse(build.toString());

					Elements language = document.select("html[lang]");

					String english = "";

					if (!language.isEmpty()) {
						english = language.first().attr("lang");
						if (!english.matches("en.*"))
							continue;
					}

					Elements htmlLinks = document.getElementsByTag("a");

					for (Element link : htmlLinks) {
						String textLink = link.attr("href");

						if (textLink.trim().isEmpty()
								|| textLink.trim().startsWith("#")) {
							continue;
						}

						textLink = c.reltoabs(urlpoped, textLink.trim());

						if (textLink.isEmpty() || textLink == null) {
							continue;
						}

						if (textLink.matches(".*[^\u0000-\u007F]+.*")) {

							continue;

						} else {
							if (!(textLink.equals(urlpoped))) {

								outLinkSet.add(textLink);

								if (inlinks.containsKey(textLink)) {
									inlinks.get(textLink).add(urlpoped);
								} else {
									Set<String> ilk = new HashSet<String>();
									ilk.add(urlpoped);
									inlinks.put(textLink, ilk);
								}

							}

							if (!PriorityMap.containsKey(textLink)) { // check
								Frontier PQ = new Frontier(textLink, 1, 0);
								linklist.add(PQ);
								PriorityMap.put(textLink, PQ);
							} else {
								Frontier pQueue = PriorityMap.get(textLink);
								pQueue.setInlink(pQueue.getInlink() + 1);
							}
						}
					}

					PriorityMap.get(urlpoped).setOutlink(outLinkSet.size());
					filecount++;
					if (filecount == 201) {
						filecount = 1;
						filename++;
					}
					end_time = System.currentTimeMillis();
					if (end_time - start_time <= 1000) {
						Thread.sleep(1000 - (end_time - start_time));
					}

					writetofile("C:\\IR\\IRAssignment 3 files\\SECONDTIME\\"
							+ filename + ".txt", document, urlpoped, outLinkSet);

					count++;
					System.out.println("count:::" + count);
					// linklist.remove();
				}

				else
					continue;

			}

			catch (Exception e) {
				System.out.println("EXCEPTION");
				e.printStackTrace();
			}
			// ISO-8859-1
		}
		writemap(inlinks);
	}

	public static void writetofile(String Filename, Document doc, String URL,
			HashSet<String> Out) throws IOException {
		FileWriter writer = new FileWriter(Filename, true);
		writer.write("<DOC>\r\n");
		writer.write("<URL>" + URL + "</URL>\r\n");
		writer.write("<HEAD>" + doc.title() + "</HEAD>\r\n");
		writer.write("<TEXT>" + doc.select("body").text() + "</TEXT>\r\n");
		writer.write("<RAWTEXT>" + doc.html() + " " + "</RAWTEXT>\r\n");
		StringBuilder sb = new StringBuilder();

		for (String s : Out) {
			sb.append(s).append(" ");
		}

		writer.write("<OUTLINKS>" + sb.toString().trim() + "</OUTLINKS>\r\n");
		writer.write("</DOC>\r\n");
		writer.close();
	}

	public static void writemap(HashMap<String, Set<String>> inlinkMap)
			throws IOException {
		FileWriter writer = new FileWriter(
				"C:\\IR\\IRAssignment 3 files\\SECONDTIME\\inlinks.txt", true);

		for (Entry<String, Set<String>> entry : inlinkMap.entrySet()) {
			writer.write("<DOC>\r\n");
			writer.write("<URL>" + entry.getKey() + "</URL>\r\n");
			StringBuilder builder = new StringBuilder();
			for (String s : entry.getValue()) {
				builder.append(s).append(" ");
			}
			writer.write("<INLINK>" + builder.toString().trim()
					+ "</INLINK>\r\n");
			writer.write("</DOC>\r\n");
		}
	}

}