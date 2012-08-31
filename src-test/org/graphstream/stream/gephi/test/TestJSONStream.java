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
package org.graphstream.stream.gephi.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.Sink;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.stream.gephi.JSONReceiver;
import org.graphstream.stream.gephi.JSONSender;
import org.graphstream.stream.thread.ThreadProxyPipe;
import org.junit.Test;

public class TestJSONStream {

    /**
     * Test attribute changes, including graph attribute, edge attribute and
     * note attribute
     */
    // @Test
    public void testJSONStreamAttributesChanges() {

	JSONReceiver receiver = new JSONReceiver("localhost", 8080,
		"workspace0");

	receiver.setDebug(true);

	ThreadProxyPipe pipe = receiver.getStream();

	Graph g = new MultiGraph("workspace0", false, true);

	pipe.addSink(g);

	g.addSink(new SinkAdapter() {

	    public void graphAttributeAdded(String sourceId, long timeId,
		    String attribute, Object value) {
		System.out.println("Graph Attribtue Added");
	    }
	});

	new Thread() {

	    public void run() {

		Graph g = new MultiGraph("workspace0", false, true);
		JSONSender sender = new JSONSender("localhost", 8080,
			"workspace0");

		sender.setDebug(true);

		g.addSink(sender);

		g.addAttribute("attribute", "foo");
		g.changeAttribute("attribute", false);

		Edge e = g.addEdge("AB", "A", "B");
		e.addAttribute("attribute", "foo");
		e.changeAttribute("attribute", false);
		Node n = e.getNode0();
		n.addAttribute("attribute", "foo");
		n.changeAttribute("attribute", false);

	    }
	}.start();

	try {
	    Thread.sleep(100);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	// The receiver pro-actively checks for events on the ThreadProxyPipe
	pipe.pump();

	// assertEquals(false, g.getAttribute("attribute"));
	// assertEquals(false, g.getEdge("AB").getAttribute("attribute"));
	// assertEquals(false,
	// g.getEdge("AB").getNode0().getAttribute("attribute"));
    }

    /**
     * Test multiple senders running on separated threads.
     */
    // @Test
    public void testJSONStreamMultiThreadSenders() {

	Graph g = new MultiGraph("workspace0");

	JSONReceiver receiver = new JSONReceiver("localhost", 8080,
		"workspace0");

	// receiver.setDebug(true);

	ThreadProxyPipe pipe = receiver.getStream();

	pipe.addSink(g);

	launchClient("localhost", 8080, "workspace0", "0");
	launchClient("localhost", 8080, "workspace0", "1");

	for (int i = 0; i < 10; i++) {
	    pipe.pump();

	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

	pipe.pump();

	// assertEquals("workspace0", g.getAttribute("id"));
	assertEquals(180, g.getNodeCount());
    }

    private void launchClient(final String host, final int port,
	    final String workspace, final String prefix) {

	new Thread() {

	    @Override
	    public void run() {

		Graph g = new MultiGraph(workspace + prefix);

		JSONSender sender = new JSONSender(host, port, workspace);

		g.addSink(sender);

		g.addAttribute("id", workspace);

		for (int i = 0; i < 30; i++) {
		    g.addNode(prefix + i + "_1");
		    g.addNode(prefix + i + "_0");
		    g.addNode(prefix + i + "_2");
		    try {
			Thread.sleep(1);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }
	}.start();
    }

    /**
     * Hopefully tests all possible graph events through the JSONStream
     * framework.
     */
    @Test
    public void testJSONStreamEvents() {

	Graph g = new DefaultGraph("workspace0", false, true);

	JSONReceiver receiver = new JSONReceiver("localhost", 8080,
		"workspace0");

	ThreadProxyPipe pipe = receiver.getStream();

	pipe.addSink(g);

	g.addSink(new SinkAdapter() {
	    /*
	     * public void graphAttributeAdded(String sourceId, long timeId,
	     * String attribute, Object value) { assertEquals(0, value);
	     * assertEquals("graphAttribute", attribute); }
	     * 
	     * public void graphAttributeChanged(String sourceId, long timeId,
	     * String attribute, Object oldValue, Object newValue) {
	     * assertTrue((Integer) newValue == 0 || (Integer) newValue == 1);
	     * assertEquals("graphAttribute", attribute); }
	     * 
	     * public void graphAttributeRemoved(String sourceId, long timeId,
	     * String attribute) { assertEquals("graphAttribute", attribute); }
	     * 
	     * public void nodeAttributeAdded(String sourceId, long timeId,
	     * String nodeId, String attribute, Object value) { assertEquals(0,
	     * value); assertEquals("nodeAttribute", attribute); }
	     * 
	     * public void nodeAttributeChanged(String sourceId, long timeId,
	     * String nodeId, String attribute, Object oldValue, Object
	     * newValue) { assertTrue((Integer) newValue == 0 || (Integer)
	     * newValue == 1); assertEquals("nodeAttribute", attribute); }
	     * 
	     * public void nodeAttributeRemoved(String sourceId, long timeId,
	     * String nodeId, String attribute) { assertEquals("nodeAttribute",
	     * attribute); }
	     * 
	     * public void edgeAttributeAdded(String sourceId, long timeId,
	     * String edgeId, String attribute, Object value) { assertEquals(0,
	     * value); assertEquals("edgeAttribute", attribute); }
	     * 
	     * public void edgeAttributeChanged(String sourceId, long timeId,
	     * String edgeId, String attribute, Object oldValue, Object
	     * newValue) { assertTrue((Integer) newValue == 0 || (Integer)
	     * newValue == 1); assertEquals("edgeAttribute", attribute); }
	     * 
	     * public void edgeAttributeRemoved(String sourceId, long timeId,
	     * String edgeId, String attribute) { assertEquals("edgeAttribute",
	     * attribute); }
	     */

	    public void nodeAdded(String sourceId, long timeId, String nodeId) {
		assertTrue("node0".equals(nodeId) || "node1".equals(nodeId));
	    }

	    public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		assertTrue("node0".equals(nodeId) || "node1".equals(nodeId));
	    }

	    public void edgeAdded(String sourceId, long timeId, String edgeId,
		    String fromNodeId, String toNodeId, boolean directed) {
		assertEquals("edge", edgeId);
		assertEquals("node0", fromNodeId);
		assertEquals("node1", toNodeId);
		assertEquals(true, directed);
	    }

	    public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		assertEquals("edge", edgeId);
	    }

	    public void graphCleared(String sourceId, long timeId) {

	    }

	    public void stepBegins(String sourceId, long timeId, double step) {
		assertEquals(1.1, step);
	    }
	});

	new Thread() {

	    @Override
	    public void run() {
		Graph g = new MultiGraph("workspace0", false, true);

		JSONSender sender = new JSONSender("localhost", 8080,
			"workspace0");

		g.addSink(sender);

		Node node0 = g.addNode("node0");
		Edge edge = g.addEdge("edge", "node0", "node1", true);
		/*
		 * node0.addAttribute("nodeAttribute", 0);
		 * node0.changeAttribute("nodeAttribute", 1);
		 * node0.removeAttribute("nodeAttribute");
		 * edge.addAttribute("edgeAttribute", 0);
		 * edge.changeAttribute("edgeAttribute", 1);
		 * edge.removeAttribute("edgeAttribute");
		 * g.addAttribute("graphAttribute", 0);
		 * g.changeAttribute("graphAttribute", 1);
		 * g.removeAttribute("graphAttribute");
		 */
		g.stepBegins(1.1);
		g.removeEdge("edge");
		g.removeNode("node0");
		g.clear();
	    }
	}.start();

	try {
	    Thread.sleep(100);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	pipe.pump();
    }

    public static void main(String[] args) {
	new TestJSONStream().testJSONStreamAttributesChanges();
	// new TestJSONStream().testJSONStreamMultiThreadSenders();
	// new TestJSONStream().testJSONStreamEvents();
    }
}
