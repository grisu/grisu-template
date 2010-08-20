package org.vpac.grisu.frontend.view.swing.jobcreation.templates.validators;

import java.util.SortedSet;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.model.GrisuRegistryManager;

public class JobnameValidator implements Validator {

	private ServiceInterface si;

	public void setServiceInterface(ServiceInterface si) {
		this.si = si;
	}

	public boolean validate(Problems arg0, String arg1, Object arg2) {

		if (si == null) {
			arg0.add("Can't access joblist. Not logged in?");
			return false;
		}

		// TODO: this is not very fast, need to cache it somehow
		SortedSet<String> allJobnames = GrisuRegistryManager.getDefault(si)
				.getUserEnvironmentManager().getCurrentJobnames(false);

		if (allJobnames.contains(arg2)) {
			arg0.add("Jobname " + arg2 + " already exists.");
			return false;
		}

		return true;
	}

}
