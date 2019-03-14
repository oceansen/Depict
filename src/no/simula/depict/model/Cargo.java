package no.simula.depict.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import no.simula.depict.data.TableMetadata;

import edu.uci.ics.jung.graph.Graph;

public class Cargo implements Serializable 
{
	private static final long serialVersionUID = 1337063904636118342L;

	private Graph<DepictVertex, DepictEdge> graph;
	private List<TestCaseInteraction> testCaseInteractions;
	private List<TableMetadata> tables;
	
	public Cargo(Graph<DepictVertex, DepictEdge> graph, List<TestCaseInteraction> tcis, List<TableMetadata> tables)
	{
		this.graph = graph;
		this.testCaseInteractions = tcis;
		this.tables = tables;
	}
	
	public Graph<DepictVertex, DepictEdge> getGraph() {
		return graph;
	}
	public void setGraph(Graph<DepictVertex, DepictEdge> graph) {
		this.graph = graph;
	}
	
	public List<TableMetadata> getTables() {
		if (tables == null)
			tables = new ArrayList<TableMetadata>();
		
		return tables;
	}

	public void setTables(List<TableMetadata> tables) {
		this.tables = tables;
	}

	public List<TestCaseInteraction> getTestCaseInteractions() {
		return testCaseInteractions;
	}

	public void setTestCaseInteractions(
			List<TestCaseInteraction> testCaseInteractions) {
		this.testCaseInteractions = testCaseInteractions;
	}
}
