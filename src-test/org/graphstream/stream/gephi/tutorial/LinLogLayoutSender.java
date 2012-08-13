package org.graphstream.stream.gephi.tutorial;

import java.io.IOException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.gephi.JSONSender;
import org.graphstream.stream.thread.ThreadProxyPipe;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

/**
 * a simple example of loading a graph in GS side, do LinLog layout and send the layout to Gephi
 * @author Min WU
 *
 */
public class LinLogLayoutSender {
    
    public static void main(String args[]) throws IOException, GraphParseException {
	
	(new LinLogLayoutSender()).findCommunities("data/karate.gml");
    }
    
    private Graph graph;
    private Viewer viewer;
    
    //LinLog layout configure parameters
    private LinLog layout;
    private double a = 0;
    private double r = -1.3;
    private double force = 3;
    
    //a proxy pipe to do interaction in the GS viewer
    private ProxyPipe fromViewer;
    
    private double cutThreshold = 1;
    
    private ConnectedComponents cc;
    private SpriteManager sm;
    private Sprite ccCount;
    private Sprite modValue;
    private Modularity modularity;
    
    private JSONSender jsonSender;
    
    public void findCommunities(String fileName) throws IOException, GraphParseException {
	
	//graph = new MultiGraph("G",false,true);
	graph = new SingleGraph("communities");
	viewer = graph.display(false);
	fromViewer = viewer.newThreadProxyOnGraphicGraph();
	layout = new LinLog(false);
	cc = new ConnectedComponents(graph);
	sm = new SpriteManager(graph);
	ccCount = sm.addSprite("CC");
	modularity = new Modularity("module");
	modValue = sm.addSprite("M");
	
	modularity.init(graph);
	layout.configure(a, r, true, force);
	cc.setCutAttribute("cut");
	ccCount.setPosition(Units.PX, 20, 20, 0);
	cc.setCountAttribute("module");
	modValue.setPosition(Units.PX, 20, 40, 0);
	layout.addSink(graph);
	graph.addSink(layout);
	fromViewer.addSink(graph);
		
	graph.addAttribute("ui.antialias");
	graph.addAttribute("ui.stylesheet", styleSheet);
	
	jsonSender = new JSONSender("localhost", 8080, "workspace0");
	layout.addSink(jsonSender);
		
	graph.read(fileName);
	
	while(!graph.hasAttribute("ui.viewClosed")) {
	    fromViewer.pump();
	    layout.compute();
	    showCommunities();
	    ccCount.setAttribute("ui.label", 
		    String.format("Modules %d", cc.getConnectedComponentsCount()));
	    modValue.setAttribute("ui.label", 
		    String.format("Modularity %f", modularity.getMeasure()));
	}
    }
    
    public void showCommunities() {
	int nEdges = graph.getEdgeCount();
	double averageDist = 0;
	double edgesDist[] = new double[nEdges];
	
	for (int i = 0; i < nEdges; ++i ) {
	    Edge edge = graph.getEdge(i);
	    edgesDist[i] = GraphPosLengthUtils.edgeLength(edge);
	    averageDist += edgesDist[i];
	}
	averageDist /= nEdges; 
		
	for (int i = 0; i < nEdges; ++i) {
	    Edge edge = graph.getEdge(i);
	    if (edgesDist[i] > averageDist * cutThreshold) {
		edge.addAttribute("ui.class", "cut");
		edge.addAttribute("cut");
	    } else {
		edge.removeAttribute("ui.class");
		edge.removeAttribute("cut");
	    }
	}
    }
	
    protected static String styleSheet = 
	    "node { size: 7px; fill-color: rgb(150,150,150); }" + 
            "edge { size: 2px; fill-color: rgb(255,50,50); }" + 
	    "edge.cut { fill-color: rgba(200,200,200,128); }" + 
            "sprite#CC { size: 0px; text-color: rgb(150,100,100); text-size: 20; }" +
	    "sprite#M { size: 0px; text-color: rgb(100,150,100); text-size: 20; }";
}