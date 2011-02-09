package grisu.frontend.view.swing.jobcreation.templates.filters;

import grisu.utils.FileHelpers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class BasenameFilter implements Filter {

	public static final String SEPARATOR = "separator";
	public static final String PREFIX = "prefix";
	public static final String POSTFIX = "postfix";

	public static final String MULTI_PREFIX = "multiPrefix";
	public static final String MULTI_POSTFIX = "multiPostfix";

	private String separator = " ";
	private String prefix = "";
	private String postfix = "";
	private String multiPrefix = "";
	private String multiPostfix = "";

	public void config(Map<String, String> config) {

		String sep = config.get(SEPARATOR);
		if (StringUtils.isNotBlank(sep)) {
			separator = sep;
		}
		String pre = config.get(PREFIX);
		if (StringUtils.isNotBlank(pre)) {
			prefix = pre;
		}
		String post = config.get(POSTFIX);
		if (StringUtils.isNotBlank(post)) {
			postfix = post;
		}
		String multiPre = config.get(MULTI_PREFIX);
		if (StringUtils.isNotBlank(multiPre)) {
			multiPrefix = multiPre;
		}
		String multiPost = config.get(MULTI_POSTFIX);
		if (StringUtils.isNotBlank(multiPost)) {
			multiPostfix = multiPost;
		}
	}

	public String filter(String value) {

		if (value.contains(",")) {

			List<String> temp = new LinkedList<String>();
			for (final String url : value.split(",")) {
				temp.add(multiPrefix + FileHelpers.getFilename(url)
						+ multiPostfix);
			}
			return prefix + StringUtils.join(temp, separator) + postfix;
		}

		return prefix + FileHelpers.getFilename(value).trim() + postfix;
	}

}
