package org.graphstream.stream.gephi.test;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.gephi.JSONSender;

/**
 * a example of using the JSONStream sender
 * @author wumalbert
 *
 */
public class ExampleJSONSender {
    
    public static void main(String args[]) {
        
	Graph graph = new SingleGraph("Tutorial 1");

        JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
	graph.addSink(sender);
		
	//graph.display();
		
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        sleep();
        graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");
        sleep();
        
	//graph.clear();
    }
	
    protected static void sleep() {
        try {
            Thread.sleep(1000); 
        } catch (Exception e) {}
    }
}
