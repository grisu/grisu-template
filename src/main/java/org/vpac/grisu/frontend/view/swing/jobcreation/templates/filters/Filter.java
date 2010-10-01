package org.vpac.grisu.frontend.view.swing.jobcreation.templates.filters;

import java.util.Map;

public interface Filter {

	public abstract void config(Map<String, String> config);

	public abstract String filter(String value);

}