package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import org.apache.commons.lang3.StringUtils;

import grisu.control.exceptions.RemoteFileSystemException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.FileManager;
import grisu.model.GrisuRegistryManager;

public class InputFileParser extends SingleInputFile {

	public InputFileParser(String name, PanelConfig config)
			throws TemplateException {
		super(name, config);
	}

	@Override
	protected void fileChanged() {
		if (!isInitFinished()) {
			return;
		}

		if (selectedFile != null) {
			removeValue("inputFileUrl", selectedFile);
		}
		selectedFile = (String) getComboBox().getSelectedItem();

		addValue("inputFileUrl", selectedFile);

		if (Boolean.parseBoolean(getPanelProperty(SET_AS_STDIN))) {
			try {
				setValue("stdin", FileManager.getFilename(selectedFile));
			} catch (TemplateException e) {
				myLogger.debug("Can't set stdin value: "
						+ e.getLocalizedMessage());
				return;
			}
		}

		if (StringUtils.isBlank(selectedFile)) {
			return;
		}

		FileManager fm = GrisuRegistryManager.getDefault(getServiceInterface())
				.getFileManager();

		String inputFileContent = null;
		try {
			inputFileContent = fm.getFileContent(selectedFile);
			String[] fileLines = inputFileContent.split("\n");
			for (String line : fileLines) {
				System.out.println("line: " + line);
				if (line.contains("%Mem")) {
					int startIndex = line.indexOf("=") + 1;
					String mem = line.substring(startIndex, line.length() - 1);
					setValue("memory", mem);
				} else if (line.contains("%NProcShared")) {
					int startIndex = line.indexOf("=") + 1;
					String cpus = line.substring(startIndex);
					setValue("cpus", Integer.parseInt(cpus));
				}
			}

		} catch (RemoteFileSystemException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}

		addHistoryValue(selectedFile);

	}
}
