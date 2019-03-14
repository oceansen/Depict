package no.simula.depict.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import no.simula.depict.model.TestCaseInteraction;
import no.simula.depict.ui.InteractionBarchart;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class HtmlTestcaseExport 
{
	private List<TestCaseInteraction> testCaseInteractions;
	private boolean holeView;
	private String modelName;
	
	public HtmlTestcaseExport(String modelName, List<TestCaseInteraction> testCaseInteractions, boolean holeView)
	{
		this.testCaseInteractions = testCaseInteractions;
		this.holeView = holeView;
		this.modelName = modelName;
	}
	
	public void export(String fileName) throws IOException
	{
		if (testCaseInteractions == null)
			return;
		
		Path path = FileSystems.getDefault().getPath(fileName);
		StringBuffer sb = new StringBuffer();
		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n");
		sb.append("<TITLE>DEPICT result page</TITLE>\n");
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<style type=\"text/css\">" +
			"table.gridtable {" +
			"font-family: verdana,arial,sans-serif;" +
			"font-size:11px;" +
			"color:#333333;" +
			"border-width: 1px;" +
			"border-color: #666666;" +
			"border-collapse: collapse;" +
		"}" +
		"table.gridtable th {" +
		"	border-width: 1px;" +
		"	padding: 8px;" +
		"	border-style: solid;" +
		"	border-color: #666666;" +
		"	background-color: #dedede;" +
		"}" +
		"table.gridtable td {" + 
		"	border-width: 1px;" +
		"	padding: 8px;" +
		"	border-style: solid;" +
		"	border-color: #666666;" +
		"	background-color: #ffffff;" +
		"}" +
		"</style>");
		sb.append("</head>\n");
		sb.append("<h2 style=\"font-family: verdana,arial,sans-serif;\">DEPICT report</h2>\n");
		sb.append(String.format("<h3 style=\"font-family: verdana,arial,sans-serif;\">Model: %s</h3>\n", modelName));
		sb.append("<hr/><br/>\n");
		sb.append("<table class=\"gridtable\">\n");
		sb.append("<th>Id</th><th>Test case</th><th>Count</th><th>Expression</th><th>SQL</th><th>Elapsed [msec.]</th>\n");
		
		addTestCaseInteractionCombinationHtml(sb);
		
		sb.append("</table>\n");

		//
		// Add bar chart
		String imgPath;
		if ((imgPath = saveBarchartImageFile(path)) != null)
			sb.append(String.format("<p><img src=\"%s\"/></p>", imgPath));

		sb.append("<br/><hr/>\n");
		
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd MMM yyyy");
		sb.append(String.format("<p style=\"font-family: verdana,arial,sans-serif;\">Report generated at: %s</p>", sdf.format(cal.getTime())));

		sb.append("<body>\n");
		sb.append("</body>\n");
		sb.append("</html>\n");
		
		Files.deleteIfExists(path);
		Files.write(path, sb.toString().getBytes(), StandardOpenOption.CREATE);
	}

	private String saveBarchartImageFile(Path embeddingHtmlFilePath) throws IOException
	{
		final String IMG_FILE_PATH_SUFFIX = ".barchart.png";
		
		JFreeChart chart = InteractionBarchart.createGraph(testCaseInteractions);
		ChartUtilities.saveChartAsPNG(new File(embeddingHtmlFilePath.toString() + IMG_FILE_PATH_SUFFIX), chart, 750, 35 * testCaseInteractions.size());
		String imgFileName = embeddingHtmlFilePath.getFileName().toString() + IMG_FILE_PATH_SUFFIX;
		return imgFileName;
	}
	
	private void addTestCaseInteractionCombinationHtml(StringBuffer sb)
	{
		String ROW_NOCOLOR = "";
		String ROW_HIGHLIGHT = "style=\"background-color:yellow\"";
		
		String rowFormat = "<tr><td %s>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
		
		for (TestCaseInteraction tci : testCaseInteractions)
		{
			if (!holeView)
				sb.append(String.format(rowFormat, 
						tci.getInteractionsCount() == 0 ? ROW_HIGHLIGHT : ROW_NOCOLOR, 
						tci.getInteractionsId(),		
						tci.getTestCase().getFullName(), 
						String.valueOf(tci.getInteractionsCount()), 
						tci.getExpressions().toString(), 
						tci.getSqlStatement(),
						String.valueOf(tci.getElapsedTimeMillis())
					)
				);
			else
			{
				if (tci.getInteractionsCount() == 0)
					sb.append(String.format(rowFormat, 
							ROW_HIGHLIGHT, 
							tci.getInteractionsId(), 
							tci.getTestCase().getFullName(), 
							String.valueOf(tci.getInteractionsCount()), 
							tci.getExpressions().toString(), 
							tci.getSqlStatement(),
							String.valueOf(tci.getElapsedTimeMillis())
						)
					);
			}
	    }
	}
}
