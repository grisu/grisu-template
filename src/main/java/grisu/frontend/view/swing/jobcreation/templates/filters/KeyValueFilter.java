package grisu.frontend.view.swing.jobcreation.templates.filters;

import java.util.Map;

import org.python.antlr.PythonParser.return_stmt_return;

public class KeyValueFilter implements Filter {

	@Override
	public void config(Map<String, String> config) {
		// TODO Auto-generated method stub

	}

	//To display values in the combobox/textcombo in the form "key (value)" and use only the value part while processing
	@Override
	public String filter(String value) {
		// TODO Auto-generated method stub
		int startIndex = value.lastIndexOf("(");
		int endIndex = value.lastIndexOf(")");
		if(startIndex>=0 && endIndex >startIndex)
			return value.substring(startIndex+1, endIndex);
		return value;
	}

}
