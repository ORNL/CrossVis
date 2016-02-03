package prefuse.data.parser;

import java.util.StringTokenizer;

/**
 * DataParser instance the parses an array of float values from a text string.
 * Values are expected to be comma separated and can be within brackets,
 * parentheses, or curly braces.
 *  
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class FloatArrayParser implements DataParser {
    
    /**
     * Returns float[].class.
     * @see prefuse.data.parser.DataParser#getType()
     */
    public Class getType() {
        return float[].class;
    }
    
    /**
     * @see prefuse.data.parser.DataParser#format(java.lang.Object)
     */
    public String format(Object value) {
        if ( value == null ) return null;
        if ( !(value instanceof float[]) )
            throw new IllegalArgumentException(
              "This class can only format Objects of type float[].");
        
        float[] values = (float[])value;
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('[');
        for ( int i=0; i<values.length; ++i ) {
            if ( i > 0 ) sbuf.append(", ");
            sbuf.append(values[i]).append('f');
        }
        sbuf.append(']');
        return sbuf.toString();
    }
    
    /**
     * @see prefuse.data.parser.DataParser#canParse(java.lang.String)
     */
    public boolean canParse(String text) {
        try {
            StringTokenizer st = new StringTokenizer(text, "\"[](){}, ");
            while ( st.hasMoreTokens() ) {
                Float.parseFloat(st.nextToken());
            }
            return true;
        } catch ( NumberFormatException e ) {
            return false;
        }
    }
    
    /**
     * Parse an int array from a text string.
     * @param text the text string to parse
     * @return the parsed integer array
     * @throws DataParseException if an error occurs during parsing
     */
    public Object parse(String text) throws DataParseException {
        try {
            StringTokenizer st = new StringTokenizer(text, "\"[](){}, ");
            float[] array = new float[st.countTokens()];
            for ( int i=0; st.hasMoreTokens(); ++i ) {
                String tok = st.nextToken();
                array[i] = Float.parseFloat(tok);
            }
            return array;
        } catch ( NumberFormatException e ) {
            throw new DataParseException(e);
        }
    }
    
} // end of class FloatArrayParser
