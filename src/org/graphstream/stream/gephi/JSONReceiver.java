package org.graphstream.stream.gephi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.AbstractElement.AttributeChangeEvent;
import org.graphstream.stream.gephi.JSONEventConstants.Fields;
import org.graphstream.stream.gephi.JSONEventConstants.Types;
import org.graphstream.stream.thread.ThreadProxyPipe;
import org.graphstream.stream.Source;
import org.graphstream.stream.SourceBase.ElementType;

/**
 * connect GraphStream to Gephi
 * Gephi works as source, sends events to GraphStream
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
    private String sourceId; 
    
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
     * @param host, the host of the Gephi server
     * @param port, the port of the Gephi server
     * @param workspace, the workspace name of the Gephi server
     */
    public JSONReceiver(String host, int port, String workspace) {
	this.host = host;
	this.port = port;
	this.workspace = workspace;
	this.sourceId = String.format("<Gephi json stream %x>", System.nanoTime());
	this.debug = false;
	
	currentStream = new ThreadProxyPipe();
	init();
	start();
    }

    /**
     * 
     * @param host, the host of the Gephi server
     * @param port, the port of the Gephi server
     * @param workspace, the workspace name of the Gephi server
     * @param debug, the program mode
     */
    public JSONReceiver(String host, int port, String workspace, boolean debug) {
	this.host = host;
	this.port = port;
	this.workspace = workspace;
	this.sourceId = String.format("<Gephi json stream %x>", System.nanoTime());
	this.debug = debug;
	
	currentStream = new ThreadProxyPipe();
	init();
	start();
    }
    
    /**
     * set debug mode
     * @param debug
     */
    public void setDebug(boolean debug) {
	this.debug = debug;
    }
    
    /**
     * set debug message
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
	    //connect to Gephi server
	    //use getGraph operation to get graphs from Gephi with the following URL 
	    //http://localhost:8080/workspace0?operation=getGrap
            URL url = new URL("http", host, port, "/"+workspace+"?operation=getGraph");
            urlConnection = url.openConnection();
            urlConnection.setDoOutput(false);
            urlConnection.connect();
	} catch (IOException ex) {
	    //can't connect to gephi
	    ex.printStackTrace();
	}
    }
    
    /**
     * process the stream received from Gephi
     */
    public void run() {
        //read the result from the servers
        try {
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                if(debug) debug(line);
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
     * Parse the JSON string received from Gephi as events to be processed by GraphStream
     * {<event_type>:{<object_identifier>:{<attribute_name>:<attribute_value>,<attribute_name>:<attribute_value>}}}
     * {
     *   "id": "1278944510", //event identifier
     *   "t":**, //time stamp
     *   "an":{"A":{"label":"Streaming Node A","size":2}
     *         "B":{"label":"Streaming Node B","size":1}
     *         "C":{"label":"Streaming Node C","size":1}
     *   }, 
     *   "dn":{"filter":"ALL"}
     * }    
     * @param content, GSON string received from Gephi
     */
    private void parse(String content) {
        content = content.trim();
        if (content.length() == 0) return;
        
        try {
            JSONObject jo = new JSONObject(content);
            
            String id = null; //event ID
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
                if (Fields.ID.value().equals(key)) continue;
                if (Fields.T.value().equals(key)) continue;
                
                Object gObjs = jo.get(key);
                if (gObjs instanceof JSONObject) {
                    parse(key, (JSONObject)gObjs, id, t);
                } else {
                    throw new IllegalArgumentException("Invalid attribute: "+key);
                    //logger.log(Level.WARNING, "JSON attribute ignored: \"{0}\"", new String[]{key});
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Parse the detailed JSON string received from Gephi
     * @param type, event type, one of AN,CN,DN,AE,CE,DE,CG
     * @param gObjs, JSON message
     * @param eventId, event identifier
     * @param t, time stamp of the event
     * @throws JSONException
     */
    private void parse(String type, JSONObject gObjs, String eventId, Double t) throws JSONException {

        Types eventType = Types.fromString(type);
        
        if (gObjs.has("filter")) {
            if (gObjs.getString("filter").equals("ALL")) {
        	currentStream.sendGraphCleared(sourceId);
            }
            /*Map<String, Object> attributes = null;
            if (gObjs.has("attributes")) {
                JSONObject attrObj = gObjs.getJSONObject("attributes");
                attributes = readAttributes(attrObj);
            }
            
            handler.handleGraphEvent(
                    new FilterEvent(this, eventType.getEventType(),
                    eventType.getElementType(), getFilter(eventType.getElementType(), gObjs), attributes));*/
            return;
        }

        if (eventType.equals(Types.CG)) {
            this.sendAttributes(gObjs, null, ElementType.GRAPH, AttributeChangeEvent.CHANGE);
            return;
        }
        
        Iterator<String> it = gObjs.keys();
        while (it.hasNext()) {
            //GraphEvent event = null;
            String elementId = it.next();
            JSONObject gObj = (JSONObject)gObjs.get(elementId);
            
            switch( eventType ) {
            case AN:
        	currentStream.sendNodeAdded(sourceId, elementId);
        	this.sendAttributes(gObj, elementId, ElementType.NODE, AttributeChangeEvent.ADD);
        	break;
            case CN:
        	this.sendAttributes(gObj, elementId, ElementType.NODE, AttributeChangeEvent.CHANGE);
        	break;
            case DN:
        	currentStream.sendNodeRemoved(sourceId, elementId);
        	break;
            case AE:
                String fromNodeId = gObj.getString(Fields.SOURCE.value());
                String toNodeId = gObj.getString(Fields.TARGET.value());
                
                boolean directed = true;
                if (gObj.has(Fields.DIRECTED.value())) {
                    directed = Boolean.valueOf(gObj.getString(Fields.DIRECTED.value()));
                }
                currentStream.sendEdgeAdded(sourceId, elementId, fromNodeId, toNodeId, directed);
               
                Iterator<String> i2 = gObj.keys();
                while (i2.hasNext()) {
                    String key = i2.next();
                    
                    if (key.equals(Fields.SOURCE.value())) continue;
                    if (key.equals(Fields.TARGET.value())) continue;
                    if (key.equals(Fields.DIRECTED.value())) continue;

                    Object value = gObj.get(key);
                    currentStream.sendAttributeChangedEvent(sourceId, elementId, ElementType.EDGE, 
                	    key, AttributeChangeEvent.ADD, null, value);
                }
        	break;
            case CE:
        	this.sendAttributes(gObj, elementId, ElementType.EDGE, AttributeChangeEvent.CHANGE);
        	break;
            case DE:
        	currentStream.sendEdgeRemoved(sourceId, elementId);
        	break;
            
            }
        }
    }

    /**
     * read attributes from the JSON object and send them to GraphStream 
     * @param gObj, the attributes JSON Object
     * @param elementId, elementId
     * @param elementType, element type, GRAPH, NODE, EDGE
     * @param changeEventType, attribute change event type, AND, CHANGE, REMOVE 
     * @throws JSONException
     */
    private void sendAttributes(JSONObject gObj, String elementId, ElementType elementType, 
	    AttributeChangeEvent changeEventType) throws JSONException {
	Iterator<String> it = gObj.keys();
	while (it.hasNext()) {
	    String key = it.next();
	    Object value = gObj.get(key);
	    currentStream.sendAttributeChangedEvent(sourceId, elementId, elementType, key, changeEventType, null, value);
	}
    }
}
