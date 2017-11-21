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
    private static final Logger LOGGER = LoggerFactory.getLogger(LogSummaryPanel.class.getSimpleName());
    private static final Dimension TXT_SIZE = new Dimension(375, 25);
    private final XLogInfo info;
    private static final Dimension CHART_SIZE = new Dimension(800, 300);

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
        gridBagConstraints.gridx = 1;
        panel.add(UIUtility.createLabel("Number of events: " + info.getNumberOfEvents(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 0;
        panel.add(UIUtility.createLabel("Start: " + info.getLogTimeBoundaries().getStartDate(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        panel.add(UIUtility.createLabel("End: " + info.getLogTimeBoundaries().getEndDate(), TXT_SIZE), gridBagConstraints);

        XEventClasses eventClasses = info.getEventClasses();
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < eventClasses.size(); i++) {
            XEventClass eventClass = eventClasses.getByIndex(i);
            dataset.addValue(eventClass.size(), eventClass.getId(), "");
        }
        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        ChartPanel chartPanel = new ChartPanel(ChartFactory.createBarChart(null, "Event Name", "Count", dataset,
                PlotOrientation.HORIZONTAL, true, true, false));
        chartPanel.setPreferredSize(CHART_SIZE);
        panel.add(chartPanel, gridBagConstraints);

        gridBagConstraints.gridy++;
        //panel.add(UIUtility.createLabel("All Traces Available in the Log", TXT_SIZE), gridBagConstraints);
        //gridBagConstraints.gridy++;
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        for (XTrace trace : info.getLog()) {
            StringBuilder events = new StringBuilder();
            if(!trace.getAttributes().isEmpty() && trace.getAttributes().get("concept:name")!=null) {
                events.append(trace.getAttributes().get("concept:name").toString());
            }
            events.append(" ⇨");
            for (XEvent event : trace) {
                if(event.getAttributes()!=null && event.getAttributes().get("concept:name")!=null) {
                    events.append(" ").append(event.getAttributes().get("concept:name").toString()).append(" →");
                }
            }
            listModel.addElement(events.substring(0, events.length()-1));
        }
        final JScrollPane jScrollPane = new JScrollPane(list);
        jScrollPane.setPreferredSize(CHART_SIZE);
        panel.add(jScrollPane, gridBagConstraints);
        return panel;
    }
}
