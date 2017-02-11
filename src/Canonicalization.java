import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.validator.routines.UrlValidator;

public class Canonicalization {
	public static String queryFinal = "";

	public static String Canc(String URL) throws Exception {
		// Case handling
		char[] Escape = URL.toCharArray();

		for (int i = 0; i < Escape.length - 2; i++) {
			if (Escape[i] == '%') {
				Escape[i + 1] = Character.toUpperCase(Escape[i + 1]);
				Escape[i + 2] = Character.toUpperCase(Escape[i + 2]);
				// System.out.println("here");
			}
		}
		URI u = new URI(new String(Escape));

		String path1 = u.getPath();

		URI norm1 = new URI(u.getScheme().toLowerCase(), u.getUserInfo(), u

		.getHost().toLowerCase(), u.getPort(), path1, null, null);

		String n = norm1.toString();

		String scheme = norm1.getScheme();

		String Final = n.replaceAll(":80", "");
		// remove ? and /

		if ((Final.charAt(Final.length() - 1) == '?')) {

			Final = Final.substring(0, Final.length() - 1);
		}
		Final = Final.replaceAll("(?<!" + scheme + ":)/{2,}", "/");

		while (Final.endsWith("/")) {
			Final = Final.substring(0, Final.length() - 1);
		}

		return Final;

	}

	public static String reltoabs(String PURL, String extracted_link) {
		try {
			// extract the links here
			// String extracted_link;
			String PURL1 = "";

			if (PURL.endsWith("/")) {
				PURL = PURL.substring(0, PURL.length() - 1);
			}

			int slash = PURL.lastIndexOf('/');

			if (slash > 6) {

				PURL1 = PURL.substring(0, slash);
			}
			URI u_par = new URI(PURL);

			extracted_link = extracted_link.replaceAll("\\.{2,}", "");

			if (extracted_link.toLowerCase().matches("http://.*")
					|| extracted_link.toLowerCase().matches("https://.*")) {
				if (extracted_link.toLowerCase().matches("http://.*")
						|| extracted_link.toLowerCase().matches("https://.*")) {
					// extracted_link = extracted_link.replaceFirst("/+", "");
					return Canc(extracted_link);
				}
			}
			extracted_link = extracted_link.replaceFirst("/+", "/");

			URI u_ext = new URI(extracted_link);

			String host_par = u_par.getHost();

			String d = "";

			String host_ext = u_ext.getHost();

			UrlValidator u = new UrlValidator();

			if (host_ext == null) {

				if (!extracted_link.toLowerCase().startsWith("http")) {
					d = u_par.getScheme().toLowerCase() + "://"
							+ extracted_link.replaceFirst("/+", "");
				}

				if (extracted_link.startsWith("www.")) {

					return Canc(u_par.getScheme() + "://" + extracted_link);
				}

				if (u.isValid(d)) {

					return Canc(d);

				} else {

					if (extracted_link.toLowerCase().startsWith("/")) {
						String ext_www = extracted_link.replaceFirst("/+", "");

						if (ext_www.startsWith("www.")) {

							return Canc(u_par.getScheme() + "://" + ext_www);
						}

						return Canc(u_par.getScheme() + "://"
								+ host_par.toLowerCase() + "/" + extracted_link);
					} else

						return Canc(PURL1 + "/" + extracted_link);
				}
			} else
				return Canc(extracted_link);
		}

		catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) {
		reltoabs("http://en.wikipedia.org/wiki/Robots_exclusion_standard",
				"//skckjsd/kbja");

	}
}
