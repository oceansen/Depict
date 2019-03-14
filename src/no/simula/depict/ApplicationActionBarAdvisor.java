package no.simula.depict;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    protected void makeActions(IWorkbenchWindow window) {
    }

    protected void fillCoolBar(ICoolBarManager coolBar)
    {
    	IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        ToolBarContributionItem mainBar = new ToolBarContributionItem(toolbar, "main");
        coolBar.add(mainBar);
        //toolbar.add(saveAction);
    }
    
    protected void fillMenuBar(IMenuManager menuBar) {
    }
}
