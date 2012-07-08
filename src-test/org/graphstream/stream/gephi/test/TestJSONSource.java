package org.graphstream.stream.gephi.test;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONSource;

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
	JSONSource source = new JSONSource("localhost", 8080,"workspace0");
	source.addSink(g);
	g.display();
	source.processStream();
    }
}
