package no.simula.depict.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestCaseInteraction implements Serializable
{
	private static final long serialVersionUID = 9091764080875172846L;
	private CteTestCase testCase;
	private String sqlStatement;
	private int interactionsId;
	private int interactionsCount;
	private List<CteClassReference> classReferences;
	private List<String> expressions;
	private List<String> tableNames;
	private long elapsedTimeMillis;
	
	public TestCaseInteraction(CteTestCase testCase, List<CteClassReference> tcis) //, String bitmap)
	{
		this.testCase = testCase;
		classReferences = tcis;
		expressions = new ArrayList<String>();
		tableNames = new ArrayList<String>();
	}
	
	public CteTestCase getTestCase() {
		return testCase;
	}
	
	public String getSqlStatement() {
		return sqlStatement;
	}
	public void setSqlStatement(String sqlCountStatement) {
		this.sqlStatement = sqlCountStatement;
	}

	public int getInteractionsId() {
		return interactionsId;
	}
	public void setInteractionsId(int interactionsId) {
		this.interactionsId = interactionsId;
	}
	
	public List<CteClassReference> getClassReferences() {
		return classReferences;
	}

	public List<String> getExpressions() {
		return expressions;
	}

	public List<String> getTableNames() {
		return tableNames;
	}

	public int getInteractionsCount() {
		return interactionsCount;
	}

	public void setInteractionsCount(int interactionsCount) {
		this.interactionsCount = interactionsCount;
	}

	public long getElapsedTimeMillis() {
		return elapsedTimeMillis;
	}

	public void setElapsedTimeMillis(long elapsedTimeMillis) {
		this.elapsedTimeMillis = elapsedTimeMillis;
	}
}
