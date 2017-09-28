/*
 * onprom-toolkit
 *
 * LogSummaryPanel.java
 *
 * Copyright (C) 2016-2017 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 *  KAOS: Knowledge-Aware Operational Support project
 *  (https://kaos.inf.unibz.it).
 *
 *  Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Frame showing log summary
 * <p>
 *
 * @author T. E. Kalayci on 04-Jul-2017
 */
public class LogSummaryPanel extends JInternalFrame {
    private static final Dimension TXT_SIZE = new Dimension(400, 25);
    private static final Logger logger = LoggerFactory.getLogger(LogSummaryPanel.class.getSimpleName());
    private final XLogInfo info;

    public LogSummaryPanel(XLogInfo _info) {
        super("Log Summary", true, true, true, true);
        info = _info;
        initUI();
    }

    private void initUI() {
        this.getContentPane().setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(createPanel());
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.setSize(new Dimension(1024, 768));
        this.setVisible(true);
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        panel.add(UIUtility.createLabel("Number of traces: " + info.getNumberOfTraces(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridy++;
        panel.add(UIUtility.createLabel("Number of events: " + info.getNumberOfEvents(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridy++;
        panel.add(UIUtility.createLabel("Start date of the log: " + info.getLogTimeBoundaries().getStartDate(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridy++;
        panel.add(UIUtility.createLabel("End date of the log: " + info.getLogTimeBoundaries().getEndDate(), TXT_SIZE), gridBagConstraints);
        XEventClasses eventClasses = info.getEventClasses();
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < eventClasses.size(); i++) {
            XEventClass eventClass = eventClasses.getByIndex(i);
            dataset.addValue(eventClass.size(), eventClass.toString(), "");
        }
        gridBagConstraints.gridy++;
        JFreeChart barChart = ChartFactory.createBarChart(
                "Events",
                "Event Name", "Count",
                dataset, PlotOrientation.HORIZONTAL,
                true, true, false);
        panel.add(new ChartPanel(barChart), gridBagConstraints);
        //JTable showing all cases
        gridBagConstraints.gridy++;
        panel.add(UIUtility.createLabel("All Traces Available in the Log", TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridy++;
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        for (XTrace trace : info.getLog()) {
            StringBuilder events = new StringBuilder();
            for (XEvent event : trace) {
                events.append(event.getAttributes().get("concept:name").toString()).append("▷");
            }
            try {
                listModel.addElement(trace.getAttributes().get("concept:name").toString() + " (" + events.substring(0, events.length() - 1) + ")");
            } catch (NullPointerException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        panel.add(new JScrollPane(list), gridBagConstraints);
        return panel;
    }
}
