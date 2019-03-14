
package no.simula.depict.wizards;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import no.simula.depict.data.TableColumnMetadata;
import no.simula.depict.model.DepictEdge;

public class EditSurrogateRelationshipDialog extends JDialog implements ActionListener
{
	private static final String OK_CMD = "OK";
	private static final String CANCEL_CMD = "Cancel";
	
	private static final long serialVersionUID = 1L;
	private DepictEdge edge;
	private KeyComboBoxModel aColumnsComboBoxModel;
	private KeyComboBoxModel bColumnsComboBoxModel;
	private KeyListModel foreignKeyListModel;
	private KeyListModel primaryKeyListModel;
	private boolean ok = false;
	private Image imgAdd;
	private Image imgUp;
	private Image imgDn;
	private Image imgDel;
	private boolean readOnly = false;
	private boolean rightTablePrimary = true;
	private boolean selfRelationship;
	
	public EditSurrogateRelationshipDialog(final Frame parent, DepictEdge edge, List<TableColumnMetadata> foreignTableCols, List<TableColumnMetadata> primaryTableCols, boolean readOnly) 
	{
		super(parent, "Surrogate relationship editor", true);
		this.edge = edge;
		this.readOnly = readOnly;
		
		selfRelationship = edge.getForeignTableName().equals(edge.getPrimaryTableName()) ? true : false;

		aColumnsComboBoxModel = new KeyComboBoxModel(foreignTableCols);
		bColumnsComboBoxModel = new KeyComboBoxModel(primaryTableCols);
		
		foreignKeyListModel = new KeyListModel();
		for (String s : edge.getForeignKeyColumns())
		{
			int i = foreignTableCols.indexOf(new TableColumnMetadata(s));
			TableColumnMetadata tcmd = foreignTableCols.get(i);
			foreignKeyListModel.addElement(tcmd);
			
			aColumnsComboBoxModel.removeElement(tcmd);
		}
		
		primaryKeyListModel = new KeyListModel();
		for (String s : edge.getPrimaryKeyColumns())
		{
			int i = primaryTableCols.indexOf(new TableColumnMetadata(s));
			TableColumnMetadata tcmd = primaryTableCols.get(i);
			primaryKeyListModel.addElement(tcmd);
			bColumnsComboBoxModel.removeElement(tcmd);
		}

		if (parent != null) 
		{
			Dimension parentSize = parent.getSize();
			Point p = parent.getLocation();
			setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
	    }
		
		setSize(new Dimension(390, 300));
		setResizable(false);
		loadImages();
		
		init();
		
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    pack(); 
	    setVisible(true);
	}

	private void loadImages()
	{
	    Bundle bundle = FrameworkUtil.getBundle(this.getClass());
	    URL url; 

	    try 
	    {
		    url = FileLocator.find(bundle, new Path("icons/1390414194_plus-sign.png"), null);
	    	imgAdd = ImageIO.read(url);

	    	url = FileLocator.find(bundle, new Path("icons/1390416272_55.png"), null);
	    	imgUp = ImageIO.read(url);

	    	url = FileLocator.find(bundle, new Path("icons/1390416306_54.png"), null);
	    	imgDn = ImageIO.read(url);

	    	url = FileLocator.find(bundle, new Path("icons/1390416428_trash.png"), null);
	    	imgDel = ImageIO.read(url);
		} 
	    catch (IOException e) 
	    {
			e.printStackTrace();
		}
	}
	
	private void init()
	{
		GridBagConstraints c = new GridBagConstraints();
	    JPanel pnlForm = new JPanel(new GridBagLayout());
	    Dimension buttonDimension = new Dimension(25, 25);
	    
	    //
	    // Row 1
	    JLabel lblA = new JLabel("Table: " + edge.getForeignTableName());
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.gridy = 0;
	    c.weightx = 0.5;
	    c.insets = new Insets(10, 10, 0, 0);
	    c.gridwidth = 2;
	    c.anchor = GridBagConstraints.PAGE_START;
	    pnlForm.add(lblA, c);
	    
	    JLabel lblB = new JLabel("Table: " + edge.getPrimaryTableName());
	    c.gridx = 2;
	    c.gridy = 0;
	    c.weightx = 0.5;
	    c.insets = new Insets(10, 10, 0, 0);
	    c.gridwidth = 2;
	    c.anchor = GridBagConstraints.PAGE_START;
	    pnlForm.add(lblB, c);
	    
	    //
	    // Row 1
	    final JRadioButton rdoPrimary = new JRadioButton("Primary");
	    rdoPrimary.setActionCommand("rdoPrimary");
	    rdoPrimary.setEnabled(false);
	    rdoPrimary.setSelected(false);
	    rdoPrimary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				rightTablePrimary = !rdoPrimary.isSelected();
			}
	    });
	    c.gridx = 0;
	    c.gridy = 1;
	    pnlForm.add(rdoPrimary, c);
	    
	    final JRadioButton rdoSecondary = new JRadioButton("Primary");
	    rdoSecondary.setActionCommand("rdoSecondary");
	    rdoSecondary.setEnabled(false);
	    rdoSecondary.setSelected(true);
	    rdoSecondary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				rightTablePrimary = rdoSecondary.isSelected();
			}
	    });
	    c.gridx = 2;
	    c.gridy = 1;
	    pnlForm.add(rdoSecondary, c);

	    ButtonGroup group = new ButtonGroup();
	    group.add(rdoPrimary);
	    group.add(rdoSecondary);

	    if (selfRelationship)
	    {
		    rdoPrimary.setEnabled(true);
		    rdoSecondary.setEnabled(true);
	    }
	    
	    c.gridwidth = 1;
	    //
	    // Row 2
	    final JComboBox<TableColumnMetadata> cboForeignCols = new JComboBox<TableColumnMetadata>();
	    cboForeignCols.setModel(aColumnsComboBoxModel);
	    c.gridx = 0;
	    c.gridy = 2;
	    c.weightx = 0.8;
	    c.insets = new Insets(10, 10, 0, 0);
	    pnlForm.add(cboForeignCols, c);

	    JButton cmdAddA = new JButton();
	    cmdAddA.setToolTipText("Add a column to the relationship");
	    cmdAddA.setIcon(new ImageIcon(imgAdd));
	    cmdAddA.setPreferredSize(buttonDimension);
	    cmdAddA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (cboForeignCols.getSelectedIndex() != -1)
				{
					TableColumnMetadata c = (TableColumnMetadata) cboForeignCols.getSelectedItem();
					foreignKeyListModel.addElement(c);
					aColumnsComboBoxModel.removeElement(c);
				}
			}});
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.LINE_END;
	    c.gridx = 1;
	    c.gridy = 2;
	    c.weightx = 0.2;
	    c.insets = new Insets(10, 10, 0, 15);
	    pnlForm.add(cmdAddA, c);
	    
	    final JComboBox<TableColumnMetadata> cboPrimaryCols = new JComboBox<TableColumnMetadata>();
	    cboPrimaryCols.setModel(bColumnsComboBoxModel);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 2;
	    c.gridy = 2;
	    c.weightx = 0.8;
	    c.insets = new Insets(10, 10, 0, 0);
	    pnlForm.add(cboPrimaryCols, c);

	    JButton cmdAddB = new JButton();
	    cmdAddB.setToolTipText("Add a column to the relationship");
	    cmdAddB.setIcon(new ImageIcon(imgAdd));
	    cmdAddB.setPreferredSize(buttonDimension);
	    cmdAddB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (cboPrimaryCols.getSelectedIndex() != -1)
				{
					TableColumnMetadata c = (TableColumnMetadata) cboPrimaryCols.getSelectedItem();
					primaryKeyListModel.addElement(c);
					bColumnsComboBoxModel.removeElement(c);
				}
			}});
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.LINE_END;
	    c.gridx = 3;
	    c.gridy = 2;
	    c.weightx = 0.2;
	    c.insets = new Insets(10, 10, 0, 10);
	    c.gridwidth = 1;
	    pnlForm.add(cmdAddB, c);
	    
	    c.anchor = GridBagConstraints.CENTER;
	    
	    //
	    // Row 3
	    final JList<TableColumnMetadata> lstForeignKeyCols = new JList<TableColumnMetadata>();
	    lstForeignKeyCols.setModel(foreignKeyListModel);
	    lstForeignKeyCols.setPreferredSize(new Dimension(150, 120));
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.anchor = GridBagConstraints.LINE_START;
	    c.gridx = 0;
	    c.gridy = 3;
	    c.insets = new Insets(10, 10, 10, 0);
	    c.weightx = 0;
	    c.gridheight = 3;
	    pnlForm.add(lstForeignKeyCols, c);

	    c.gridwidth = 1;
	    c.gridheight = 1;
	    
	    //
	    // Row 4
	    JButton cmdUpA = new JButton();
	    cmdUpA.setToolTipText("Move the selected column one position up");
	    cmdUpA.setIcon(new ImageIcon(imgUp));
	    cmdUpA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
			    int i = lstForeignKeyCols.getSelectedIndex();
			    if (i < 0)
			    	return;
			    foreignKeyListModel.swapElements(i, i - 1);
			    lstForeignKeyCols.setSelectedIndex(i - 1);
			    lstForeignKeyCols.updateUI();				
			}});
	    cmdUpA.setPreferredSize(buttonDimension);
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.LINE_END;
	    c.gridx = 1;
	    c.gridy = 3;
	    c.weightx = 0.2;
	    c.insets = new Insets(10, 10, 0, 15);
	    pnlForm.add(cmdUpA, c);

	    JButton cmdDownA = new JButton();
	    cmdDownA.setToolTipText("Move the selected column one position down");
	    cmdDownA.setIcon(new ImageIcon(imgDn));
	    cmdDownA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
			    int i = lstForeignKeyCols.getSelectedIndex();
			    if (i < 0)
			    	return;
			    foreignKeyListModel.swapElements(i + 1, i);
			    lstForeignKeyCols.setSelectedIndex(i + 1);
			    lstForeignKeyCols.updateUI();				
			}});
	    cmdDownA.setPreferredSize(buttonDimension);
	    c.gridx = 1;
	    c.gridy = 4;
	    c.insets = new Insets(0, 10, 0, 15);
	    pnlForm.add(cmdDownA, c);
	    
	    JButton cmdDelA = new JButton();
	    cmdDelA.setIcon(new ImageIcon(imgDel));
	    cmdDelA.setToolTipText("Remove the selected column form the list");
	    cmdDelA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (lstForeignKeyCols.getSelectedIndex() != -1)
				{
					aColumnsComboBoxModel.addElement(lstForeignKeyCols.getSelectedValue());
					foreignKeyListModel.removeElement(lstForeignKeyCols.getSelectedValue());
				}
			}});
	    cmdDelA.setPreferredSize(buttonDimension);
	    c.gridx = 1;
	    c.gridy = 5;
	    c.insets = new Insets(0, 10, 0, 15);
	    pnlForm.add(cmdDelA, c);

	    final JList<TableColumnMetadata> lstPrimaryKeyCols = new JList<TableColumnMetadata>();
	    lstPrimaryKeyCols.setModel(primaryKeyListModel);
	    lstPrimaryKeyCols.setPreferredSize(new Dimension(150, 120));
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 2;
	    c.gridy = 3;
	    c.insets = new Insets(10, 10, 10, 0);
	    c.weightx = 0;
	    c.gridheight = 3;
	    pnlForm.add(lstPrimaryKeyCols, c);

	    c.gridwidth = 1;
	    c.gridheight = 1;
	    
	    JButton cmdUpB = new JButton();
	    cmdUpB.setToolTipText("Move the selected column one position up");
	    cmdUpB.setIcon(new ImageIcon(imgUp));
	    cmdUpB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
			    int i = lstPrimaryKeyCols.getSelectedIndex();
			    if (i < 0)
			    	return;
			    primaryKeyListModel.swapElements(i, i - 1);
			    i = i - 1;
			    lstPrimaryKeyCols.setSelectedIndex(i);
			    lstPrimaryKeyCols.updateUI();				
			}});
	    cmdUpB.setPreferredSize(buttonDimension);
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.LINE_END;
	    c.gridx = 3;
	    c.gridy = 3;
	    c.weightx = 0.2;
	    c.insets = new Insets(10, 10, 0, 10);
	    pnlForm.add(cmdUpB, c);

	    JButton cmdDownB = new JButton();
	    cmdDownB.setIcon(new ImageIcon(imgDn));
	    cmdDownB.setToolTipText("Move the selected column one position down");
	    cmdDownB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
			    int i = lstPrimaryKeyCols.getSelectedIndex();
			    if (i < 0)
			    	return;
			    primaryKeyListModel.swapElements(i + 1, i);
			    lstPrimaryKeyCols.setSelectedIndex(i + 1);
			    lstPrimaryKeyCols.updateUI();				
			}});
	    cmdDownB.setPreferredSize(buttonDimension);
	    c.gridx = 3;
	    c.gridy = 4;
	    c.insets = new Insets(0, 10, 0, 10);
	    pnlForm.add(cmdDownB, c);
	    
	    JButton cmdDelB = new JButton();
	    cmdDelB.setIcon(new ImageIcon(imgDel));
	    cmdDelB.setToolTipText("Remove the selected column form the list");
	    cmdDelB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (lstPrimaryKeyCols.getSelectedIndex() != -1)
				{
					bColumnsComboBoxModel.addElement(lstPrimaryKeyCols.getSelectedValue());
					primaryKeyListModel.removeElement(lstPrimaryKeyCols.getSelectedValue());
				}
			}});
	    cmdDelB.setPreferredSize(buttonDimension);
	    c.gridx = 3;
	    c.gridy = 5;
	    c.insets = new Insets(0, 10, 0, 10);
	    pnlForm.add(cmdDelB, c);

		JPanel pnlCommands = new JPanel();
		pnlCommands.setLayout(new BoxLayout(pnlCommands, BoxLayout.LINE_AXIS));
		pnlCommands.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		pnlCommands.add(Box.createHorizontalGlue());
		
	    JButton cmdOk = new JButton(OK_CMD);
	    cmdOk.addActionListener(this);
	    cmdOk.setPreferredSize(new Dimension(75, 30));
	    pnlCommands.add(cmdOk);
		pnlCommands.add(Box.createRigidArea(new Dimension(10, 0)));

	    JButton cmdCancel = new JButton(CANCEL_CMD);
	    cmdCancel.addActionListener(this);
	    cmdCancel.setPreferredSize(new Dimension(75, 30));
	    pnlCommands.add(cmdCancel);

	    add(pnlForm, BorderLayout.CENTER);
		add(pnlCommands, BorderLayout.PAGE_END);

		if (readOnly)
		{
			cboForeignCols.setEnabled(false);
			cboPrimaryCols.setEnabled(false);
			lstForeignKeyCols.setEnabled(false);
			lstPrimaryKeyCols.setEnabled(false);
			cmdAddA.setEnabled(false);
			cmdUpA.setEnabled(false);
			cmdDownA.setEnabled(false);
			cmdDelA.setEnabled(false);
			cmdAddB.setEnabled(false);
			cmdUpB.setEnabled(false);
			cmdDownB.setEnabled(false);
			cmdDelB.setEnabled(false);
			cmdOk.setEnabled(false);
		}

		if (selfRelationship)
			cmdOk.setEnabled(true);
	}
	
	private void verify() throws Exception
	{
		if (foreignKeyListModel.size() == 0 || primaryKeyListModel.size() == 0)
			throw new Exception("You must select at least one column for each table!");
			
		if (foreignKeyListModel.size() != primaryKeyListModel.size())
			throw new Exception("The two sets of key must have the same number of elements!");

		int i = 0;
		for (TableColumnMetadata t1 : getForeignKeySelectedColumns())
		{
			TableColumnMetadata t2 = getPrimaryKeySelectedColumns().get(i);
			if (t1.getSqlDataType() != t2.getSqlDataType())
				throw new Exception("Each element of the lists must have the same data type of its correspondant!");

			++i;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		if (ae.getActionCommand().equals(OK_CMD))
		{
			try
			{
				verify();
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null,
						"The definition of the surrogate relationship has one or more errors:\n" + e.getMessage(),
					    "Surrogate relationship editor",
					    JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			ok = true;
			dispose();
			
		}
		else if (ae.getActionCommand().equals(CANCEL_CMD))
		{
			ok = false;
			dispose();
		}
	}
	
	private class KeyComboBoxModel extends DefaultComboBoxModel<TableColumnMetadata>
	{
		private static final long serialVersionUID = 1L;

		public KeyComboBoxModel(List<TableColumnMetadata> keyColumns)
		{
			for (TableColumnMetadata tcm : keyColumns)
				this.addElement(tcm);
		}
	}
	
	private class KeyListModel extends DefaultListModel<TableColumnMetadata>
	{
		private static final long serialVersionUID = 1L;

		private void swapElements(int pos1, int pos2) 
		{
			if (pos1 < 0 || pos2 < 0 || pos1 >= size() || pos2 >= size())
				return;
			
			TableColumnMetadata e1 = get(pos1);
			set(pos1, get(pos2));
			set(pos2, e1);
		}

	}

	public boolean isOk() {
		return ok;
	}

	public List<TableColumnMetadata> getForeignKeySelectedColumns() 
	{
		Object[] data = foreignKeyListModel.toArray();
		List<TableColumnMetadata> l = new ArrayList<TableColumnMetadata>();
		for (Object o : data)
			l.add((TableColumnMetadata)o);
		
		return l;
	}

	public List<TableColumnMetadata> getPrimaryKeySelectedColumns() 
	{
		Object[] data = primaryKeyListModel.toArray();
		List<TableColumnMetadata> l = new ArrayList<TableColumnMetadata>();
		for (Object o : data)
			l.add((TableColumnMetadata)o);
		
		return l;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public boolean isRightTablePrimary() {
		return rightTablePrimary;
	}
}
