
public class Document {
	public String URL;
	public String HEAD;
	public String TEXT;
	public String HTML;
	public String OUTLINKS;
	public String INLINKS;
	public String getURL() {
		return URL;
	}
	public String getHEAD() {
		return HEAD;
	}
	public String getTEXT() {
		return TEXT;
	}
	public String getHTML() {
		return HTML;
	}
	public String getOUTLINKS() {
		return OUTLINKS;
	}			
	public String getINLINKS() {
		return INLINKS;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	public void setHEAD(String hEAD) {
		HEAD = hEAD;
	}
	public void setTEXT(String tEXT) {
		TEXT = tEXT;
	}
	public void setHTML(String hTML) {
		HTML = hTML;
	}
	public void setOUTLINKS(String oUTLINKS) {
		OUTLINKS = oUTLINKS;
	}
	public void setINLINKS(String iNLINKS) {
		INLINKS = iNLINKS;
	}
	public Document(String uRL, String hEAD, String tEXT, String hTML,
			String oUTLINKS, String iNLINKS) {
		super();
		URL = uRL;
		HEAD = hEAD;
		TEXT = tEXT;
		HTML = hTML;
		OUTLINKS = oUTLINKS;
		INLINKS = iNLINKS;
	}
	public Document() {
		super();
	}


	




//	public static void main(String[] args) throws JsonProcessingException {
//		
////		String json = mapper
////				.writeValueAsString(new Document("10", "hello"));
//		
//		
//		
////		System.out.println(json);
//	}

}
