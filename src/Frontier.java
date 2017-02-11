public class Frontier implements Comparable<Frontier> {

	private String url;
	private Integer inlink;
	private Integer outlink;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getInlink() {
		return inlink;
	}

	public void setInlink(Integer inlink) {
		this.inlink = inlink;
	}

	public Integer getOutlink() {
		return outlink;
	}

	public void setOutlink(Integer outlink) {
		this.outlink = outlink;
	}

	public Frontier(String url, Integer inlink, Integer outlink) {
		super();
		this.url = url;
		this.inlink = inlink;
		this.outlink = outlink;
	}

	public Frontier() {
		super();
	}

	public int compareTo(Frontier f) {
		Integer inlink2 = f.getInlink();
		return this.inlink - inlink2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	} 

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Frontier other = (Frontier) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return url + " ::: " + inlink + " ::: " + outlink;
	}

}
