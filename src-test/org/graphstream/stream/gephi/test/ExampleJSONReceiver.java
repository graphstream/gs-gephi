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

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONReceiver;
import org.graphstream.stream.thread.ThreadProxyPipe;

/**
 * a simple example of using the JSONStream receiver
 * 
 * @author wumalbert
 * 
 */
public class ExampleJSONReceiver {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	// ----- On the receiver side -----
	//
	// a graph that will display the received events
	Graph g = new MultiGraph("G", false, true);
	g.display();
	// the receiver that waits for events
	JSONReceiver receiver = new JSONReceiver("localhost", 8080,
		"workspace0");
	receiver.setDebug(true);
	ThreadProxyPipe pipe = receiver.getStream();
	// plug the pipe to the sink of the graph
	pipe.addSink(g);
	// The receiver pro-actively checks for events on the ThreadProxyPipe
	while (true) {
	    pipe.pump();
	}
    }

}
