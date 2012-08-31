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
package org.graphstream.stream.gephi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.graphstream.stream.gephi.JSONEventConstants.Fields;
import org.graphstream.stream.gephi.JSONEventConstants.Types;
import org.graphstream.stream.sync.SourceTime;
import org.graphstream.stream.thread.ThreadProxyPipe;

/**
 * connect GraphStream to Gephi Gephi works as source, sends events to
 * GraphStream
 * 
 * @author wumalbert
 * 
 */
public class JSONReceiver extends Thread {

    /**
     * the host of the Gephi server
     */
    private String host;

    /**
     * the port of the Gephi server
     */
    private int port;

    /**
     * the workspace name of the Gephi server
     */
    private String workspace;

    /**
     * the gephi source ID
     */
    protected String sourceId;

    protected SourceTime sourceTime;
    /**
     * The current pipe commands are being written to.
     */
    protected ThreadProxyPipe currentStream;

    /**
     * URLConnection which is responsible for connecting to Gephi
     */
    private URLConnection urlConnection;

    /**
     * program debug mode
     */
    private boolean debug;

    /**
     * 
     * @param host
     *            , the host of the Gephi server
     * @param port
     *            , the port of the Gephi server
     * @param workspace
     *            , the workspace name of the Gephi server
     */
    public JSONReceiver(String host, int port, String workspace) {
	this.host = host;
	this.port = port;
	this.workspace = workspace;
	this.sourceId = String.format("<Gephi json stream %x>",
		System.nanoTime());
	this.debug = false;

	this.sourceTime = new SourceTime(this.sourceId);
	currentStream = new ThreadProxyPipe();
	init();
	start();
    }

    /**
     * 
     * @param host
     *            , the host of the Gephi server
     * @param port
     *            , the port of the Gephi server
     * @param workspace
     *            , the workspace name of the Gephi server
     * @param debug
     *            , the program mode
     */
    public JSONReceiver(String host, int port, String workspace, boolean debug) {
	this.host = host;
	this.port = port;
	this.workspace = workspace;
	this.sourceId = String.format("<Gephi json stream %x>",
		System.nanoTime());
	this.debug = debug;

	currentStream = new ThreadProxyPipe();
	init();
	start();
    }

    /**
     * set debug mode
     * 
     * @param debug
     */
    public void setDebug(boolean debug) {
	this.debug = debug;
    }

    /**
     * set debug message
     * 
     * @param message
     * @param data
     */
    private void debug(String message, Object... data) {
	// System.err.print( LIGHT_YELLOW );
	// System.err.printf("[//%s:%d | ", host, port);
	// System.err.print( RESET );
	System.err.printf(message, data);
	// System.err.print( LIGHT_YELLOW );
	System.err.printf("]%n");
	// System.err.println( RESET );
    }

    public ThreadProxyPipe getStream() {
	return currentStream;
    }

    /**
     * connect to Gephi
     */
    protected void init() {
	try {
	    // connect to Gephi server
	    // use getGraph operation to get graphs from Gephi with the
	    // following URL
	    // http://localhost:8080/workspace0?operation=getGrap
	    URL url = new URL("http", host, port, "/" + workspace
		    + "?operation=getGraph");
	    urlConnection = url.openConnection();
	    urlConnection.setDoOutput(false);
	    urlConnection.connect();
	} catch (IOException ex) {
	    // can't connect to gephi
	    ex.printStackTrace();
	}
    }

    /**
     * process the stream received from Gephi
     */
    public void run() {
	// read the result from the servers
	try {
	    InputStream inputStream = urlConnection.getInputStream();
	    BufferedReader bf = new BufferedReader(new InputStreamReader(
		    inputStream));
	    String line;
	    while ((line = bf.readLine()) != null) {
		if (debug)
		    debug(line);
		// each line is a event in Gephi
		parse(line);
	    }
	    inputStream.close();

	} catch (IOException e) {
	    e.printStackTrace();
	    return;
	}
    }

    /**
     * Parse the JSON string received from Gephi as events to be processed by
     * GraphStream
     * {<event_type>:{<object_identifier>:{<attribute_name>:<attribute_value
     * >,<attribute_name>:<attribute_value>}}} { "id": "1278944510", //event
     * identifier "t":**, //time stamp
     * "an":{"A":{"label":"Streaming Node A","size":2}
     * "B":{"label":"Streaming Node B","size":1}
     * "C":{"label":"Streaming Node C","size":1} }, "dn":{"filter":"ALL"} }
     * 
     * @param content
     *            , GSON string received from Gephi
     */
    private void parse(String content) {
	content = content.trim();
	if (content.length() == 0)
	    return;

	try {
	    JSONObject jo = new JSONObject(content);

	    String id = null; // event ID
	    if (jo.has(Fields.ID.value())) {
		id = jo.getString(Fields.ID.value());
	    }

	    Double t = null;
	    if (jo.has(Fields.T.value())) {
		String tstr = jo.getString(Fields.T.value());
		t = Double.valueOf(tstr);
	    }

	    Iterator<String> keys = jo.keys();

	    while (keys.hasNext()) {
		String key = keys.next();
		if (Fields.ID.value().equals(key))
		    continue;
		if (Fields.T.value().equals(key))
		    continue;

		Object gObjs = jo.get(key);
		if (gObjs instanceof JSONObject) {
		    parse(key, (JSONObject) gObjs, id, t);
		} else {
		    throw new IllegalArgumentException("Invalid attribute: "
			    + key);
		    // logger.log(Level.WARNING,
		    // "JSON attribute ignored: \"{0}\"", new String[]{key});
		}
	    }
	} catch (JSONException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Parse the detailed JSON string received from Gephi
     * 
     * @param type
     *            , event type, one of AN,CN,DN,AE,CE,DE,CG
     * @param gObjs
     *            , JSON message
     * @param eventId
     *            , event identifier
     * @param t
     *            , time stamp of the event
     * @throws JSONException
     */
    private void parse(String type, JSONObject gObjs, String eventId, Double t)
	    throws JSONException {

	Types eventType = Types.fromString(type);

	if (gObjs.has("filter")) {
	    if (gObjs.getString("filter").equals("ALL")) {
		currentStream.graphCleared(sourceId, sourceTime.newEvent());
	    }
	    /*
	     * Map<String, Object> attributes = null; if
	     * (gObjs.has("attributes")) { JSONObject attrObj =
	     * gObjs.getJSONObject("attributes"); attributes =
	     * readAttributes(attrObj); }
	     * 
	     * handler.handleGraphEvent( new FilterEvent(this,
	     * eventType.getEventType(), eventType.getElementType(),
	     * getFilter(eventType.getElementType(), gObjs), attributes));
	     */
	    return;
	}

	if (eventType.equals(Types.CG)) {
	    Iterator<String> itrAttrs = gObjs.keys();
	    while (itrAttrs.hasNext()) {
		String key = itrAttrs.next();
		Object value = gObjs.get(key);
		currentStream.graphAttributeChanged(sourceId,
			sourceTime.newEvent(), key, null, value);
	    }
	    return;
	}

	Iterator<String> it = gObjs.keys();
	while (it.hasNext()) {
	    // GraphEvent event = null;
	    String elementId = it.next();
	    JSONObject gObj = (JSONObject) gObjs.get(elementId);
	    Iterator<String> itrAttrs = gObj.keys();

	    switch (eventType) {
	    case AN:
		currentStream.nodeAdded(sourceId, sourceTime.newEvent(),
			elementId);
		while (itrAttrs.hasNext()) {
		    String key = itrAttrs.next();
		    Object value = gObj.get(key);
		    currentStream.nodeAttributeAdded(sourceId,
			    sourceTime.newEvent(), elementId, key, value);
		}
		break;
	    case CN:
		while (itrAttrs.hasNext()) {
		    String key = itrAttrs.next();
		    Object value = gObj.get(key);
		    currentStream.nodeAttributeChanged(sourceId,
			    sourceTime.newEvent(), elementId, key, null, value);
		}
		break;
	    case DN:
		currentStream.nodeRemoved(sourceId, sourceTime.newEvent(),
			elementId);
		break;
	    case AE:
		String fromNodeId = gObj.getString(Fields.SOURCE.value());
		String toNodeId = gObj.getString(Fields.TARGET.value());

		boolean directed = true;
		if (gObj.has(Fields.DIRECTED.value())) {
		    directed = Boolean.valueOf(gObj.getString(Fields.DIRECTED
			    .value()));
		}
		currentStream.edgeAdded(sourceId, sourceTime.newEvent(),
			elementId, fromNodeId, toNodeId, directed);
		while (itrAttrs.hasNext()) {
		    String key = itrAttrs.next();

		    if (key.equals(Fields.SOURCE.value()))
			continue;
		    if (key.equals(Fields.TARGET.value()))
			continue;
		    if (key.equals(Fields.DIRECTED.value()))
			continue;

		    Object value = gObj.get(key);
		    currentStream.edgeAttributeAdded(sourceId,
			    sourceTime.newEvent(), elementId, key, value);
		}
		break;
	    case CE:
		while (itrAttrs.hasNext()) {
		    String key = itrAttrs.next();
		    Object value = gObj.get(key);
		    currentStream.edgeAttributeChanged(sourceId,
			    sourceTime.newEvent(), elementId, key, null, value);
		}
		break;
	    case DE:
		currentStream.edgeRemoved(sourceId, sourceTime.newEvent(),
			elementId);
		break;

	    }
	}
    }
}
