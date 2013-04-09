package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.RemoteFileSystemException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.FileManager;
import grisu.model.GrisuRegistryManager;

public class InputFileParser extends SingleInputFile {

	public InputFileParser(String name, PanelConfig config)
			throws TemplateException {
		super(name, config);
		// TODO Auto-generated constructor stub
	}

	
	protected void fileChanged(){
		if (!isInitFinished()) {
			return;
		}

		if (selectedFile != null) {
			removeValue("inputFileUrl", selectedFile);
		}
		selectedFile = (String) getComboBox().getSelectedItem();

		addValue("inputFileUrl", selectedFile);

		FileManager fm = GrisuRegistryManager.getDefault(getServiceInterface()).getFileManager();
		
		String inputFileContent=null;
		try {
			inputFileContent = fm.getFileContent(selectedFile);
			String[] fileLines = inputFileContent.split("\n");
			for(String line: fileLines){
				System.out.println("line: "+line);
				if(line.contains("%Mem"))
				{
					int startIndex = line.indexOf("=")+1;
					setValue("memory", line.substring(startIndex, line.length()-1));
				}
				else if(line.contains("%NProcShared")){
					int startIndex = line.indexOf("=")+1;
					setValue("cpus", Integer.parseInt(line.substring(startIndex)));
				}
			}
			
		} catch (RemoteFileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(inputFileContent);
	//			fm.getFileContent(url)
	//			addValue("cpus", 4)
				
				addHistoryValue(selectedFile);

	}
}
