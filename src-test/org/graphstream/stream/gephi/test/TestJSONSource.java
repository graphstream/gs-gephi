package org.graphstream.stream.gephi.test;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONReceiver;
import org.graphstream.stream.gephi.JSONSource;
import org.graphstream.stream.thread.ThreadProxyPipe;

public class TestJSONSource {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	// ----- On the receiver side -----
	//
	// - a graph that will display the received events	
	Graph g = new MultiGraph("G",false,true);
	g.display();
	// the receiver that waits for events
	JSONSource source = new JSONSource("localhost", 8080,"workspace0");
	// plug the source to the sink of the graph
	source.addSink(g);
	source.processStream();
    }
}
