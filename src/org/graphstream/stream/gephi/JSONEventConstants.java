/**
 * 
 */
package org.graphstream.stream.gephi;

/**
 * @author wumalbert
 * 
 */
public class JSONEventConstants {

    public enum Types {
	AN("an"), CN("cn"), DN("dn"), AE("ae"), CE("ce"), DE("de"), CG("cg");

	private String value;

	private Types(String value) {
	    this.value = value;
	}

	public String value() {
	    return value;
	}

	public static Types fromString(String strtype) {
	    for (Types type : Types.values()) {
		if (type.value.equalsIgnoreCase(strtype)) {
		    return type;
		}
	    }
	    throw new IllegalArgumentException("Invalid type");
	}
    }

    public enum Fields {
	ID("id"), T("t"), // timestamp
	SOURCE("source"), TARGET("target"), DIRECTED("directed");
	private String value;

	private Fields(String value) {
	    this.value = value;
	}

	public String value() {
	    return value;
	}
    };
}
