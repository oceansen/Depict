package no.simula.depict.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.util.List;

import no.simula.depict.model.TestCaseInteraction;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class InteractionBarchart 
{
	public static JFreeChart createGraph(List<TestCaseInteraction> tcis)
	{
		CategoryDataset dataset = createDataset(tcis);
		
		String title = "Test cases coverage";
		return createChart(dataset, title);
	}

	private static CategoryDataset createDataset(List<TestCaseInteraction> tcis) 
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		if (tcis == null)
		{
        	dataset.addValue(0, "", "");
        	return dataset;
		}
		
        for (TestCaseInteraction tci : tcis)
        	dataset.addValue(tci.getInteractionsCount(), "", tci.getTestCase().getFullName());
        
        return dataset;
	}
	
    private static JFreeChart createChart(CategoryDataset dataset, String title) 
    {
        // create the chart...
        //final JFreeChart chart = ChartFactory.createBarChart(
        final JFreeChart chart = ChartFactory.createBarChart3D(
            title,				      // chart title
            "Test cases",             // domain axis label
            "Interactions",           // range axis label
            dataset,                  // data
            PlotOrientation.HORIZONTAL, // orientation
            false,                     // include legend
            true,                     // tooltips?
            false                     // URLs?
        );

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        //final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        final BarRenderer3D renderer = (BarRenderer3D) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        
        // set up gradient paints for series...
        final GradientPaint gp0 = new GradientPaint(
            0.0f, 0.0f, Color.blue, 
            0.0f, 0.0f, Color.lightGray
        );
        /*
        final GradientPaint gp1 = new GradientPaint(
            0.0f, 0.0f, Color.green, 
            0.0f, 0.0f, Color.lightGray
        );
        final GradientPaint gp2 = new GradientPaint(
            0.0f, 0.0f, Color.red, 
            0.0f, 0.0f, Color.lightGray
        );
        */
        renderer.setSeriesPaint(0, gp0);
        //renderer.setSeriesPaint(1, gp1);
        //renderer.setSeriesPaint(2, gp2);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        //domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        domainAxis.setLabelFont(new Font("Arial Unicode MS", 0, 12));
        domainAxis.setTickLabelFont(new Font("Arial Unicode MS", 0, 9));
        return chart;
    }
}
