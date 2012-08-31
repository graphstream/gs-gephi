/*
 * Copyright (C) 2012 wumalbert
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
 * associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.graphstream.stream.gephi.test;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.gephi.JSONSender;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.swingViewer.Viewer;

/**
 * a example of using the JSONStream sender
 * 
 * @author wumalbert
 * 
 */
public class ExampleJSONSender {

    public static void main(String args[]) {

	Graph graph = new SingleGraph("Tutorial 1");

	Viewer viewer = graph.display();

	JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
	sender.setDebug(true);
	graph.addSink(sender);

	graph.addNode("A");
	graph.addNode("B");
	graph.addNode("C");
	sleep();
	graph.addEdge("AB", "A", "B");
	graph.addEdge("BC", "B", "C");
	graph.addEdge("CA", "C", "A");
	sleep();

	// graph.clear();
    }

    protected static void sleep() {
	try {
	    Thread.sleep(1000);
	} catch (Exception e) {
	}
    }
}
