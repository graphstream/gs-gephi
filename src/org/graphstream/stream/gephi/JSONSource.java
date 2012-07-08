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
import org.graphstream.graph.implementations.AbstractElement.AttributeChangeEvent;
import org.graphstream.stream.gephi.JSONEventConstants.Fields;
import org.graphstream.stream.gephi.JSONEventConstants.Types;
import org.graphstream.stream.SourceBase;

/**
 * connect GraphStream to Gephi
 * Gephi works as source, sends events to GraphStream
 * @author wumalbert
 *
 */
public class JSONSource extends SourceBase {

    private String host; // the host of the Gephi server
    private int port; // the port of the Gephi server
    private String workspace; // the workspace name of the Gephi server
    private String sourceId; // the gephi source ID

    /**
     * 
     * @param host, the host of the Gephi server
     * @param port, the port of the Gephi server
     * @param workspace, the workspace name of the Gephi server
     */
    public JSONSource(String host, int port, String workspace) {
	this.host = host;
	this.port = port;
	this.workspace = workspace;
	this.sourceId = String.format("<Gephi json stream %x>", System.nanoTime());
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
        	this.sendGraphCleared(sourceId);
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
        	this.sendNodeAdded(sourceId, elementId);
        	this.sendAttributes(gObj, elementId, ElementType.NODE, AttributeChangeEvent.ADD);
        	break;
            case CN:
        	this.sendAttributes(gObj, elementId, ElementType.NODE, AttributeChangeEvent.CHANGE);
        	break;
            case DN:
        	this.sendNodeRemoved(sourceId, elementId);
        	break;
            case AE:
                String fromNodeId = gObj.getString(Fields.SOURCE.value());
                String toNodeId = gObj.getString(Fields.TARGET.value());
                
                boolean directed = true;
                if (gObj.has(Fields.DIRECTED.value())) {
                    directed = Boolean.valueOf(gObj.getString(Fields.DIRECTED.value()));
                }
                this.sendEdgeAdded(sourceId, elementId, fromNodeId, toNodeId, directed);
               
                Iterator<String> i2 = gObj.keys();
                while (i2.hasNext()) {
                    String key = i2.next();
                    
                    if (key.equals(Fields.SOURCE.value())) continue;
                    if (key.equals(Fields.TARGET.value())) continue;
                    if (key.equals(Fields.DIRECTED.value())) continue;

                    Object value = gObj.get(key);
                    this.sendAttributeChangedEvent(sourceId, elementId, ElementType.EDGE, 
                	    key, AttributeChangeEvent.ADD, null, value);
                }
        	break;
            case CE:
        	this.sendAttributes(gObj, elementId, ElementType.EDGE, AttributeChangeEvent.CHANGE);
        	break;
            case DE:
        	this.sendEdgeRemoved(sourceId, elementId);
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
	    sendAttributeChangedEvent(sourceId, elementId, elementType, key, changeEventType, null, value);
	}
    }
    
    /**
     * connect to Gephi and process the stream received from Gephi
     */
    public void processStream() {
	try {
	    //connect to Gephi server
	    //use getGraph operation to get graphs from Gephi with the following URL 
	    //http://localhost:8080/workspace0?operation=getGrap
            URL url = new URL("http", host, port, "/"+workspace+"?operation=getGraph");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(false);
            connection.connect();
    
            //read the result from the servers
            try {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bf.readLine()) != null) {
                    System.out.println(line);
                    // each line is a event in Gephi
                    parse(line);
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
