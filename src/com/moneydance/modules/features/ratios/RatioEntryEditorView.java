/*
 * ************************************************************************
 * Copyright (C) 2012 Mennē Software Solutions, LLC
 *
 * This code is released as open source under the Apache 2.0 License:<br/>
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">
 * http://www.apache.org/licenses/LICENSE-2.0</a><br />
 * ************************************************************************
 */

package com.moneydance.modules.features.ratios;

import com.moneydance.apps.md.model.RootAccount;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;
import com.moneydance.awt.GridC;
import com.moneydance.modules.features.ratios.selector.AccountFilterSelectLabel;
import com.moneydance.modules.features.ratios.selector.RatioAccountSelector;
import com.moneydance.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Displays a UI to edit a ratio specification.
 *
 * @author Kevin Menningen
 */
class RatioEntryEditorView extends JPanel {
  private final ResourceProvider _resources;
  private final MoneydanceGUI _mdGui;

  private JCheckBox _showPercent;
  private JCheckBox _alwaysPositive;
  private JCheckBox _useTaxDate;

  private JRadioButton _numeratorMatchInto;
  private JRadioButton _numeratorMatchOutOf;
  private JRadioButton _numeratorMatchBoth;
  private JRadioButton _numeratorEndBalance;
  private JRadioButton _numeratorAvgBalance;
  private JTextField _numeratorLabelField;
  private AccountFilterSelectLabel _numeratorDualAcctSelector;
  private TxnTagFilterView _numeratorTagsView;

  private JRadioButton _denominatorMatchInto;
  private JRadioButton _denominatorMatchOutOf;
  private JRadioButton _denominatorMatchBoth;
  private JRadioButton _denominatorEndBalance;
  private JRadioButton _denominatorAvgBalance;
  private JTextField _denominatorLabelField;
  private AccountFilterSelectLabel _denominatorDualAcctSelector;
  private TxnTagFilterView _denominatorTagsView;

  private JTextArea _notesField;

  private RatioEntry _editingRatio = null;

  RatioEntryEditorView(ResourceProvider resources, MoneydanceGUI mdGui) {
    _resources = resources;
    _mdGui = mdGui;
  }

  void layoutUI() {
    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(UiUtil.VGAP, UiUtil.HGAP,
                                              UiUtil.VGAP, UiUtil.HGAP));
    setOpaque(true);

    setupAccountSelectors();
    setupTagFilters();

    int y = 0;
    // options
    JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtil.DLG_HGAP * 2, 0));
    optionPanel.setOpaque(false);
    _showPercent = new JCheckBox(_resources.getString(L10NRatios.SHOW_PERCENT));
    _showPercent.setOpaque(false);
    optionPanel.add(_showPercent);
    _alwaysPositive = new JCheckBox(_resources.getString(L10NRatios.ALWAYS_POSITIVE));
    _alwaysPositive.setOpaque(false);
    optionPanel.add(_alwaysPositive);
    _useTaxDate = new JCheckBox(_mdGui.getStr(L10NRatios.USE_TAX_DATE));
    _useTaxDate.setOpaque(false);
    optionPanel.add(_useTaxDate);
    add(optionPanel, GridC.getc(1, y));
    add(Box.createVerticalStrut(UiUtil.DLG_VGAP), GridC.getc(0, y++));

    // numerator and denominator - share 1/2 width with a gap in between
    JPanel split50 = new JPanel(new GridLayout(1, 2, UiUtil.DLG_HGAP * 2, 0));
    split50.setOpaque(false);
    JPanel numeratorPanel = createNumeratorPanel();
    JPanel denominatorPanel = createDenominatorPanel();
    split50.add(numeratorPanel);
    split50.add(denominatorPanel);
    add(split50, GridC.getc(0, y++).colspan(2).wxy(1, 1).fillboth());

    // extra space before notes
    add(Box.createVerticalStrut(UiUtil.DLG_VGAP), GridC.getc(0, y++));
    add(new JLabel(UiUtil.getLabelText(_mdGui, L10NRatios.NOTES)), GridC.getc(0, y++).west());
    // force the notes field to be 4 rows tall
    _notesField = new JTextArea(4, 10);
    int height = _notesField.getPreferredSize().height + 4; // add 4 for the border
    add(Box.createVerticalStrut(height), GridC.getc(2, y));
    add(new JScrollPane(_notesField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
        GridC.getc(0, y).colspan(2).rowspan(3).wy(1).fillboth());
    addEventListeners();
  }

  private JPanel createNumeratorPanel() {
    int y = 0;
    JPanel numeratorPanel = new JPanel(new GridBagLayout());
    numeratorPanel.setOpaque(false);
    JLabel label = new JLabel(_resources.getString(L10NRatios.NUMERATOR));
    Font boldFont = label.getFont().deriveFont(Font.BOLD);
    label.setFont(boldFont);
    numeratorPanel.add(label, GridC.getc(1, y++).west());
    numeratorPanel.add(Box.createVerticalStrut(UiUtil.VGAP), GridC.getc(1, y++));
    numeratorPanel.add(new JLabel(RatiosUtil.getLabelText(_resources, L10NRatios.LABEL)), GridC.getc(1, y).label());
    _numeratorLabelField = new JTextField();
    numeratorPanel.add(_numeratorLabelField, GridC.getc(3, y++).field());
    // account filtering
    numeratorPanel.add(new JLabel(UiUtil.getLabelText(_mdGui, L10NRatios.ACCOUNTS)), GridC.getc(1, y).label());
    _numeratorDualAcctSelector.layoutUI();
    numeratorPanel.add(_numeratorDualAcctSelector, GridC.getc(3, y++).field());
    // transaction filtering
    numeratorPanel.add(new JLabel(RatiosUtil.getLabelText(_resources, L10NRatios.TXN_MATCH)), GridC.getc(1, y).label());
    JPanel matchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtil.HGAP * 2, 0));
    matchPanel.setOpaque(false);
    ButtonGroup group = new ButtonGroup();
    _numeratorMatchInto = new JRadioButton(_resources.getString(L10NRatios.TXN_INTO));
    _numeratorMatchInto.setOpaque(false);
    group.add(_numeratorMatchInto);
    matchPanel.add(_numeratorMatchInto);
    _numeratorMatchOutOf = new JRadioButton(_resources.getString(L10NRatios.TXN_OUT_OF));
    _numeratorMatchOutOf.setOpaque(false);
    group.add(_numeratorMatchOutOf);
    matchPanel.add(_numeratorMatchOutOf);
    _numeratorMatchBoth = new JRadioButton(_resources.getString(L10NRatios.TXN_BOTH));
    _numeratorMatchBoth.setOpaque(false);
    group.add(_numeratorMatchBoth);
    matchPanel.add(_numeratorMatchBoth);
    numeratorPanel.add(matchPanel, GridC.getc(3, y++).wx(1).fillx().insets(
        GridC.TOP_FIELD_INSET, 0, 0, 0));
    JPanel matchPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtil.HGAP * 2, 0));
    matchPanel2.setOpaque(false);
    _numeratorEndBalance = new JRadioButton(_mdGui.getStr(L10NRatios.ENDING_BALANCE));
    _numeratorEndBalance.setOpaque(false);
    group.add(_numeratorEndBalance);
    matchPanel2.add(_numeratorEndBalance);
    _numeratorAvgBalance = new JRadioButton(_resources.getString(L10NRatios.AVERAGE_BALANCE));
    _numeratorAvgBalance.setOpaque(false);
    group.add(_numeratorAvgBalance);
    matchPanel2.add(_numeratorAvgBalance);
    numeratorPanel.add(matchPanel2, GridC.getc(3, y++).wx(1).fillx().insets(
      0, 0, GridC.BOTTOM_FIELD_INSET + UiUtil.DLG_VGAP, 0));
    // tag filtering
    numeratorPanel.add(new JLabel(UiUtil.getLabelText(_mdGui, L10NRatios.FILTER_BY_TAG)), GridC.getc(1, y).label().north());
    numeratorPanel.add(_numeratorTagsView, GridC.getc(3, y++).field());
    return numeratorPanel;
  }

  private JPanel createDenominatorPanel() {
    int y = 0;
    JPanel denominatorPanel = new JPanel(new GridBagLayout());
    denominatorPanel.setOpaque(false);
    JLabel label = new JLabel(_resources.getString(L10NRatios.DENOMINATOR));
    Font boldFont = label.getFont().deriveFont(Font.BOLD);
    label.setFont(boldFont);
    denominatorPanel.add(label, GridC.getc(1, y++).west());
    denominatorPanel.add(Box.createVerticalStrut(UiUtil.VGAP), GridC.getc(1, y++));
    denominatorPanel.add(new JLabel(RatiosUtil.getLabelText(_resources, L10NRatios.LABEL)), GridC.getc(1, y).label());
    _denominatorLabelField = new JTextField();
    denominatorPanel.add(_denominatorLabelField, GridC.getc(3, y++).field());
    // account filtering
    denominatorPanel.add(new JLabel(UiUtil.getLabelText(_mdGui, L10NRatios.ACCOUNTS)), GridC.getc(1, y).label());
    _denominatorDualAcctSelector.layoutUI();
    denominatorPanel.add(_denominatorDualAcctSelector, GridC.getc(3, y++).field());
    // transaction filtering
    denominatorPanel.add(new JLabel(RatiosUtil.getLabelText(_resources, L10NRatios.TXN_MATCH)), GridC.getc(1, y).label());
    JPanel matchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtil.HGAP * 2, 0));
    matchPanel.setOpaque(false);
    ButtonGroup group = new ButtonGroup();
    _denominatorMatchInto = new JRadioButton(_resources.getString(L10NRatios.TXN_INTO));
    _denominatorMatchInto.setOpaque(false);
    group.add(_denominatorMatchInto);
    matchPanel.add(_denominatorMatchInto);
    _denominatorMatchOutOf = new JRadioButton(_resources.getString(L10NRatios.TXN_OUT_OF));
    _denominatorMatchOutOf.setOpaque(false);
    group.add(_denominatorMatchOutOf);
    matchPanel.add(_denominatorMatchOutOf);
    _denominatorMatchBoth = new JRadioButton(_resources.getString(L10NRatios.TXN_BOTH));
    _denominatorMatchBoth.setOpaque(false);
    group.add(_denominatorMatchBoth);
    matchPanel.add(_denominatorMatchBoth);
    denominatorPanel.add(matchPanel, GridC.getc(3, y++).wx(1).fillx().insets(
        GridC.TOP_FIELD_INSET, 0, 0, 0));
    JPanel matchPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtil.HGAP * 2, 0));
    matchPanel2.setOpaque(false);
    _denominatorEndBalance = new JRadioButton(_mdGui.getStr(L10NRatios.ENDING_BALANCE));
    _denominatorEndBalance.setOpaque(false);
    group.add(_denominatorEndBalance);
    matchPanel2.add(_denominatorEndBalance);
    _denominatorAvgBalance = new JRadioButton(_resources.getString(L10NRatios.AVERAGE_BALANCE));
    _denominatorAvgBalance.setOpaque(false);
    group.add(_denominatorAvgBalance);
    matchPanel2.add(_denominatorAvgBalance);
    denominatorPanel.add(matchPanel2, GridC.getc(3, y++).wx(1).fillx().insets(
        0, 0, GridC.BOTTOM_FIELD_INSET + UiUtil.DLG_VGAP, 0));
    // tag filtering
    denominatorPanel.add(new JLabel(UiUtil.getLabelText(_mdGui, L10NRatios.FILTER_BY_TAG)), GridC.getc(1, y).label().north());
    denominatorPanel.add(_denominatorTagsView, GridC.getc(3, y++).field());
    return denominatorPanel;
  }

  private void addEventListeners() {
    final ItemListener numeratorListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        enableNumeratorControls();
      }
    };
    _numeratorMatchInto.addItemListener(numeratorListener);
    _numeratorMatchOutOf.addItemListener(numeratorListener);
    _numeratorMatchBoth.addItemListener(numeratorListener);
    _numeratorEndBalance.addItemListener(numeratorListener);
    _numeratorAvgBalance.addItemListener(numeratorListener);

    final ItemListener denominatorListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        enableDenominatorControls();
      }
    };
    _denominatorMatchInto.addItemListener(denominatorListener);
    _denominatorMatchOutOf.addItemListener(denominatorListener);
    _denominatorMatchBoth.addItemListener(denominatorListener);
    _denominatorEndBalance.addItemListener(denominatorListener);
    _denominatorAvgBalance.addItemListener(denominatorListener);
  }

  private void enableNumeratorControls() {
    final boolean enabled = !_numeratorEndBalance.isSelected() && !_numeratorAvgBalance.isSelected();
    _numeratorTagsView.setEnabled(enabled);
    // if both parts are using balances, tax date is useless
    if (!enabled && (_denominatorEndBalance.isSelected() || _denominatorAvgBalance.isSelected())) {
      _useTaxDate.setEnabled(false);
    } else {
      _useTaxDate.setEnabled(true);
    }
  }

  private void enableDenominatorControls() {
    final boolean enabled = !_denominatorEndBalance.isSelected() && !_denominatorAvgBalance.isSelected();
    _denominatorTagsView.setEnabled(enabled);
    // if both parts are using balances, tax date is useless
    if (!enabled && (_numeratorEndBalance.isSelected() || _numeratorAvgBalance.isSelected())) {
      _useTaxDate.setEnabled(false);
    } else {
      _useTaxDate.setEnabled(true);
    }
  }

  void setRatio(RatioEntry ratio) {
    // first save current settings back to the previous entry
    saveControlsToData();
    _editingRatio = ratio;
    // then reload with the new stuff
    loadControlsFromData();
  }

  void saveControlsToData() {
    if (_editingRatio == null) return;
    _editingRatio.setShowPercent(_showPercent.isSelected());
    _editingRatio.setAlwaysPositive(_alwaysPositive.isSelected());
    _editingRatio.setUseTaxDate(_useTaxDate.isSelected());

    if (_numeratorMatchInto.isSelected()) {
      _editingRatio.setNumeratorTxnMatchInto();
    } else if (_numeratorMatchOutOf.isSelected()) {
      _editingRatio.setNumeratorTxnMatchOutOf();
    } else if (_numeratorMatchBoth.isSelected()) {
      _editingRatio.setNumeratorTxnMatchBoth();
    } else if (_numeratorEndBalance.isSelected()) {
      _editingRatio.setNumeratorEndBalanceOnly();
    } else {
      _editingRatio.setNumeratorAverageBalance();
    }
    if (_denominatorMatchInto.isSelected()) {
      _editingRatio.setDenominatorTxnMatchInto();
    } else if (_denominatorMatchOutOf.isSelected()) {
      _editingRatio.setDenominatorTxnMatchOutOf();
    } else if (_denominatorMatchBoth.isSelected()) {
      _editingRatio.setDenominatorTxnMatchBoth();
    } else if (_denominatorEndBalance.isSelected()) {
      _editingRatio.setDenominatorEndBalanceOnly();
    } else {
      _editingRatio.setDenominatorAverageBalance();
    }
    _editingRatio.setNumeratorLabel(_numeratorLabelField.getText());
    _editingRatio.setDenominatorLabel(_denominatorLabelField.getText());
    _editingRatio.setNumeratorRequiredAccounts(_numeratorDualAcctSelector.getRequiredAccountFilter(), _mdGui.getCurrentAccount());
    _editingRatio.setNumeratorDisallowedAccounts(_numeratorDualAcctSelector.getDisallowedAccountFilter(), _mdGui.getCurrentAccount());
    _editingRatio.setDenominatorRequiredAccounts(_denominatorDualAcctSelector.getRequiredAccountFilter(), _mdGui.getCurrentAccount());
    _editingRatio.setDenominatorDisallowedAccounts(_denominatorDualAcctSelector.getDisallowedAccountFilter(), _mdGui.getCurrentAccount());
    _editingRatio.setNumeratorTags(_numeratorTagsView.getSelectedTags());
    _editingRatio.setNumeratorTagLogic(_numeratorTagsView.getTagLogic());
    _editingRatio.setDenominatorTags(_denominatorTagsView.getSelectedTags());
    _editingRatio.setDenominatorTagLogic(_denominatorTagsView.getTagLogic());
    _editingRatio.setNotes(_notesField.getText());
  }

  private void loadControlsFromData() {
    if (_editingRatio == null) {
      // reset to the defaults
      _showPercent.setSelected(true);
      _alwaysPositive.setSelected(true);
      _useTaxDate.setSelected(false);
      // since we use a button group we only need to set the radio button that is on
      _numeratorMatchInto.setSelected(true);
      _denominatorMatchInto.setSelected(true);
      _numeratorLabelField.setText(N12ERatios.EMPTY);
      _denominatorLabelField.setText(N12ERatios.EMPTY);
      _numeratorDualAcctSelector.setRequiredAccountFilter(RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
      _denominatorDualAcctSelector.setRequiredAccountFilter(RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
      _numeratorDualAcctSelector.setDisallowedAccountFilter(RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
      _denominatorDualAcctSelector.setDisallowedAccountFilter(RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
      _numeratorTagsView.reset();
      _denominatorTagsView.reset();
      _notesField.setText(N12ERatios.EMPTY);
      return;
    }
    _showPercent.setSelected(_editingRatio.getShowPercent());
    _alwaysPositive.setSelected(_editingRatio.getAlwaysPositive());
    _useTaxDate.setSelected(_editingRatio.getUseTaxDate());
    // since we use a button group we only need to set the radio button that is on
    if (_editingRatio.getNumeratorTxnMatchInto()) {
      _numeratorMatchInto.setSelected(true);
    } else if (_editingRatio.getNumeratorTxnMatchOutOf()) {
      _numeratorMatchOutOf.setSelected(true);
    } else if (_editingRatio.getNumeratorTxnMatchBoth()) {
      _numeratorMatchBoth.setSelected(true);
    } else if (_editingRatio.getNumeratorEndBalanceOnly()) {
      _numeratorEndBalance.setSelected(true);
    } else {
      _numeratorAvgBalance.setSelected(true);
    }
    if (_editingRatio.getDenominatorTxnMatchInto()) {
      _denominatorMatchInto.setSelected(true);
    } else if (_editingRatio.getDenominatorTxnMatchOutOf()) {
      _denominatorMatchOutOf.setSelected(true);
    } else if (_editingRatio.getDenominatorTxnMatchBoth()) {
      _denominatorMatchBoth.setSelected(true);
    } else if (_editingRatio.getDenominatorEndBalanceOnly()) {
      _denominatorEndBalance.setSelected(true);
    } else {
      _denominatorAvgBalance.setSelected(true);
    }
    _numeratorLabelField.setText(_editingRatio.getNumeratorLabel());
    _denominatorLabelField.setText(_editingRatio.getDenominatorLabel());

    // use the object that supports IAccountSelector to re-use common code for loading an account list
    RatioAccountSelector accountSelector = createAccountSelector(_mdGui.getCurrentAccount());
    _numeratorDualAcctSelector.setRequiredAccountFilter(
        accountSelector.selectFromEncodedString(_editingRatio.getNumeratorEncodedRequiredAccounts()));
    _numeratorDualAcctSelector.setDisallowedAccountFilter(accountSelector.selectFromEncodedString(
        _editingRatio.getNumeratorEncodedDisallowedAccounts()));
    _denominatorDualAcctSelector.setRequiredAccountFilter(accountSelector.selectFromEncodedString(
        _editingRatio.getDenominatorEncodedRequiredAccounts()));
    _denominatorDualAcctSelector.setDisallowedAccountFilter(accountSelector.selectFromEncodedString(
        _editingRatio.getDenominatorEncodedDisallowedAccounts()));

    _numeratorTagsView.setSelectedTags(_mdGui.getCurrentAccount(),
                                       _editingRatio.getNumeratorTags(), _editingRatio.getNumeratorTagLogic());
    _denominatorTagsView.setSelectedTags(_mdGui.getCurrentAccount(),
                                         _editingRatio.getDenominatorTags(), _editingRatio.getDenominatorTagLogic());
    _denominatorTagsView.reset();
    _notesField.setText(_editingRatio.getNotes());
  }

  static RatioAccountSelector createAccountSelector(final RootAccount rootAccount) {
    return new RatioAccountSelector(rootAccount);
  }

  private void setupAccountSelectors() {
    _numeratorDualAcctSelector = new AccountFilterSelectLabel(_mdGui, _resources);
    _numeratorDualAcctSelector.setRequiredAccountFilter(
        RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
    _numeratorDualAcctSelector.setDisallowedAccountFilter(
        RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
    _denominatorDualAcctSelector = new AccountFilterSelectLabel(_mdGui, _resources);
    _denominatorDualAcctSelector.setRequiredAccountFilter(
        RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
    _denominatorDualAcctSelector.setDisallowedAccountFilter(
        RatioAccountSelector.buildAccountFilter(_mdGui.getCurrentAccount()));
  }


  private void setupTagFilters() {
    _numeratorTagsView = new TxnTagFilterView(_mdGui);
    _numeratorTagsView.layoutUI();
    _numeratorTagsView.setOpaque(false);
    _denominatorTagsView = new TxnTagFilterView(_mdGui);
    _denominatorTagsView.layoutUI();
    _denominatorTagsView.setOpaque(false);
  }
}
