/*
 * onprom-toolkit
 *
 * LogSummaryPanel.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
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

package it.unibz.inf.onprom.ui.component;

import it.unibz.inf.onprom.ui.utility.UIUtility;
import it.unibz.inf.pm.ocel.classification.OcelEventClasses;
import it.unibz.inf.pm.ocel.classification.OcelEventClassifier;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import it.unibz.inf.pm.ocel.info.OcelAttributeInfo;
import it.unibz.inf.pm.ocel.info.OcelLogInfo;
import it.unibz.inf.pm.ocel.info.OcelTimeBounds;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Frame showing log summary
 * <p>
 *
 * @author T. E. Kalayci on 06-Mar-2022
 */
public class OcelLogSummaryPanel extends JInternalFrame {
    private static final Dimension TXT_SIZE = new Dimension(375, 25);
    private static final Dimension CHART_SIZE = new Dimension(800, 300);

    private final OcelLog info;


    public OcelLogSummaryPanel(OcelLog info) {
        super("Log Summary", true, true, true, true);
        this.info = info;
        this.getContentPane().setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(createPanel(info));
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
    }

    private JPanel createPanel(OcelLog log) {
        OcelLogInfo info = new OcelLogInfo() {
            @Override
            public OcelLog getLog() {
                return null;
            }

            @Override
            public int getNumberOfEvents() {
                return 0;
            }

            @Override
            public int getNumberOfObjects() {
                return 0;
            }

            @Override
            public int getNumberOfTraces() {
                return 0;
            }

            @Override
            public Collection<OcelEventClassifier> getEventClassifiers() {
                return null;
            }

            @Override
            public OcelEventClasses getEventClasses(OcelEventClassifier var1) {
                return null;
            }

            @Override
            public OcelEventClasses getEventClasses() {
                return null;
            }

            @Override
            public OcelEventClasses getResourceClasses() {
                return null;
            }

            @Override
            public OcelEventClasses getNameClasses() {
                return null;
            }

            @Override
            public OcelEventClasses getTransitionClasses() {
                return null;
            }

            @Override
            public OcelTimeBounds getLogTimeBoundaries() {
                return null;
            }

            @Override
            public OcelTimeBounds getEventTimeBoundaries(OcelEvent var1) {
                return null;
            }

            @Override
            public OcelAttributeInfo getLogAttributeInfo() {
                return null;
            }

            @Override
            public OcelAttributeInfo getTraceAttributeInfo() {
                return null;
            }

            @Override
            public OcelAttributeInfo getEventAttributeInfo() {
                return null;
            }

            @Override
            public OcelAttributeInfo getObjectAttributeInfo() {
                return null;
            }

            @Override
            public OcelAttributeInfo getMetaAttributeInfo() {
                return null;
            }
        };


        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        panel.add(UIUtility.createLabel("Number of objects: " + log.getObjects().size(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        panel.add(UIUtility.createLabel("Number of events: " + log.getEvents().size(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 0;
        //info.getLogTimeBoundaries().getStartDate()
        panel.add(UIUtility.createLabel("Start: " + log.getObjects().size(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        //info.getLogTimeBoundaries().getEndDate()
        panel.add(UIUtility.createLabel("End: " + log.getObjects().size(), TXT_SIZE), gridBagConstraints);

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        OcelEventClasses eventClasses = info.getEventClasses();
//        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        for (int i = 0; i < eventClasses.size(); i++) {
//            OcelEventClass eventClass = eventClasses.getByIndex(i);
//            dataset.addValue(eventClass.size(), eventClass.getId(), "");
//        }
        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        ChartPanel chartPanel = new ChartPanel(ChartFactory.createBarChart(null, "Event Name", "Count", dataset,
                PlotOrientation.HORIZONTAL, true, true, false));
        chartPanel.setPreferredSize(CHART_SIZE);
        panel.add(chartPanel, gridBagConstraints);

        gridBagConstraints.gridy++;
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        Map<String, OcelEvent> events = log.getEvents();
        Graph graph = new SingleGraph("Events and Objects");

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true);
        graph.setStrict(false);
        graph.display();


        for (OcelEvent evt : events.values()) {
            StringBuilder eventStr = new StringBuilder();
            String id = evt.getId();
            String activity = evt.getActivity();
            eventStr.append(activity + " ⇨");
            List<String> omap = evt.getOmap();
            Node node = graph.addNode(id);
            node.setAttribute("ui.class", "marked");
            if (omap.size() > 0) {
                for (String o : omap) {
                    eventStr.append("  " + o.toString());
                    graph.addEdge(id + "->" + o, id, o);
                }
            }
            listModel.addElement(eventStr.substring(0, eventStr.length() - 1));
        }

//        for (Node node : graph) {
//            node.setAttribute("ui.label", node.getId());
//        }

//        explore(graph.getNode(startNode));

        final JScrollPane jScrollPane = new JScrollPane(list);
        jScrollPane.setPreferredSize(CHART_SIZE);
        panel.add(jScrollPane, gridBagConstraints);
        return panel;
    }

    public void explore(Node source) {
        Iterator<? extends Node> k = source.getBreadthFirstIterator();
        while (k.hasNext()) {
            Node next = k.next();
            next.setAttribute("ui.class", "marked");
            sleep();
        }
    }

    protected void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
    }

    protected String styleSheet = "graph { padding: 50px; fill-color: white; }" +
            "node {" +
            "	fill-color: blue;" +
            "}" +
            "node.marked {" +
            "	fill-color: red;" +
            "}";
}
