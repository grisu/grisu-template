package grisu.frontend.view.swing.jobcreation.templates;

import grisu.frontend.view.swing.jobcreation.templates.filters.Filter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Validator;

public class PanelConfig {

	private final Map<String, String> config = new HashMap<String, String>();
	private final LinkedList<Filter> filters = new LinkedList<Filter>();
	private final LinkedList<Validator<String>> validators = new LinkedList<Validator<String>>();

	private String type;

	public PanelConfig() {
	}

	public void addConfig(String key, String value) {
		config.put(key, value);
	}

	public void addFilter(Filter filter) {
		filters.add(filter);
	}

	public void addValidator(Validator<String> validator) {
		validators.add(validator);
	}

	public LinkedList<Filter> getFilters() {
		return filters;
	}

	public String getPanelType() {
		return type;
	}

	public Map<String, String> getProperties() {
		return config;
	}

	public String getType() {
		if (StringUtils.isBlank(type)) {
			return "StringInput";
		} else {
			return type;
		}
	}

	public List<Validator<String>> getValidators() {
		return validators;
	}

	public void setType(String type) {
		this.type = type;
	}

}
