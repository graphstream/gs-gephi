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

import java.io.*;
import java.net.*;

import org.graphstream.stream.Sink;
//import org.apache.commons.codec.binary.Base64;
import org.graphstream.stream.gephi.JSONEventConstants.Fields;
import org.graphstream.stream.gephi.JSONEventConstants.Types;
import org.json.*;

/**
 * This class is responsible to send GraphStream event in JSON format to Gephi
 * server, so as to communicate from GraphStream to Gephi.
 * 
 * @author wumalbert
 * 
 */
public class JSONSender implements Sink {
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
     * End of Line
     */
    // private static String EOL = "\r\n";
    private static String EOL = "\r";

    /**
     * program debug mode
     */
    private boolean debug;

    /**
     * A sink which can send event to an Gephi server in JSON format
     * 
     * @param host
     *            , the host of the Gephi server
     * @param port
     *            , the port of the Gephi server
     * @param workspace
     *            , the workspace name of the Gephi server
     */
    public JSONSender(String host, int port, String workspace) {
	this.host = host;
	this.port = port;
	this.workspace = workspace;
	this.debug = false;
    }

    /**
     * A sink which can send event to an Gephi server in JSON format
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
    public JSONSender(String host, int port, String workspace, boolean debug) {
	this.host = host;
	this.port = port;
	this.workspace = workspace;
	this.debug = debug;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#edgeAttributeAdded(java.lang.String,
     * long, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
	    String attribute, Object value) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(
		    Types.CE.value(),
		    new JSONObject().put(edgeId,
			    new JSONObject().put(attribute, value)));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#edgeAttributeChanged(java.lang.String
     * , long, java.lang.String, java.lang.String, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void edgeAttributeChanged(String sourceId, long timeId,
	    String edgeId, String attribute, Object oldValue, Object newValue) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(
		    Types.CE.value(),
		    new JSONObject().put(edgeId,
			    new JSONObject().put(attribute, newValue)));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#edgeAttributeRemoved(java.lang.String
     * , long, java.lang.String, java.lang.String)
     */
    @Override
    public void edgeAttributeRemoved(String sourceId, long timeId,
	    String edgeId, String attribute) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(
		    Types.CE.value(),
		    new JSONObject().put(edgeId,
			    new JSONObject().put(attribute, JSONObject.NULL)));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#graphAttributeAdded(java.lang.String
     * , long, java.lang.String, java.lang.Object)
     */
    @Override
    public void graphAttributeAdded(String sourceId, long timeId,
	    String attribute, Object value) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(Types.CG.value(),
		    new JSONObject().put(attribute, value));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#graphAttributeChanged(java.lang.
     * String, long, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void graphAttributeChanged(String sourceId, long timeId,
	    String attribute, Object oldValue, Object newValue) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(Types.CG.value(),
		    new JSONObject().put(attribute, newValue));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#graphAttributeRemoved(java.lang.
     * String, long, java.lang.String)
     */
    @Override
    public void graphAttributeRemoved(String sourceId, long timeId,
	    String attribute) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(Types.CG.value(),
		    new JSONObject().put(attribute, JSONObject.NULL));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#nodeAttributeAdded(java.lang.String,
     * long, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
	    String attribute, Object value) {

	// make an JSON object
	JSONObject jsonObj = null;
	try {
	    if (attribute.equalsIgnoreCase("xyz")) {
		jsonObj = new JSONObject().put(
			Types.CN.value(),
			new JSONObject().put(nodeId, new JSONObject()
				// .put("x", ((Double)((Object[])value)[0])*200)
				// .put("y", ((Double)((Object[])value)[1])*200)
				// .put("z", ((Double)((Object[])value)[2])*200)
				.put("x", ((Double) ((Object[]) value)[0]))
				.put("y", ((Double) ((Object[]) value)[1]))
				.put("z", ((Double) ((Object[]) value)[2]))));
	    } else {
		jsonObj = new JSONObject().put(
			Types.CN.value(),
			new JSONObject().put(nodeId,
				new JSONObject().put(attribute, value)));
	    }
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#nodeAttributeChanged(java.lang.String
     * , long, java.lang.String, java.lang.String, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void nodeAttributeChanged(String sourceId, long timeId,
	    String nodeId, String attribute, Object oldValue, Object newValue) {

	// make an JSON object
	JSONObject jsonObj = null;
	try {
	    if (attribute.equalsIgnoreCase("xyz")) {
		jsonObj = new JSONObject().put(
			Types.CN.value(),
			new JSONObject().put(nodeId, new JSONObject()
				// .put("x",
				// ((Double)((Object[])newValue)[0])*200)
				// .put("y",
				// ((Double)((Object[])newValue)[1])*200)
				// .put("z",
				// ((Double)((Object[])newValue)[2])*200)
				.put("x", ((Double) ((Object[]) newValue)[0]))
				.put("y", ((Double) ((Object[]) newValue)[1]))
				.put("z", ((Double) ((Object[]) newValue)[2]))

			));
	    } else {
		jsonObj = new JSONObject().put(
			Types.CN.value(),
			new JSONObject().put(nodeId,
				new JSONObject().put(attribute, newValue)));
	    }
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.graphstream.stream.AttributeSink#nodeAttributeRemoved(java.lang.String
     * , long, java.lang.String, java.lang.String)
     */
    @Override
    public void nodeAttributeRemoved(String sourceId, long timeId,
	    String nodeId, String attribute) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(
		    Types.CN.value(),
		    new JSONObject().put(nodeId,
			    new JSONObject().put(attribute, JSONObject.NULL)));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.graphstream.stream.ElementSink#edgeAdded(java.lang.String, long,
     * java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void edgeAdded(String sourceId, long timeId, String edgeId,
	    String fromNodeId, String toNodeId, boolean directed) {
	// make an JSON object
	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(
		    Types.AE.value(),
		    new JSONObject().put(
			    edgeId,
			    new JSONObject()
				    .put(Fields.SOURCE.value(), fromNodeId)
				    .put(Fields.TARGET.value(), toNodeId)
				    .put(Fields.DIRECTED.value(), directed)));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.graphstream.stream.ElementSink#edgeRemoved(java.lang.String,
     * long, java.lang.String)
     */
    @Override
    public void edgeRemoved(String sourceId, long timeId, String edgeId) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(Types.DE.value(),
		    new JSONObject().put(edgeId, new JSONObject()));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.graphstream.stream.ElementSink#graphCleared(java.lang.String,
     * long)
     */
    @Override
    public void graphCleared(String sourceId, long timeId) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(Types.DN.value(),
		    new JSONObject().put("filter", "ALL"));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.graphstream.stream.ElementSink#nodeAdded(java.lang.String, long,
     * java.lang.String)
     */
    @Override
    public void nodeAdded(String sourceId, long timeId, String nodeId) {

	// make an JSON object
	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(Types.AN.value(),
		    new JSONObject().put(nodeId, new JSONObject()));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.graphstream.stream.ElementSink#nodeRemoved(java.lang.String,
     * long, java.lang.String)
     */
    @Override
    public void nodeRemoved(String sourceId, long timeId, String nodeId) {

	JSONObject jsonObj = null;
	try {
	    jsonObj = new JSONObject().put(Types.DN.value(),
		    new JSONObject().put(nodeId, new JSONObject()));
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (debug)
	    debug(jsonObj.toString());

	// send jsonObj to Gephi
	doSend(jsonObj, "updateGraph");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.graphstream.stream.ElementSink#stepBegins(java.lang.String,
     * long, double)
     */
    @Override
    public void stepBegins(String sourceId, long timeId, double step) {
	// TODO Auto-generated method stub

    }

    /**
     * Send JSONObject message to Gephi server
     * 
     * @param obj
     *            , the JSON message content
     * @param operation
     *            , the operation sending to the server, like "updateGraph",
     *            "getGraph"
     */
    private void doSend(JSONObject obj, String operation) {

	try {
	    URL url = new URL("http", host, port, "/" + workspace
		    + "?operation=" + operation + "&format=JSON");

	    URLConnection connection = url.openConnection();

	    connection.setDoOutput(true);
	    connection.connect();

	    OutputStream outputStream = null;
	    PrintStream out = null;
	    try {
		outputStream = connection.getOutputStream();
		out = new PrintStream(outputStream, true);

		out.print(obj.toString() + EOL);
		out.flush();
		out.close();

		// send event message to the server and read the result from the
		// server
		InputStream inputStream = connection.getInputStream();
		BufferedReader bf = new BufferedReader(new InputStreamReader(
			inputStream));
		String line;
		while ((line = bf.readLine()) != null) {
		    // if (debug) debug(line);
		}
		inputStream.close();
	    } catch (UnknownServiceException e) {
		// protocol doesn't support output
		e.printStackTrace();
		return;
	    }
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }
}
