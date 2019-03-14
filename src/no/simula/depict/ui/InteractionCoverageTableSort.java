package no.simula.depict.ui;

import no.simula.depict.model.TestCaseInteraction;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

public class InteractionCoverageTableSort extends ViewerComparator 
{
	private static final int COL_ID_INDEX = 0;
	private static final int COL_TESTCASE_INDEX = 1;
	private static final int COL_COUNT_INDEX = 2;
	
	private int propertyIndex;
	private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;
	
	public InteractionCoverageTableSort() 
	{
		this.propertyIndex = 0;
		direction = ASCENDING; //DESCENDING;
	}
	
	public int getDirection() 
	{
		return direction == DESCENDING ? SWT.DOWN : SWT.UP;
	}
	
	public void setColumn(int column) 
	{
		if (column == this.propertyIndex) 
		{
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} 
		else 
		{
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) 
	{
		int rc = 0;
		TestCaseInteraction tci1 = (TestCaseInteraction) e1;
		TestCaseInteraction tci2 = (TestCaseInteraction) e2;
		
		switch (propertyIndex)
		{
			case COL_ID_INDEX:
			if (tci1.getInteractionsId() > tci2.getInteractionsId())
				rc = 1;
			else if (tci1.getInteractionsId() < tci2.getInteractionsId())
				rc = -1;
			else
				rc = 0;
			break;
			
			case COL_TESTCASE_INDEX:
			rc = tci1.getTestCase().getFullName().compareTo(tci2.getTestCase().getFullName());
			break;

			case COL_COUNT_INDEX:
			if (tci1.getInteractionsCount() > tci2.getInteractionsCount())
				rc = 1;
			else if (tci1.getInteractionsCount() < tci2.getInteractionsCount())
				rc = -1;
			else
				rc = 0;
			break;
		}
		
		/*
	    Person p1 = (Person) e1;
	    Person p2 = (Person) e2;
	    switch (propertyIndex) {
	    case 0:
	      rc = p1.getFirstName().compareTo(p2.getFirstName());
	      break;
	    case 1:
	      rc = p1.getLastName().compareTo(p2.getLastName());
	      break;
	    case 2:
	      rc = p1.getGender().compareTo(p2.getGender());
	      break;
	    case 3:
	      if (p1.isMarried() == p2.isMarried()) {
	        rc = 0;
	      } else
	        rc = (p1.isMarried() ? 1 : -1);
	      break;
	    default:
	      rc = 0;
	    }
	    */
	    // If descending order, flip the direction
	    if (direction == DESCENDING) 
	    {
	    	rc = -rc;
	    }
	    return rc;
    }
}
