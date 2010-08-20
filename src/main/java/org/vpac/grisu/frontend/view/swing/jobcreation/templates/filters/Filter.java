package org.vpac.grisu.frontend.view.swing.jobcreation.templates.filters;

public interface Filter {

	public abstract void config(String[] config);

	public abstract String filter(String value);

}