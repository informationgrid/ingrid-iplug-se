/**
 * 
 */
package org.apache.nutch.net.urlnormalizer.regex;

import junit.framework.TestCase;

import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Pattern;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

/**
 * @author joachim
 *
 */
public class TestRegExURLNormalizerHelper extends TestCase {

    private PatternMatcher matcher = new Perl5Matcher();
    Perl5Compiler compiler = new Perl5Compiler(); 
    
    public void testNormalizerDefault() throws Exception {
        
        Perl5Pattern pattern = (Perl5Pattern) compiler.compile("(\\?|&|/|)([;_]?((?i)l|j|bv_|ps_)?((?i)s|sid|phpsessid|sessionid|conversationid|sess_id)(=|_).*?)(\\?|&|#|/|$)");
        String substitution = "$1";

        String urlString = Util.substitute(matcher, pattern, new Perl5Substitution(
                substitution), "http://www.endlager-asse.de/cln_135?sid=EECCBC17C7694EC084DC72B530305DF1&blablabla=w", Util.SUBSTITUTE_ALL); // actual
        assertEquals("http://www.endlager-asse.de/cln_135?blablabla=w", urlString);

        urlString = Util.substitute(matcher, pattern, new Perl5Substitution(
                substitution), "http://www.endlager-asse.de/cln_135/sid_EECCBC17C7694EC084DC72B530305DF1/DE/5_AsseService/A_Umgebungsueberwachung/_node.html", Util.SUBSTITUTE_ALL); // actual
        assertEquals("http://www.endlager-asse.de/cln_135/DE/5_AsseService/A_Umgebungsueberwachung/_node.html", urlString);
        
        urlString = Util.substitute(matcher, pattern, new Perl5Substitution(
                substitution), "http://www.endlager-asse.de/cln_135/sidcom/sid_EECCBC17C7694EC084DC72B530305DF1/DE/5_AsseService/A_Umgebungsueberwachung/_node.html", Util.SUBSTITUTE_ALL); // actual
        assertEquals("http://www.endlager-asse.de/cln_135/sidcom/DE/5_AsseService/A_Umgebungsueberwachung/_node.html", urlString);
    }
    
}
