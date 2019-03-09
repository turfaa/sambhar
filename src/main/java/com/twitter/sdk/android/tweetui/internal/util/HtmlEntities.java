package com.twitter.sdk.android.tweetui.internal.util;

import com.facebook.appevents.UserDataStore;
import com.facebook.share.internal.MessengerShareContentUtility;
import com.twitter.sdk.android.core.internal.TwitterApiConstants.Errors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HtmlEntities {
    private static final String[][] BASIC_ARRAY;
    public static final HtmlEntities HTML40 = new HtmlEntities();
    static final String[][] HTML40_ARRAY;
    static final String[][] ISO8859_1_ARRAY;
    final EntityMap map = new LookupEntityMap();

    interface EntityMap {
        void add(String str, int i);

        String name(int i);

        int value(String str);
    }

    public static final class Unescaped {
        public final ArrayList<int[]> indices;
        public final String unescaped;

        public Unescaped(String str, ArrayList<int[]> arrayList) {
            this.unescaped = str;
            this.indices = arrayList;
        }
    }

    static class PrimitiveEntityMap implements EntityMap {
        private final Map mapNameToValue = new HashMap();
        private final IntHashMap mapValueToName = new IntHashMap();

        PrimitiveEntityMap() {
        }

        public void add(String str, int i) {
            this.mapNameToValue.put(str, Integer.valueOf(i));
            this.mapValueToName.put(i, str);
        }

        public String name(int i) {
            return (String) this.mapValueToName.get(i);
        }

        public int value(String str) {
            Object obj = this.mapNameToValue.get(str);
            if (obj == null) {
                return -1;
            }
            return ((Integer) obj).intValue();
        }
    }

    static class LookupEntityMap extends PrimitiveEntityMap {
        private static final int LOOKUP_TABLE_SIZE = 256;
        private String[] lookupTable;

        LookupEntityMap() {
        }

        public String name(int i) {
            if (i < 256) {
                return lookupTable()[i];
            }
            return super.name(i);
        }

        private String[] lookupTable() {
            if (this.lookupTable == null) {
                createLookupTable();
            }
            return this.lookupTable;
        }

        private void createLookupTable() {
            this.lookupTable = new String[256];
            for (int i = 0; i < 256; i++) {
                this.lookupTable[i] = super.name(i);
            }
        }
    }

    static {
        r1 = new String[96][];
        r1[0] = new String[]{"nbsp", "160"};
        r1[1] = new String[]{"iexcl", "161"};
        r1[2] = new String[]{"cent", "162"};
        r1[3] = new String[]{"pound", "163"};
        r1[4] = new String[]{"curren", "164"};
        r1[5] = new String[]{"yen", "165"};
        r1[6] = new String[]{"brvbar", "166"};
        r1[7] = new String[]{"sect", "167"};
        r1[8] = new String[]{"uml", "168"};
        r1[9] = new String[]{"copy", "169"};
        r1[10] = new String[]{"ordf", "170"};
        r1[11] = new String[]{"laquo", "171"};
        r1[12] = new String[]{"not", "172"};
        r1[13] = new String[]{"shy", "173"};
        r1[14] = new String[]{"reg", "174"};
        r1[15] = new String[]{"macr", "175"};
        r1[16] = new String[]{"deg", "176"};
        r1[17] = new String[]{"plusmn", "177"};
        r1[18] = new String[]{"sup2", "178"};
        r1[19] = new String[]{"sup3", "179"};
        r1[20] = new String[]{"acute", "180"};
        r1[21] = new String[]{"micro", "181"};
        r1[22] = new String[]{"para", "182"};
        r1[23] = new String[]{"middot", "183"};
        r1[24] = new String[]{"cedil", "184"};
        r1[25] = new String[]{"sup1", "185"};
        r1[26] = new String[]{"ordm", "186"};
        r1[27] = new String[]{"raquo", "187"};
        r1[28] = new String[]{"frac14", "188"};
        r1[29] = new String[]{"frac12", "189"};
        r1[30] = new String[]{"frac34", "190"};
        r1[31] = new String[]{"iquest", "191"};
        r1[32] = new String[]{"Agrave", "192"};
        r1[33] = new String[]{"Aacute", "193"};
        r1[34] = new String[]{"Acirc", "194"};
        r1[35] = new String[]{"Atilde", "195"};
        r1[36] = new String[]{"Auml", "196"};
        r1[37] = new String[]{"Aring", "197"};
        r1[38] = new String[]{"AElig", "198"};
        r1[39] = new String[]{"Ccedil", "199"};
        r1[40] = new String[]{"Egrave", "200"};
        r1[41] = new String[]{"Eacute", "201"};
        r1[42] = new String[]{"Ecirc", "202"};
        r1[43] = new String[]{"Euml", "203"};
        r1[44] = new String[]{"Igrave", "204"};
        r1[45] = new String[]{"Iacute", "205"};
        r1[46] = new String[]{"Icirc", "206"};
        r1[47] = new String[]{"Iuml", "207"};
        r1[48] = new String[]{"ETH", "208"};
        r1[49] = new String[]{"Ntilde", "209"};
        r1[50] = new String[]{"Ograve", "210"};
        r1[51] = new String[]{"Oacute", "211"};
        r1[52] = new String[]{"Ocirc", "212"};
        r1[53] = new String[]{"Otilde", "213"};
        r1[54] = new String[]{"Ouml", "214"};
        r1[55] = new String[]{"times", "215"};
        r1[56] = new String[]{"Oslash", "216"};
        r1[57] = new String[]{"Ugrave", "217"};
        r1[58] = new String[]{"Uacute", "218"};
        r1[59] = new String[]{"Ucirc", "219"};
        r1[60] = new String[]{"Uuml", "220"};
        r1[61] = new String[]{"Yacute", "221"};
        r1[62] = new String[]{"THORN", "222"};
        r1[63] = new String[]{"szlig", "223"};
        r1[64] = new String[]{"agrave", "224"};
        r1[65] = new String[]{"aacute", "225"};
        r1[66] = new String[]{"acirc", "226"};
        r1[67] = new String[]{"atilde", "227"};
        r1[68] = new String[]{"auml", "228"};
        r1[69] = new String[]{"aring", "229"};
        r1[70] = new String[]{"aelig", "230"};
        r1[71] = new String[]{"ccedil", "231"};
        r1[72] = new String[]{"egrave", "232"};
        r1[73] = new String[]{"eacute", "233"};
        r1[74] = new String[]{"ecirc", "234"};
        r1[75] = new String[]{"euml", "235"};
        r1[76] = new String[]{"igrave", "236"};
        r1[77] = new String[]{"iacute", "237"};
        r1[78] = new String[]{"icirc", "238"};
        r1[79] = new String[]{"iuml", "239"};
        r1[80] = new String[]{"eth", "240"};
        r1[81] = new String[]{"ntilde", "241"};
        r1[82] = new String[]{"ograve", "242"};
        r1[83] = new String[]{"oacute", "243"};
        r1[84] = new String[]{"ocirc", "244"};
        r1[85] = new String[]{"otilde", "245"};
        r1[86] = new String[]{"ouml", "246"};
        r1[87] = new String[]{"divide", "247"};
        r1[88] = new String[]{"oslash", "248"};
        r1[89] = new String[]{"ugrave", "249"};
        r1[90] = new String[]{"uacute", "250"};
        r1[91] = new String[]{"ucirc", "251"};
        r1[92] = new String[]{"uuml", "252"};
        r1[93] = new String[]{"yacute", "253"};
        r1[94] = new String[]{"thorn", "254"};
        r1[95] = new String[]{"yuml", "255"};
        ISO8859_1_ARRAY = r1;
        r1 = new String[151][];
        r1[0] = new String[]{"fnof", "402"};
        r1[1] = new String[]{"Alpha", "913"};
        r1[2] = new String[]{"Beta", "914"};
        r1[3] = new String[]{"Gamma", "915"};
        r1[4] = new String[]{"Delta", "916"};
        r1[5] = new String[]{"Epsilon", "917"};
        r1[6] = new String[]{"Zeta", "918"};
        r1[7] = new String[]{"Eta", "919"};
        r1[8] = new String[]{"Theta", "920"};
        r1[9] = new String[]{"Iota", "921"};
        r1[10] = new String[]{"Kappa", "922"};
        r1[11] = new String[]{"Lambda", "923"};
        r1[12] = new String[]{"Mu", "924"};
        r1[13] = new String[]{"Nu", "925"};
        r1[14] = new String[]{"Xi", "926"};
        r1[15] = new String[]{"Omicron", "927"};
        r1[16] = new String[]{"Pi", "928"};
        r1[17] = new String[]{"Rho", "929"};
        r1[18] = new String[]{"Sigma", "931"};
        r1[19] = new String[]{"Tau", "932"};
        r1[20] = new String[]{"Upsilon", "933"};
        r1[21] = new String[]{"Phi", "934"};
        r1[22] = new String[]{"Chi", "935"};
        r1[23] = new String[]{"Psi", "936"};
        r1[24] = new String[]{"Omega", "937"};
        r1[25] = new String[]{"alpha", "945"};
        r1[26] = new String[]{"beta", "946"};
        r1[27] = new String[]{"gamma", "947"};
        r1[28] = new String[]{"delta", "948"};
        r1[29] = new String[]{"epsilon", "949"};
        r1[30] = new String[]{"zeta", "950"};
        r1[31] = new String[]{"eta", "951"};
        r1[32] = new String[]{"theta", "952"};
        r1[33] = new String[]{"iota", "953"};
        r1[34] = new String[]{"kappa", "954"};
        r1[35] = new String[]{"lambda", "955"};
        r1[36] = new String[]{"mu", "956"};
        r1[37] = new String[]{"nu", "957"};
        r1[38] = new String[]{"xi", "958"};
        r1[39] = new String[]{"omicron", "959"};
        r1[40] = new String[]{"pi", "960"};
        r1[41] = new String[]{"rho", "961"};
        r1[42] = new String[]{"sigmaf", "962"};
        r1[43] = new String[]{"sigma", "963"};
        r1[44] = new String[]{"tau", "964"};
        r1[45] = new String[]{"upsilon", "965"};
        r1[46] = new String[]{"phi", "966"};
        r1[47] = new String[]{"chi", "967"};
        r1[48] = new String[]{"psi", "968"};
        r1[49] = new String[]{"omega", "969"};
        r1[50] = new String[]{"thetasym", "977"};
        r1[51] = new String[]{"upsih", "978"};
        r1[52] = new String[]{"piv", "982"};
        r1[53] = new String[]{"bull", "8226"};
        r1[54] = new String[]{"hellip", "8230"};
        r1[55] = new String[]{"prime", "8242"};
        r1[56] = new String[]{"Prime", "8243"};
        r1[57] = new String[]{"oline", "8254"};
        r1[58] = new String[]{"frasl", "8260"};
        r1[59] = new String[]{"weierp", "8472"};
        r1[60] = new String[]{MessengerShareContentUtility.MEDIA_IMAGE, "8465"};
        r1[61] = new String[]{"real", "8476"};
        r1[62] = new String[]{"trade", "8482"};
        r1[63] = new String[]{"alefsym", "8501"};
        r1[64] = new String[]{"larr", "8592"};
        r1[65] = new String[]{"uarr", "8593"};
        r1[66] = new String[]{"rarr", "8594"};
        r1[67] = new String[]{"darr", "8595"};
        r1[68] = new String[]{"harr", "8596"};
        r1[69] = new String[]{"crarr", "8629"};
        r1[70] = new String[]{"lArr", "8656"};
        r1[71] = new String[]{"uArr", "8657"};
        r1[72] = new String[]{"rArr", "8658"};
        r1[73] = new String[]{"dArr", "8659"};
        r1[74] = new String[]{"hArr", "8660"};
        r1[75] = new String[]{"forall", "8704"};
        r1[76] = new String[]{"part", "8706"};
        r1[77] = new String[]{"exist", "8707"};
        r1[78] = new String[]{"empty", "8709"};
        r1[79] = new String[]{"nabla", "8711"};
        r1[80] = new String[]{"isin", "8712"};
        r1[81] = new String[]{"notin", "8713"};
        r1[82] = new String[]{"ni", "8715"};
        r1[83] = new String[]{"prod", "8719"};
        r1[84] = new String[]{"sum", "8721"};
        r1[85] = new String[]{"minus", "8722"};
        r1[86] = new String[]{"lowast", "8727"};
        r1[87] = new String[]{"radic", "8730"};
        r1[88] = new String[]{"prop", "8733"};
        r1[89] = new String[]{"infin", "8734"};
        r1[90] = new String[]{"ang", "8736"};
        r1[91] = new String[]{"and", "8743"};
        r1[92] = new String[]{"or", "8744"};
        r1[93] = new String[]{"cap", "8745"};
        r1[94] = new String[]{"cup", "8746"};
        r1[95] = new String[]{"int", "8747"};
        r1[96] = new String[]{"there4", "8756"};
        r1[97] = new String[]{"sim", "8764"};
        r1[98] = new String[]{"cong", "8773"};
        r1[99] = new String[]{"asymp", "8776"};
        r1[100] = new String[]{"ne", "8800"};
        r1[101] = new String[]{"equiv", "8801"};
        r1[102] = new String[]{"le", "8804"};
        r1[103] = new String[]{UserDataStore.GENDER, "8805"};
        r1[104] = new String[]{"sub", "8834"};
        r1[105] = new String[]{"sup", "8835"};
        r1[106] = new String[]{"sube", "8838"};
        r1[107] = new String[]{"supe", "8839"};
        r1[108] = new String[]{"oplus", "8853"};
        r1[109] = new String[]{"otimes", "8855"};
        r1[110] = new String[]{"perp", "8869"};
        r1[111] = new String[]{"sdot", "8901"};
        r1[112] = new String[]{"lceil", "8968"};
        r1[113] = new String[]{"rceil", "8969"};
        r1[114] = new String[]{"lfloor", "8970"};
        r1[115] = new String[]{"rfloor", "8971"};
        r1[116] = new String[]{"lang", "9001"};
        r1[117] = new String[]{"rang", "9002"};
        r1[118] = new String[]{"loz", "9674"};
        r1[119] = new String[]{"spades", "9824"};
        r1[120] = new String[]{"clubs", "9827"};
        r1[121] = new String[]{"hearts", "9829"};
        r1[122] = new String[]{"diams", "9830"};
        r1[123] = new String[]{"OElig", "338"};
        r1[124] = new String[]{"oelig", "339"};
        r1[125] = new String[]{"Scaron", "352"};
        r1[126] = new String[]{"scaron", "353"};
        r1[127] = new String[]{"Yuml", "376"};
        r1[128] = new String[]{"circ", "710"};
        r1[129] = new String[]{"tilde", "732"};
        r1[130] = new String[]{"ensp", "8194"};
        r1[131] = new String[]{"emsp", "8195"};
        r1[132] = new String[]{"thinsp", "8201"};
        r1[133] = new String[]{"zwnj", "8204"};
        r1[134] = new String[]{"zwj", "8205"};
        r1[135] = new String[]{"lrm", "8206"};
        r1[136] = new String[]{"rlm", "8207"};
        r1[137] = new String[]{"ndash", "8211"};
        r1[138] = new String[]{"mdash", "8212"};
        r1[Errors.ALREADY_FAVORITED] = new String[]{"lsquo", "8216"};
        r1[140] = new String[]{"rsquo", "8217"};
        r1[141] = new String[]{"sbquo", "8218"};
        r1[142] = new String[]{"ldquo", "8220"};
        r1[143] = new String[]{"rdquo", "8221"};
        r1[Errors.ALREADY_UNFAVORITED] = new String[]{"bdquo", "8222"};
        r1[145] = new String[]{"dagger", "8224"};
        r1[146] = new String[]{"Dagger", "8225"};
        r1[147] = new String[]{"permil", "8240"};
        r1[148] = new String[]{"lsaquo", "8249"};
        r1[149] = new String[]{"rsaquo", "8250"};
        r1[150] = new String[]{"euro", "8364"};
        HTML40_ARRAY = r1;
        r0 = new String[4][];
        r0[0] = new String[]{"quot", "34"};
        r0[1] = new String[]{"amp", "38"};
        r0[2] = new String[]{"lt", "60"};
        r0[3] = new String[]{"gt", "62"};
        BASIC_ARRAY = r0;
        fillWithHtml40Entities(HTML40);
    }

    static void fillWithHtml40Entities(HtmlEntities htmlEntities) {
        htmlEntities.addEntities(BASIC_ARRAY);
        htmlEntities.addEntities(ISO8859_1_ARRAY);
        htmlEntities.addEntities(HTML40_ARRAY);
    }

    public void addEntities(String[][] strArr) {
        for (String[] strArr2 : strArr) {
            addEntity(strArr2[0], Integer.parseInt(strArr2[1]));
        }
    }

    public void addEntity(String str, int i) {
        this.map.add(str, i);
    }

    public int entityValue(String str) {
        return this.map.value(str);
    }

    public Unescaped unescape(String str) {
        String str2 = str;
        int length = str.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        ArrayList arrayList = new ArrayList(5);
        int i = 0;
        while (i < length) {
            char charAt = str2.charAt(i);
            if (charAt == '&') {
                int i2 = i + 1;
                int indexOf = str2.indexOf(59, i2);
                if (indexOf == -1) {
                    stringBuilder.append(charAt);
                } else {
                    int i3;
                    String substring = str2.substring(i2, indexOf);
                    i2 = substring.length();
                    if (i2 <= 0) {
                        i3 = -1;
                    } else if (substring.charAt(0) != '#' || i2 <= 1) {
                        i3 = entityValue(substring);
                    } else {
                        char charAt2 = substring.charAt(1);
                        if (charAt2 != 'x' && charAt2 != 'X') {
                            try {
                                i2 = Integer.parseInt(substring.substring(1));
                            } catch (Exception unused) {
                            }
                            i3 = i2;
                        } else if (i2 > 2) {
                            i2 = Integer.valueOf(substring.substring(2), 16).intValue();
                            i3 = i2;
                        }
                        i2 = -1;
                        i3 = i2;
                    }
                    if (i3 == -1) {
                        stringBuilder.append('&');
                        if (substring.indexOf(38) == -1) {
                            stringBuilder.append(substring);
                            stringBuilder.append(';');
                        }
                    } else {
                        stringBuilder.append((char) i3);
                        arrayList.add(new int[]{i, indexOf});
                    }
                    i = indexOf;
                }
            } else {
                stringBuilder.append(charAt);
            }
            i++;
        }
        return new Unescaped(stringBuilder.toString(), arrayList);
    }
}
