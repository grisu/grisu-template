package org.vpac.grisu.frontend.view.swing.jobcreation.templates;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.filters.Filter;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels.AbstractInputPanel;

public class TemplateHelpers {
	
	public static final String COMMANDLINE_KEY = "commandline";
	public static final String USE_SCROLLBARS_KEY = "useScrollbars";

	static final Logger myLogger = Logger.getLogger(TemplateHelpers.class
			.getName());

	private static void addValueToConfig(PanelConfig config, String line)
			throws TemplateException {

		if (config == null) {
			throw new TemplateException("No config object when parsing line: "
					+ line);
		}

		line = line.trim();

		int index = line.indexOf("=");
		if (index < 0) {
			throw new TemplateException("Can't find = char in line: " + line);
		}

		String key = line.substring(0, index - 1).trim();
		String value = line.substring(index + 1).trim();

		if (StringUtils.isBlank(key)) {
			throw new TemplateException("Can't parse key for line: " + line);
		}

		if (StringUtils.isBlank(value)) {
			value = "";
			// throw new TemplateException("Can't parse value for line: " +
			// line);
		}

		if ("type".equals(key)) {
			config.setType(value);
		} else if ("filter".equals(key)) {
			Filter filter = createFilter(value);
			config.addFilter(filter);
		} else if ("validator".equals(key)) {
			Validator<String> val = createValidator(value);
			config.addValidator(val);
		} else {
			config.addConfig(key, value);
		}

	}

	public static Filter createFilter(String configString)
			throws TemplateException {

		try {

			String[] configParts = configString.split(":");

			Map<String, String> filterConfig = createFilterConfig(configString);

			Class filterClass = Class
					.forName("org.vpac.grisu.frontend.view.swing.jobcreation.templates.filters."
							+ filterConfig.get("type"));
			filterConfig.remove("type");

			Filter filter = null;
			if (filterConfig.size() == 0) {
				filter = (Filter) filterClass.newInstance();
			} else {
				Constructor<Filter> filterConstructor = filterClass
						.getConstructor(Map.class);
				filter = filterConstructor.newInstance(filterConfig);

			}

			return filter;

		} catch (Exception e) {
			throw new TemplateException(
					"Can't create filter for config string: " + configString, e);
		}

	}

	private static Map<String, String> createFilterConfig(String configString)
			throws TemplateException {

		configString = configString.trim();

		Map<String, String> config = new HashMap<String, String>();

		int startIndex = configString.indexOf("[");
		if (startIndex > 0) {
			// means configuration
			int endIndex = configString.indexOf("]");
			String[] initValues = configString.substring(startIndex + 1,
					endIndex).split(":");
			for (String value : initValues) {
				value = value.trim();
				int index = value.indexOf("=");
				if (index <= 0) {
					throw new TemplateException(
							"Can't create filter config because. Unable to find = character in string "
									+ value);
				}
				String key = value.substring(0, index).trim();
				String value2 = value.substring(index + 1).trim();
				config.put(key, value2);
			}
		}

		String type = null;
		if (startIndex == -1) {
			type = configString;
		} else {
			type = configString.substring(0, startIndex).trim();
		}
		config.put("type", type);

		return config;
	}

	public static AbstractInputPanel createInputPanel(String templateName,
			PanelConfig config) throws TemplateException {

		if (config == null) {
			throw new TemplateException("No config object. Can't create panel.");
		}

		String type = config.getType();

		try {
			Class inputPanelClass = Class
					.forName("org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels."
							+ type);
			Constructor<AbstractInputPanel> constructor = inputPanelClass
					.getConstructor(String.class, PanelConfig.class);

			AbstractInputPanel panel = constructor.newInstance(templateName,
					config);

			return panel;

		} catch (Exception e) {
			throw new TemplateException("Can't create input panel "
					+ config.getProperties().get(AbstractInputPanel.NAME)
					+ " of type " + config.getType(), e);
		}

	}

	public static JPanel createTab(LinkedList<JPanel> rows, boolean useScrollbars) {

		
		JPanel panel = new JPanel();		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (JPanel row : rows) {
			panel.add(row);
		}

		if (useScrollbars) {
		JScrollPane scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.add(scrollPane, BorderLayout.CENTER);
		
		return rootPanel;
		} else {
			return panel;
		}

	}

	private static Validator<String> createValidator(String configString)
			throws TemplateException {

		configString = configString.trim();

		Map<String, String> config = new HashMap<String, String>();

		int startIndex = configString.indexOf("[");
		if (startIndex > 0) {
			// means configuration
			int endIndex = configString.indexOf("]");
			String[] initValues = configString.substring(startIndex + 1,
					endIndex).split(":");
			for (String value : initValues) {
				value = value.trim();
				int index = value.indexOf("=");
				if (index <= 0) {
					throw new TemplateException(
							"Can't create filter config because. Unable to find = character in string "
									+ value);
				}
				String key = value.substring(0, index).trim();
				String value2 = value.substring(index + 1).trim();
				config.put(key, value2);
			}
		}

		String valName = null;
		if (startIndex == -1) {
			valName = configString;
		} else {
			valName = configString.substring(0, startIndex).trim();
		}

		Exception exception;
		// try {
		Class validatorClass = null;
		try {
			validatorClass = Class
					.forName("org.vpac.grisu.frontend.view.swing.jobcreation.templates.validators."
							+ valName);
		} catch (ClassNotFoundException e1) {
			// that's ok. let's try the inbuild ones
			myLogger.debug("Can't find validator with name: " + valName
					+ "in classpath. Trying inbuild ones...");
			Class valClass = Validators.class;

			try {
				Field field = valClass.getField(valName);
				Validator<String> value = (Validator<String>) field.get(null);
				return value;
			} catch (Exception e) {
				throw new TemplateException("Can't find validator with name "
						+ valName + ".", e);
			}
		}

		Constructor<Validator> constructor;
		try {

			if (config == null || config.size() == 0) {
				Validator<String> val = (Validator<String>) validatorClass
						.newInstance();
				return val;
			}

			constructor = validatorClass.getConstructor(Map.class);
			Validator<String> val = constructor.newInstance(config);

			return val;
		} catch (Exception e) {
			throw new TemplateException("Can't create validator with name "
					+ valName + ": " + e.getLocalizedMessage());
		}

	}

	public static Map<String, String> getAllStaticValues(List<String> lines)
			throws TemplateException {

		Map<String, String> result = new HashMap<String, String>();
		for (String line : lines) {

			line = line.trim();
			if (StringUtils.isBlank(line) || line.startsWith("#")) {
				continue;
			}

			if (line.startsWith("=")) {
				break;
			}

			int index = line.indexOf("=");
			if (index <= 0) {
				throw new TemplateException(
						"Config needs to have proper specification of the property "
								+ line
								+ " (in the form of for example \"commandline = echo hello world\"");
			}

			String propertyKey = line.substring(0, index).trim();
			String propertyValue = line.substring(index + 1).trim();
			if (StringUtils.isBlank(propertyKey)
					|| StringUtils.isBlank(propertyValue)) {
				throw new TemplateException(
						"Config needs to have proper specification of the property "
								+ line
								+ " (in the form of for example \"commandline = echo hello world\"");
			}
			result.put(propertyKey, propertyValue);
		}

		return result;
	}

	private static String getNewPageIndicator(String line)
			throws TemplateException {

		line = line.trim();

		if (line.startsWith("=")) {
			if (!line.endsWith("=")) {
				throw new TemplateException(
						"Line starts with = but doesn't end with another =: "
								+ line);
			}

			line = line.replace("=", "");
			line = line.trim();
			return line;
		} else {
			return null;
		}
	}

	private static String getNewRowIndicator(String line)
			throws TemplateException {

		line = line.trim();

		if (line.startsWith("-")) {
			if (!line.endsWith("-")) {
				throw new TemplateException(
						"Line starts with - but doesn't end with another -: "
								+ line);
			}

			line = line.replace("-", "");
			line = line.trim();
			return line;
		} else {
			return null;
		}

	}

	public static String getPanelName(String line) throws TemplateException {

		line = line.trim();

		int start = line.indexOf("[");
		if (start < 0) {
			myLogger.debug("No panel name config.");
			return null;
		}

		if (start != 0) {
			myLogger
					.debug("No panel name because [ is not the first character.");
			return null;
		}

		int end = line.indexOf("]");
		if (end < 0) {
			throw new TemplateException("No closing ] bracket in line " + line);
		}

		String name = line.substring(start + 1, end);

		return name;

	}

	public static String getValue(String property, List<String> lines)
			throws TemplateException {

		String propertValue = null;

		for (String line : lines) {
			propertValue = line.trim();

			if (propertValue.startsWith("=")) {
				break;
			}

			if (StringUtils.isBlank(propertValue)) {
				continue;
			}

			if (!propertValue.startsWith(property)) {
				continue;
			}

			int index = propertValue.indexOf("=");
			if (index <= 0) {
				throw new TemplateException(
						"Config needs to have proper specification of the property "
								+ property
								+ " (in the form of for example \"commandline = echo hello world\"");
			}

			propertValue = propertValue.substring(index + 1).trim();
			if (StringUtils.isBlank(propertValue)) {
				throw new TemplateException(
						"Config needs to have proper specification of the property "
								+ property
								+ " (in the form of for example \"commandline = echo hello world\"");
			}
			break;
		}

		if (StringUtils.isBlank(propertValue)) {
			throw new TemplateException(
					"Config needs to contain the specification of the property "
							+ property
							+ " (in the form of for example \"commandline = echo hello world\"");
		} else {
			return propertValue;
		}

	}

	public static void main(String[] args) throws IOException,
			TemplateException {

		List<String> lines = FileUtils.readLines(new File(
				"/home/markus/Desktop/test.template"));

		// LinkedHashMap<String, AbstractInputPanel> panels =
		// parseConfig(lines);
		//
		// for ( String panel : panels.keySet() ) {
		// System.out.println("Panelname: "+panel);
		// System.out.println(panels.get(panel).toString());
		// }

	}

	public static TemplateObject parseAndCreateTemplatePanel(
			ServiceInterface si, String templateFileName, List<String> lines)
			throws TemplateException {

		Map<String, String> values = getAllStaticValues(lines);
		String commandline = values.remove(COMMANDLINE_KEY);

		if (StringUtils.isBlank(commandline)) {
			throw new TemplateException(
					"\"commandline\" property not specified. You need to have a line like: \'commandline = echo hello\' in your config");
		}
		
		Boolean useScrollbars = false;
		try {
			
			useScrollbars = Boolean.parseBoolean(values.remove(USE_SCROLLBARS_KEY));
			
		} catch (Exception e) {
			// doesn't matter
		}

		TemplateObject template = new TemplateObject(si, commandline, values);

		ValidationPanel validationPanel = new ValidationPanel();

		template.setTemplateName(templateFileName);

		LinkedHashMap<String, PanelConfig> inputConfigs = parseConfig(lines);
		LinkedHashMap<String, AbstractInputPanel> inputPanels = new LinkedHashMap<String, AbstractInputPanel>();

		LinkedHashMap<String, LinkedList<JPanel>> tabs = new LinkedHashMap<String, LinkedList<JPanel>>();

		LinkedList<JPanel> currentTab = null;
		JPanel currentRow = null;

		boolean firstPanelReached = false;

		for (String line : lines) {

			line = line.trim();
			if (StringUtils.isBlank(line) || line.startsWith("#")) {
				continue;
			}

			if (!firstPanelReached && line.startsWith("=")) {
				firstPanelReached = true;
			}

			if (!firstPanelReached) {
				continue;
			}

			String lineType = getNewPageIndicator(line);

			if (StringUtils.isNotBlank(lineType)) {
				// means a new page
				currentRow = null;
				currentTab = new LinkedList<JPanel>();
				tabs.put(lineType, currentTab);
				continue;
			}

			lineType = getNewRowIndicator(line);

			if (lineType != null) {
				currentRow = new JPanel();
				currentRow.setAlignmentX(Component.LEFT_ALIGNMENT);
				currentRow.setAlignmentY(Component.TOP_ALIGNMENT);
				BoxLayout layout = new BoxLayout(currentRow, BoxLayout.X_AXIS);
				currentRow.setLayout(layout);
				if (lineType.length() > 0) {
					currentRow
							.setBorder(new TitledBorder(null, lineType,
									TitledBorder.LEADING, TitledBorder.TOP,
									null, null));
				}
				if (currentTab == null) {
					throw new TemplateException(
							"Creating row but no tab created yet to add the row to...");
				}
				currentTab.add(currentRow);
				continue;
			}

			lineType = getPanelName(line);
			if (StringUtils.isNotBlank(lineType)) {
				PanelConfig config = inputConfigs.get(lineType);

				AbstractInputPanel iPanel = createInputPanel(templateFileName,
						config);
				inputPanels.put(lineType, iPanel);
				for (Validator<String> val : config.getValidators()) {

					try {
						Method method = val.getClass().getMethod(
								"setServiceInterface", ServiceInterface.class);
						method.invoke(val, si);
					} catch (Exception e) {
						// doesn't matter. apparently serviceinterface is not
						// needed by this validator
					}

					if (iPanel.getJComboBox() != null) {
						validationPanel.getValidationGroup().add(
								iPanel.getJComboBox(), val);
					} else if (iPanel.getTextComponent() != null) {
						validationPanel.getValidationGroup().add(
								iPanel.getTextComponent(), val);
					}
				}

				if (iPanel == null) {
					throw new TemplateException(
							"Can't find panel for panelName: " + lineType);
				}

				if (currentRow == null) {
					throw new TemplateException(
							"No row created when trying to add panel with name: "
									+ lineType);
				}

				if (iPanel.isDisplayed()) {
					currentRow.add(iPanel);
				}
				continue;
			}

		}

		for (AbstractInputPanel panel : inputPanels.values()) {
			panel.setServiceInterface(si);
			panel.initPanel(template, template.getJobSubmissionObject());
		}

		JPanel mainPanel = null;
		// now create the tabs
		if (tabs.size() > 1) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			JTabbedPane tabbedPanel = new JTabbedPane();

			for (String tabname : tabs.keySet()) {
				tabbedPanel.addTab(tabname, createTab(tabs.get(tabname), useScrollbars));
			}

			mainPanel.add(tabbedPanel, BorderLayout.CENTER);

		} else {
			mainPanel = createTab(tabs.values().iterator().next(), useScrollbars);
		}

		template.setTemplatePanel(mainPanel);
		template.setInputPanels(inputPanels);
		template.setValidationPanel(validationPanel);

		// init jobobject with default values
		template.userInput(null, null);

		return template;
	}

	public static LinkedHashMap<String, PanelConfig> parseConfig(
			final List<String> lines) throws TemplateException {

		LinkedHashMap<String, PanelConfig> panels = new LinkedHashMap<String, PanelConfig>();
		String currentPanel = null;
		PanelConfig currentConfig = null;

		boolean firstPanelReached = false;

		for (String line : lines) {

			line = line.trim();

			if (StringUtils.isBlank(line) || line.startsWith("#")) {
				continue;
			}

			if (!firstPanelReached && line.startsWith("=")) {
				firstPanelReached = true;
			}

			if (!firstPanelReached) {
				continue;
			}

			String panelName = getPanelName(line);
			if (StringUtils.isNotBlank(panelName)) {
				// means new or first panel
				if ((currentPanel != null) && (currentConfig != null)) {

					panels.put(currentPanel, currentConfig);
					currentPanel = null;
					currentConfig = null;
				}

				currentPanel = panelName;
				currentConfig = new PanelConfig();
				currentConfig.addConfig(AbstractInputPanel.NAME, panelName);
			} else {

				if ((getNewPageIndicator(line) != null)
						|| (getNewRowIndicator(line) != null)) {
					// that's ok
					continue;
				}

				if (StringUtils.isBlank(currentPanel)) {
					// means no current panel so nothing to configure
					throw new TemplateException(
							"No panel specified for confg line: " + line);
				}

				addValueToConfig(currentConfig, line);

			}
		}

		if ((currentPanel != null) && (currentConfig != null)) {
			panels.put(currentPanel, currentConfig);
		}

		return panels;
	}

}
