package org.graphstream.stream.gephi.test;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONReceiver;
import org.graphstream.stream.thread.ThreadProxyPipe;

public class ExampleJSONReceiver {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	// ----- On the receiver side -----
	//
	// a graph that will display the received events
	Graph g = new MultiGraph("G",false,true);
	g.display();
	// the receiver that waits for events
	JSONReceiver receiver = new JSONReceiver("localhost", 8080,"workspace0");
	ThreadProxyPipe pipe = receiver.getStream();
	// plug the pipe to the sink of the graph
	pipe.addSink(g);
	// The receiver pro-actively checks for events on the ThreadProxyPipe
	while (true) {
	    pipe.pump();
	}
    }

}
