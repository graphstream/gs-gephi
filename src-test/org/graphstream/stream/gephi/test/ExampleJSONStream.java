package org.graphstream.stream.gephi.test;

import java.io.IOException;
import java.net.UnknownHostException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONReceiver;
import org.graphstream.stream.gephi.JSONSender;
import org.graphstream.stream.thread.ThreadProxyPipe;

/**
 * A simple example of use of the JSONSink and JSONReceiver to communicate with Gephi.
 * JSONSink sends events to Gephi, and JSONReceiver receiver events from Gephi
 * @author Min WU

 */
public class ExampleJSONStream {
    
    public static void main(String[] args) throws UnknownHostException, 
    			IOException, InterruptedException {
	// ----- On the receiver side -----
	//
	// - a graph that will display the received events
	Graph g = new MultiGraph("G", false, true);
	g.display();
	// - the receiver that waits for events
	JSONReceiver receiver = new JSONReceiver("localhost", 8080, "workspace0");
	receiver.setDebug(true);
	// - received events end up in the "default" pipe
	ThreadProxyPipe pipe = receiver.getStream();
	// - plug the pipe to the sink of the graph
	pipe.addSink(g);
	// ----- The sender side (in another thread) ------
	//
	new Thread() {
	    public void run() {
	    // - the original graph from which events are generated
		Graph g = new MultiGraph("G");
		// - the sender
		JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
		// - plug the graph to the sender so that graph events can be
		// sent automatically
		g.addSink(sender);
		// - generate some events on the client side
		String style = "node{fill-mode:plain;fill-color:#567;size:6px;}";
		g.addAttribute("stylesheet", style);
		g.addAttribute("ui.antialias", true);
		g.addAttribute("layout.stabilization-limit", 0);
		for (int i = 0; i < 50; i++) {
			g.addNode(i + "");
			if (i > 0) {
			    g.addEdge(i + "-" + (i - 1), i + "", (i - 1) + "");
			    g.addEdge(i + "--" + (i / 2), i + "", (i / 2) + "");
			}
		}
	    }
	}.start();

	// ----- Back to the receiver side -----
	//
	// -The receiver pro-actively checks for events on the ThreadProxyPipe
	while (true) {
	    pipe.pump();
	    Thread.sleep(100);
	}
    }
}
