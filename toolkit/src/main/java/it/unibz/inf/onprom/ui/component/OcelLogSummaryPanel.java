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
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

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
    private String totalEvent = "";

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

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = UIUtility.getGridBagConstraints();

        String prefix = "http://onprom.inf.unibz.it/";
        Map<String, OcelEvent> events = log.getEvents();
        List<String> timestamps = log.getTimestamps();
        Collections.sort(timestamps); // Sort by timestamps in ascending order


        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        panel.add(UIUtility.createLabel("Number of objects: " + log.getObjects().size(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        panel.add(UIUtility.createLabel("Number of events: " + log.getEvents().size(), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 0;
        panel.add(UIUtility.createLabel("Start: " + timestamps.get(0), TXT_SIZE), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        panel.add(UIUtility.createLabel("End: " + timestamps.get(timestamps.size() - 1), TXT_SIZE), gridBagConstraints);

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

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

        //create events and objects graph
        Graph graph = new SingleGraph("Events and Objects");
        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true);
        graph.setStrict(false);
        graph.display();

        for (OcelEvent evt : events.values()) {
            StringBuilder eventStr = new StringBuilder();
            String id = evt.getId();
            id = id.substring(id.indexOf(prefix) + prefix.length());
            String activity = evt.getActivity();
            eventStr.append(activity + " ⇨");
            totalEvent += activity + " ";
            List<String> omap = evt.getOmap();
            Node node = graph.addNode(id);
            node.setAttribute("ui.class", "marked");
            node.addAttribute("layout.weight", 25.0f);
            if (omap.size() > 0) {
                for (String o : omap) {
                    String short_o = o.substring(o.indexOf(prefix) + prefix.length());
                    eventStr.append(short_o + ", ");
                    Edge edge = graph.addEdge(id + "->" + short_o, id, short_o);
                    edge.setAttribute("layout.weight", 25.0f);
                }
            }
            listModel.addElement(eventStr.substring(0, eventStr.length() - 2));
        }

        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }


        //Display statistics of events with a bar graph
        Map<String, Integer> numOfEvents = getEventStatisticsInfo(totalEvent);
        for (Map.Entry<String, Integer> entry : getEventStatisticsInfo(totalEvent).entrySet()) {
            dataset.addValue(entry.getValue(), entry.getKey(), "");
        }

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

    //Count the number of various events
    private Map<String, Integer> getEventStatisticsInfo(String str) {
        Map<String, Integer> eventStatistics = new HashMap<>();
        String[] s = str.split(" ");
        for (int i = 0; i < s.length; i++) {
            String key = s[i];
            if (!"".equals(key)) {
                Integer num = eventStatistics.get(key);
                if (num == null || num == 0) {
                    eventStatistics.put(key, 1);
                } else if (num > 0) {
                    eventStatistics.put(key, num + 1);
                }
            }
        }
        return eventStatistics;
    }

    protected void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
    }

    protected String styleSheet = "graph { padding: 50px; fill-color: white; }" +
            "node {" +
            "	fill-color: #0066FF;" +
            "   size: 15px;" +
            "}" +
            "node.marked {" +
            "	fill-color: #FF3333;" +
            "   size: 20px;" +
            "}";
}
