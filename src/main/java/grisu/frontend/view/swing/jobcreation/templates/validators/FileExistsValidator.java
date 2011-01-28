package grisu.frontend.view.swing.jobcreation.templates.validators;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.RemoteFileSystemException;
import grisu.model.FileManager;
import grisu.model.GrisuRegistryManager;

import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

public class FileExistsValidator implements Validator {

	private ServiceInterface si;
	private FileManager fm;

	public void setServiceInterface(ServiceInterface si) {
		this.si = si;
		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
	}

	public boolean validate(Problems arg0, String arg1, Object arg2) {

		if (StringUtils.isBlank((String) arg2)) {
			// arg0.add("No input file specified.");
			return true;
		}

		if (this.fm == null) {
			return true;
		}

		final String file = (String) arg2;

		try {
			if (this.fm.fileExists(file)) {
				return true;
			}
		} catch (final RemoteFileSystemException e) {
			// doesn't matter, does it?
		}
		arg0.add("File " + FileManager.getFilename(file) + " doesn't exist.");
		return false;
	}

}
