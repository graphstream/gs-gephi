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
package org.graphstream.stream.gephi.tutorial;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONSender;

/*
 * A simple example to show loading or generating a graph in GS side and send it to Gephi
 * @author Min WU
 */
public class GraphSender {

    public static void main(String args[]) {
	Graph graph = new MultiGraph("Tutorial 1 GraphSender");

	graph.display();

	JSONSender sender = new JSONSender("localhost", 8080, "workspace0");

	// plug the graph to the sender so that graph events can be
	// sent automatically
	graph.addSink(sender);

	// generate the graph on the client side
	String style = "node{fill-mode:plain;fill-color:#567;size:6px;}";
	graph.addAttribute("stylesheet", style);
	graph.addAttribute("ui.antialias", true);
	graph.addAttribute("layout.stabilization-limit", 0);
	for (int i = 0; i < 500; i++) {
	    graph.addNode(i + "");
	    if (i > 0) {
		graph.addEdge(i + "-" + (i - 1), i + "", (i - 1) + "");
		graph.addEdge(i + "--" + (i / 2), i + "", (i / 2) + "");
	    }
	}
    }
}
