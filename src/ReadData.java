import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.databind.ObjectMapper;



public class ReadData {
	private static final String CLUSTER_NAME = "macbook";
	private static final String TYPE = "document";
	private static final String INDEX_NAME = "team_s";
	static Settings settings = null;

	static TransportClient transportClient = null;

	static Node node = null;

	static Client client = null;

	static {
		
		settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", CLUSTER_NAME).build();

		transportClient = new TransportClient(settings);

		transportClient.addTransportAddress(new InetSocketTransportAddress(
				"localhost", 9300));

		node = NodeBuilder.nodeBuilder().client(true).clusterName(CLUSTER_NAME)
				.node();

		client = node.client();
	}
	
	public static void indexDocument(String document, String id) {
		
		client.prepareIndex(INDEX_NAME, TYPE, id).setSource(document).execute()
				.actionGet();
	}
	
	public static String parse(String Link) {
		String res = "";
		Link = StringUtils.substringBetween(Link, "[", "]");
		String[] temp = Link.split(", ");
		for (String str : temp)
			res += str + " ";
		return res.trim();
	}

	
	public static Map<String, List<String>> queryTF(String t) throws IOException {
		TermQueryBuilder qb = QueryBuilders.termQuery("text", t);

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME)
				.setTypes("document").setScroll(new TimeValue(6000))
				.setQuery(qb).setExplain(true).setSize(1000).execute()
				.actionGet();

		// no query matched
		if (scrollResp.getHits().getTotalHits() == 0) {
			System.out.println("not found");
			return new HashMap<String, List<String>>();
		}

		Map<String, List<String>> results = new HashMap<>();

		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				
				ArrayList<String> inli = new ArrayList<String>();
				
				String url1 = (String) hit.getSource().get("URL");
				
				String inlinks = (String) hit.getSource().get("INLINKS");
				
				for (String in : inlinks.split(" ")) {
					inli.add(in);
				}
				
				results.put(url1, inli);
			}

			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
					.setScroll(new TimeValue(6000)).execute().actionGet();
			
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
	
		return results;
	}


	public static Map<String, List<String>> loadfile(String File)
			throws IOException, ClassNotFoundException {
		Map<String, List<String>> loader = new HashMap<>();
		FileInputStream fis = new FileInputStream(File);
		ObjectInputStream ois = new ObjectInputStream(fis);
		loader = (Map<String, List<String>>) ois.readObject();
		ois.close();
		fis.close();
		return loader;
	}

	
	public static void main(String[] args) throws Exception {
		Map<String, List<String>> inlinksMap = loadfile("D:\\SerializeDIS.txt");
		Pattern docPattern = Pattern.compile("<DOC>(.*?)</DOC>");
		
		Pattern UrlPattern = Pattern.compile("<URL>(.*?)</URL>");
		File dir = new File("D:\\dishant\\DATA");
		String[] list = dir.list();

		System.out.println("in main");
		for (String s : list) {
//			System.out.println(s);
			FileReader file = new FileReader("D:\\dishant\\DATA\\" + s);

			BufferedReader reader = new BufferedReader(file);
			StringBuilder builder = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				builder.append(line).append(" ");
			}

			Matcher docMatcher = docPattern.matcher(builder.toString());

			
			Pattern headPattern = Pattern.compile("<HEAD>(.*?)</HEAD>");			
			
			Pattern textPattern = Pattern.compile("<TEXT>(.*?)</TEXT>");

			Pattern RawtextPattern = Pattern.compile("<RAWTEXT>(.*?)</RAWTEXT>");
			
			Pattern Outlinks = Pattern.compile("<OUTLINKS>(.*?)</OUTLINKS>");
			
			ObjectMapper mapper = new ObjectMapper();

			//IndexToES.createIndex();

			while (docMatcher.find()) {
				// get one document at a time
				String doc = docMatcher.group(0);
				String docid = null;
				String text = null;
				String head = null;
				String rawtext = null;
				String outlinks = null;

				Matcher urlMatcher = UrlPattern.matcher(doc);

				Matcher headMatcher = headPattern.matcher(doc);

				Matcher textMatcher = textPattern.matcher(doc);

				Matcher rawMatcher = RawtextPattern.matcher(doc);
				
				Matcher outlinksMatcher = Outlinks.matcher(doc);
				
				if (urlMatcher.find()) {
					docid = urlMatcher.group(1).trim();
					System.out.println(docid);
				}
				
				if(headMatcher.find()){
					head = headMatcher.group(1).trim();
				}
				
				if(textMatcher.find()){
					text = textMatcher.group(1).trim();
				}

				if(rawMatcher.find()){
					rawtext = rawMatcher.group(1).trim();
				}
				
				if(outlinksMatcher.find()){
					outlinks = outlinksMatcher.group(1).trim();
				}
				
				
				Map<String, List<String>> test1 = queryTF(docid);
				
				if (test1.containsKey(docid)) {
					test1.get(docid).remove(inlinksMap.get(docid.trim()));
					test1.get(docid).addAll(inlinksMap.get(docid.trim()));
					String temp = parse(test1.get(docid).toString());
				
					String json = mapper.writeValueAsString(new Document(docid, head, text, rawtext, outlinks, temp));
				}else if (inlinksMap.containsKey(docid)) {
					String json = mapper.writeValueAsString
							(new Document(docid, head, text, rawtext, outlinks, parse(inlinksMap.get(docid).toString())));
				} else
					continue;
			}

			client.close();
		}

}
	
}
