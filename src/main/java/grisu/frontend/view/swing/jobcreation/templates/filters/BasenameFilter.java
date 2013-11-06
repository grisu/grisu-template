package grisu.frontend.view.swing.jobcreation.templates.filters;

import grisu.model.FileManager;
import grisu.utils.FileHelpers;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BasenameFilter implements Filter {

    public static void main(String[] args) {

        BasenameFilter bf = new BasenameFilter();

        String test = bf.filter("/home/markus/test/Diff/");

        System.out.println(test);


    }

	public static final String SEPARATOR = "separator";
	public static final String PREFIX = "prefix";
	public static final String POSTFIX = "postfix";

	public static final String MULTI_PREFIX = "multiPrefix";
	public static final String MULTI_POSTFIX = "multiPostfix";

	public static final String REMOVE_EXTENSION = "noExtension";

	private String separator = " ";
	private String prefix = "";
	private String postfix = "";
	private String multiPrefix = "";
	private String multiPostfix = "";

	private boolean removeExtension = false;

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
		String noExtension = config.get(REMOVE_EXTENSION);
		if ( Boolean.parseBoolean(noExtension)) {
			removeExtension = true;
		}
	}

	public String filter(String value) {

        value = FileManager.removeTrailingSlash(value);

		if (value.contains(",")) {

			List<String> temp = new LinkedList<String>();
			for (final String url : value.split(",")) {

				String filename = FileHelpers.getFilename(url);
				if ( removeExtension ) {
					filename = FilenameUtils.getBaseName(filename);
				}

				temp.add(multiPrefix + filename
						+ multiPostfix);
			}
			return prefix + StringUtils.join(temp, separator) + postfix;
		}


		String filename = FileHelpers.getFilename(value).trim();
		if ( removeExtension ) {
			filename = FilenameUtils.getBaseName(filename);
		}

		return prefix + filename + postfix;
	}

}
