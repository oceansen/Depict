package no.simula.depict.ui.commands;

import no.simula.depict.ui.InteractionCoveragePublisher;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class RefreshDataInteractionCommand extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		//IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		/*
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		
		IResource r = ProjectUtility.toResource((IStructuredSelection) selection);
		
		if (r == null)
		{
			return null;
		}
		
		if (!ResourceUtility.isFile(r))
		{
			return null;
		}
		
		if (!r.getFileExtension().equals(GlobalSymbols.FILE_EXT_CTEXL))
		{
			return null;
		}
		*/
		/*
		MessageDialog.openInformation(
				window.getShell(),
				"Depict",
				"Model: " + r.getName());
		*/

		InteractionCoveragePublisher.getInstance().refresh();
		return null;
	}
}
