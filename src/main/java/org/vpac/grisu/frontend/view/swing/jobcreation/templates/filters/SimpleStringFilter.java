package org.vpac.grisu.frontend.view.swing.jobcreation.templates.filters;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SimpleStringFilter implements Filter {

	public static final String SPLITSEPERATOR = "splitSeperator";
	public static final String SEPARATOR = "separator";
	public static final String PREFIX = "prefix";
	public static final String POSTFIX = "postfix";
	public static final String PREFIX_WHEN_EMPTY = "prefixWhenEmpty";
	public static final String POSTFIX_WHEN_EMPTY = "postfixWhenEmpty";

	public static final String MULTI_PREFIX = "multiPrefix";
	public static final String MULTI_POSTFIX = "multiPostfix";

	private String separator = " ";
	private String prefix = "";
	private String postfix = "";
	private String multiPrefix = "";
	private String multiPostfix = "";
	private boolean prefixWhenEmpty = false;
	private boolean postfixWhenEmpty = false;

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
		String prefEmpty = config.get(PREFIX_WHEN_EMPTY);
		try {
			prefixWhenEmpty = Boolean.parseBoolean(prefEmpty);
		} catch (Exception e) {
		}
		String postEmpty = config.get(PREFIX_WHEN_EMPTY);
		try {
			postfixWhenEmpty = Boolean.parseBoolean(postEmpty);
		} catch (Exception e) {
		}
	}

	public String filter(String value) {

		StringBuffer result = new StringBuffer();
		if (value.contains(",")) {

			List<String> temp = new LinkedList<String>();
			for (final String token : value.split(SPLITSEPERATOR)) {
				temp.add(multiPrefix + token + multiPostfix);
			}
			if (temp.size() == 0) {
				if (prefixWhenEmpty) {
					result.append(prefix);
				}
				if (postfixWhenEmpty) {
					result.append(postfix);
				}
			} else {
				result.append(prefix);
				result.append(StringUtils.join(temp, separator));
				result.append(postfix);
			}
		} else {
			if (StringUtils.isBlank(value)) {
				if (prefixWhenEmpty) {
					result.append(prefix);
				}
				if (postfixWhenEmpty) {
					result.append(postfix);
				}
			} else {
				result.append(prefix);
				result.append(value);
				result.append(postfix);
			}
		}
		return result.toString();
	}

}
