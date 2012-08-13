package org.graphstream.stream.gephi.tutorial;

import java.io.IOException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.thread.ThreadProxyPipe;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.stream.gephi.JSONReceiver;
import org.graphstream.stream.gephi.JSONSender;

/**
 * A tutorial to show connecting to Gephi, receive the graph,
 * do LinLog layout for the graph in GS side
 * and then send layout information to Gephi 
 * @author Min WU
 */
public class LinLogLayoutReceiver {
    
    public static void main(String args[]) throws IOException, GraphParseException {
	
	(new LinLogLayoutReceiver()).findCommunities();
    }

    //graph object
    private Graph graph;
    //view object
    private Viewer viewer;
    
    //LinLog layout parameter
    private LinLog layout;
    private double a = 0;
    private double r = -1.3;
    private double force = 3;
    
    //a proxy pipe to do interaction in the GS viewer
    private ProxyPipe fromViewer;
    
    //receive 
    private JSONReceiver receiver;
    
    public void findCommunities() throws IOException, GraphParseException {
	
	graph = new MultiGraph("Communities",false,true);
	viewer = graph.display(false);
	fromViewer = viewer.newThreadProxyOnGraphicGraph();
	
	layout = new LinLog(false);
	layout.configure(a, r, true, force);
	layout.addSink(graph);
	graph.addSink(layout);
	fromViewer.addSink(graph);
	
	graph.addAttribute("ui.antialias");
	graph.addAttribute("ui.stylesheet", styleSheet);
	
	// ----- On the receiver side -----
	// build the receiver that waits for events
	receiver = new JSONReceiver("localhost", 8080, "workspace0");
	//receiver.setDebug(true);
	ThreadProxyPipe pipe = receiver.getStream();
	// plug the pipe to the sink of the graph
	//pipe.addSink(graph);
	pipe.addElementSink(graph);
	
	// ----- On the sender side -----
	new Thread() {
	    public void run() {
		// build a sender that sends events(layout information) to Gephi
		JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
		//sender.setDebug(true);
		layout.addSink(sender);
	    }
	}.start();
	
	//The receiver pro-actively checks for events on the ThreadProxyPipe
	pipe.pump();
	while(!graph.hasAttribute("ui.viewClosed")) {
	    pipe.pump();
	    fromViewer.pump();
	    layout.compute();
	}
    }
    
    protected static String styleSheet = 
	    "node { size: 7px; fill-color: rgb(150,150,150); }" + 
            "edge { size: 2px; fill-color: rgb(255,50,50); }";
}