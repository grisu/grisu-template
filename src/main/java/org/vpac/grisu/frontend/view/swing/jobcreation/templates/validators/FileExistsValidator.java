package org.vpac.grisu.frontend.view.swing.jobcreation.templates.validators;

import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
import org.vpac.grisu.model.FileManager;
import org.vpac.grisu.model.GrisuRegistryManager;

public class FileExistsValidator implements Validator {

	private ServiceInterface si;
	private FileManager fm;

	public void setServiceInterface(ServiceInterface si) {
		this.si = si;
		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
	}

	public boolean validate(Problems arg0, String arg1, Object arg2) {

		System.out.println(arg2);
		if (StringUtils.isBlank((String) arg2)) {
			// arg0.add("No input file specified.");
			return true;
		}

		if (this.fm == null) {
			return true;
		}

		String file = (String) arg2;

		try {
			if (this.fm.fileExists(file)) {
				return true;
			}
		} catch (RemoteFileSystemException e) {
			// doesn't matter, does it?
		}
		arg0.add("File " + this.fm.getFilename(file) + " doesn't exist.");
		return false;
	}

}
