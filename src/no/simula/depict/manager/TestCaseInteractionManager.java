package no.simula.depict.manager;

import java.util.ArrayList;
import java.util.List;

import no.simula.depict.model.TestCaseInteraction;

public class TestCaseInteractionManager 
{
	public List<String> getTableIntersection(List<TestCaseInteraction> tcis)
	{
		List<String> r = new ArrayList<String>();
		if (tcis == null || tcis.size() == 0)
			return r;

		r.addAll(tcis.get(0).getTableNames());
		
		for (TestCaseInteraction tci : tcis)
			r = intersect(r, tci.getTableNames());
		
		return r;
	}
	
	private List<String> intersect(List<String> a, List<String> b)
	{
		List<String> r = new ArrayList<String>();
		
		for (String s : a)
		{
			if (b.contains(s) && !r.contains(s))
				r.add(s);
		}
		return r;
	}
}
