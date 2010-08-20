package org.vpac.grisu.frontend.view.swing.jobcreation.templates.filters;

import org.vpac.grisu.utils.FileHelpers;

public class BasenameFilter implements Filter {

	public void config(String[] config) {
		// nothing to do
	}

	public String filter(String value) {

		if ( value.contains(",") ) {
			StringBuffer temp = new StringBuffer();
			for ( String url : value.split(",") ) {
				temp.append(FileHelpers.getFilename(url)+" ");
			}
			return temp.toString().trim();
		}

		return FileHelpers.getFilename(value).trim();
	}


}
