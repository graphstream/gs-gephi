package org.graphstream.stream.gephi.tutorial;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONSender;
import org.graphstream.ui.swingViewer.Viewer;

/*
 * A simple example to show loading or generating a graph in GS side and send it to Gephi
 * @author Min WU
 */
public class GraphSender {
    
    public static void main(String args[]) {
	Graph graph = new MultiGraph("Tutorial 1 GraphSender");

	Viewer viewer = graph.display();
	
        JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
        // sender.setDebug(true);
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
