package grisu.frontend.view.swing.settings;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GrisuTemplateFileFilter extends FileFilter {

	@Override
	public boolean accept(File arg0) {

		if (arg0.isDirectory()) {
			return true;
		}

		if (arg0.getName().endsWith(".template")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return ("Grisu template files");
	}

}
