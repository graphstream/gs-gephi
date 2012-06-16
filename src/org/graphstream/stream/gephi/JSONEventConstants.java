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
	AN("an"),
    	CN("cn"),
    	DN("dn"),
    	AE("ae"),
    	CE("ce"),
        DE("de"),
        CG("cg");

        private String value;

        private Types(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
	
    public enum Fields {    	
        ID("id"),
        T("t"), //timestamp
        SOURCE("source"),
        TARGET("target"),
        DIRECTED("directed");
        private String value;

        private Fields(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    };
}
