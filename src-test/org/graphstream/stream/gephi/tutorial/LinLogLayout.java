package org.graphstream.stream.gephi.tutorial;

import java.io.IOException;
import java.net.UnknownHostException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.stream.gephi.JSONReceiver;
import org.graphstream.stream.gephi.JSONSender;
import org.graphstream.stream.thread.ThreadProxyPipe;
import org.graphstream.ui.layout.springbox.implementations.LinLog;

/**
 * A simple example of generating a graph in GS, sending the graph to GS
 * and then receiving the graph in GS side and do LinLog layout in GS side
 * @author Min WU
 */
public class LinLogLayout {
    
    private LinLog layout;
    private double a = 0;
    private double r = -1.3;
    private double force = 3;
	
    public static void main(String[] args) throws UnknownHostException,
	        IOException, InterruptedException, GraphParseException {
	new LinLogLayout().findCommunities();
    }
	
    public void findCommunities() throws IOException, InterruptedException, GraphParseException {
	// ----- On the receiver side -----
	//
	// - a graph that will display the received events
	Graph graph = new MultiGraph("G", false, true);
	graph.display(false);
	// - the receiver that waits for events
	JSONReceiver receiver = new JSONReceiver("localhost", 8080, "workspace0");
	receiver.setDebug(true);
	// - received events end up in the "default" pipe
	ThreadProxyPipe pipe = receiver.getStream();
	// - plug the pipe to the sink of the graph
	pipe.addSink(graph);
	
	layout = new LinLog(false);
	layout.configure(a,r,true,force);
	layout.addSink(graph);
	graph.addSink(layout);
	
	// ----- The sender side (in another thread) ------
	//
	new Thread() {
	    public void run() {
	    // - the original graph from which events are generated
		Graph graph = new MultiGraph("G");
		// - the sender
		JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
		// - plug the graph to the sender so that graph events can be
		// sent automatically
		graph.addSink(sender);
		// - generate some events on the client side
		String style = "node{fill-mode:plain;fill-color:#567;size:6px;}";
		graph.addAttribute("stylesheet", style);
		graph.addAttribute("ui.antialias", true);
		graph.addAttribute("layout.stabilization-limit", 0);
		for (int i = 0; i < 50; i++) {
			graph.addNode(i + "");
			if (i > 0) {
			    graph.addEdge(i + "-" + (i - 1), i + "", (i - 1) + "");
			    graph.addEdge(i + "--" + (i / 2), i + "", (i / 2) + "");
			}
		}
	    }
	}.start();

	// ----- Back to the receiver side -----
	//
	// -The receiver pro-actively checks for events on the ThreadProxyPipe
	while (true) {
	    pipe.pump();
	    layout.compute();
	    Thread.sleep(100);
	}
    }
    
}
