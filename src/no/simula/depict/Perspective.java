package no.simula.depict;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory 
{
	private static final String FOLDER_DEPICT_VIEWS = "depict-folder-views";
	
	public void createInitialLayout(IPageLayout layout) 
	{
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		layout.addStandaloneView(GlobalSymbols.VIEW_ID_NAVIGATOR, true, IPageLayout.LEFT, 0.2f, editorArea);
		//layout.addStandaloneView("org.eclipse.ui.navigator.ProjectExplorer", true, IPageLayout.LEFT, 0.2f, editorArea);
		//layout.getViewLayout("org.eclipse.ui.navigator.ProjectExplorer").setCloseable(false);

		IFolderLayout folderMain = layout.createFolder("depict-main-folder", IPageLayout.TOP, 0.7f, editorArea);
		folderMain.addView(GlobalSymbols.VIEW_ID_INTERACTION_COVERAGE_TABLE);
		//folderMain.addView(GlobalSymbols.VIEW_ID_SURGICALQUERY);
		folderMain.addView(GlobalSymbols.VIEW_ID_INTERACTION_COVERAGE_GRAPH);
		//folderMain.addView("no.simula.depict.view1");

		IFolderLayout folderTools = layout.createFolder("depict-folder-tools", IPageLayout.LEFT, 0.3f, editorArea);
		folderTools.addView(GlobalSymbols.VIEW_ID_MODELGRAPH);
		
		IFolderLayout folderFilter = layout.createFolder("depict-folder-filter", IPageLayout.LEFT, 0.4f, editorArea);
		folderFilter.addView(GlobalSymbols.VIEW_ID_DBFILTER);
		
		IFolderLayout folder = layout.createFolder(FOLDER_DEPICT_VIEWS, IPageLayout.RIGHT, 0.3f, editorArea);
		//folder.addView(GlobalSymbols.VIEW_ID_PROPERTYSHEET);
		folder.addView(GlobalSymbols.VIEW_ID_DBINFO);
		folder.addView(GlobalSymbols.VIEW_ID_LOG);
	}
}
