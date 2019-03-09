package com.twitter;

import com.facebook.share.internal.ShareConstants;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.twitter.Extractor.Entity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Autolink {
    public static final String DEFAULT_CASHTAG_CLASS = "tweet-url cashtag";
    public static final String DEFAULT_CASHTAG_URL_BASE = "https://twitter.com/#!/search?q=%24";
    public static final String DEFAULT_HASHTAG_CLASS = "tweet-url hashtag";
    public static final String DEFAULT_HASHTAG_URL_BASE = "https://twitter.com/#!/search?q=%23";
    public static final String DEFAULT_INVISIBLE_TAG_ATTRS = "style='position:absolute;left:-9999px;'";
    public static final String DEFAULT_LIST_CLASS = "tweet-url list-slug";
    public static final String DEFAULT_LIST_URL_BASE = "https://twitter.com/";
    public static final String DEFAULT_USERNAME_CLASS = "tweet-url username";
    public static final String DEFAULT_USERNAME_URL_BASE = "https://twitter.com/";
    protected String cashtagClass;
    protected String cashtagUrlBase;
    private Extractor extractor;
    protected String hashtagClass;
    protected String hashtagUrlBase;
    protected String invisibleTagAttrs;
    protected LinkAttributeModifier linkAttributeModifier;
    protected LinkTextModifier linkTextModifier;
    protected String listClass;
    protected String listUrlBase;
    protected boolean noFollow;
    protected String symbolTag;
    protected String textWithSymbolTag;
    protected String urlClass;
    protected String urlTarget;
    protected String usernameClass;
    protected boolean usernameIncludeSymbol;
    protected String usernameUrlBase;

    public interface LinkAttributeModifier {
        void modify(Entity entity, Map<String, String> map);
    }

    public interface LinkTextModifier {
        CharSequence modify(Entity entity, CharSequence charSequence);
    }

    private static CharSequence escapeHTML(CharSequence charSequence) {
        StringBuilder stringBuilder = new StringBuilder(charSequence.length() * 2);
        for (int i = 0; i < charSequence.length(); i++) {
            char charAt = charSequence.charAt(i);
            if (charAt == '\"') {
                stringBuilder.append("&quot;");
            } else if (charAt == '<') {
                stringBuilder.append("&lt;");
            } else if (charAt != '>') {
                switch (charAt) {
                    case '&':
                        stringBuilder.append("&amp;");
                        break;
                    case '\'':
                        stringBuilder.append("&#39;");
                        break;
                    default:
                        stringBuilder.append(charAt);
                        break;
                }
            } else {
                stringBuilder.append("&gt;");
            }
        }
        return stringBuilder;
    }

    public Autolink() {
        this.urlClass = null;
        this.noFollow = true;
        this.usernameIncludeSymbol = false;
        this.symbolTag = null;
        this.textWithSymbolTag = null;
        this.urlTarget = null;
        this.linkAttributeModifier = null;
        this.linkTextModifier = null;
        this.extractor = new Extractor();
        this.urlClass = null;
        this.listClass = DEFAULT_LIST_CLASS;
        this.usernameClass = DEFAULT_USERNAME_CLASS;
        this.hashtagClass = DEFAULT_HASHTAG_CLASS;
        this.cashtagClass = DEFAULT_CASHTAG_CLASS;
        this.usernameUrlBase = "https://twitter.com/";
        this.listUrlBase = "https://twitter.com/";
        this.hashtagUrlBase = DEFAULT_HASHTAG_URL_BASE;
        this.cashtagUrlBase = DEFAULT_CASHTAG_URL_BASE;
        this.invisibleTagAttrs = DEFAULT_INVISIBLE_TAG_ATTRS;
        this.extractor.setExtractURLWithoutProtocol(false);
    }

    public String escapeBrackets(String str) {
        int length = str.length();
        if (length == 0) {
            return str;
        }
        StringBuilder stringBuilder = new StringBuilder(length + 16);
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt == '>') {
                stringBuilder.append("&gt;");
            } else if (charAt == '<') {
                stringBuilder.append("&lt;");
            } else {
                stringBuilder.append(charAt);
            }
        }
        return stringBuilder.toString();
    }

    public void linkToText(Entity entity, CharSequence charSequence, Map<String, String> map, StringBuilder stringBuilder) {
        if (this.noFollow) {
            map.put("rel", "nofollow");
        }
        if (this.linkAttributeModifier != null) {
            this.linkAttributeModifier.modify(entity, map);
        }
        if (this.linkTextModifier != null) {
            charSequence = this.linkTextModifier.modify(entity, charSequence);
        }
        stringBuilder.append("<a");
        for (Entry entry : map.entrySet()) {
            stringBuilder.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
            stringBuilder.append(escapeHTML((CharSequence) entry.getKey()));
            stringBuilder.append("=\"");
            stringBuilder.append(escapeHTML((CharSequence) entry.getValue()));
            stringBuilder.append("\"");
        }
        stringBuilder.append(">");
        stringBuilder.append(charSequence);
        stringBuilder.append("</a>");
    }

    public void linkToTextWithSymbol(Entity entity, CharSequence charSequence, CharSequence charSequence2, Map<String, String> map, StringBuilder stringBuilder) {
        CharSequence charSequence3;
        int i = 1;
        if (this.symbolTag == null || this.symbolTag.length() == 0) {
            charSequence3 = charSequence;
        } else {
            charSequence3 = String.format("<%s>%s</%s>", new Object[]{this.symbolTag, charSequence, this.symbolTag});
        }
        Object escapeHTML = escapeHTML(charSequence2);
        if (!(this.textWithSymbolTag == null || this.textWithSymbolTag.length() == 0)) {
            escapeHTML = String.format("<%s>%s</%s>", new Object[]{this.textWithSymbolTag, escapeHTML, this.textWithSymbolTag});
        }
        if (!this.usernameIncludeSymbol && Regex.AT_SIGNS.matcher(charSequence).matches()) {
            i = 0;
        }
        if (i != 0) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(charSequence3.toString());
            stringBuilder2.append(escapeHTML);
            linkToText(entity, stringBuilder2.toString(), map, stringBuilder);
            return;
        }
        stringBuilder.append(charSequence3);
        linkToText(entity, escapeHTML, map, stringBuilder);
    }

    public void linkToHashtag(Entity entity, String str, StringBuilder stringBuilder) {
        CharSequence subSequence = str.subSequence(entity.getStart().intValue(), entity.getStart().intValue() + 1);
        String value = entity.getValue();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        String str2 = ShareConstants.WEB_DIALOG_PARAM_HREF;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(this.hashtagUrlBase);
        stringBuilder2.append(value);
        linkedHashMap.put(str2, stringBuilder2.toString());
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append("#");
        stringBuilder2.append(value);
        linkedHashMap.put("title", stringBuilder2.toString());
        if (Regex.RTL_CHARACTERS.matcher(str).find()) {
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append(this.hashtagClass);
            stringBuilder3.append(" rtl");
            linkedHashMap.put("class", stringBuilder3.toString());
        } else {
            linkedHashMap.put("class", this.hashtagClass);
        }
        linkToTextWithSymbol(entity, subSequence, value, linkedHashMap, stringBuilder);
    }

    public void linkToCashtag(Entity entity, String str, StringBuilder stringBuilder) {
        String value = entity.getValue();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        str = ShareConstants.WEB_DIALOG_PARAM_HREF;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(this.cashtagUrlBase);
        stringBuilder2.append(value);
        linkedHashMap.put(str, stringBuilder2.toString());
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append("$");
        stringBuilder2.append(value);
        linkedHashMap.put("title", stringBuilder2.toString());
        linkedHashMap.put("class", this.cashtagClass);
        linkToTextWithSymbol(entity, "$", value, linkedHashMap, stringBuilder);
    }

    public void linkToMentionAndList(Entity entity, String str, StringBuilder stringBuilder) {
        String value = entity.getValue();
        CharSequence subSequence = str.subSequence(entity.getStart().intValue(), entity.getStart().intValue() + 1);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        StringBuilder stringBuilder2;
        if (entity.listSlug != null) {
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append(value);
            stringBuilder3.append(entity.listSlug);
            value = stringBuilder3.toString();
            linkedHashMap.put("class", this.listClass);
            str = ShareConstants.WEB_DIALOG_PARAM_HREF;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(this.listUrlBase);
            stringBuilder2.append(value);
            linkedHashMap.put(str, stringBuilder2.toString());
        } else {
            linkedHashMap.put("class", this.usernameClass);
            str = ShareConstants.WEB_DIALOG_PARAM_HREF;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(this.usernameUrlBase);
            stringBuilder2.append(value);
            linkedHashMap.put(str, stringBuilder2.toString());
        }
        linkToTextWithSymbol(entity, subSequence, value, linkedHashMap, stringBuilder);
    }

    public void linkToURL(Entity entity, String str, StringBuilder stringBuilder) {
        str = entity.getValue();
        CharSequence escapeHTML = escapeHTML(str);
        if (!(entity.displayURL == null || entity.expandedURL == null)) {
            String replace = entity.displayURL.replace("…", "");
            int indexOf = entity.expandedURL.indexOf(replace);
            if (indexOf != -1) {
                String substring = entity.expandedURL.substring(0, indexOf);
                String substring2 = entity.expandedURL.substring(indexOf + replace.length());
                String str2 = entity.displayURL.startsWith("…") ? "…" : "";
                String str3 = entity.displayURL.endsWith("…") ? "…" : "";
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("<span ");
                stringBuilder2.append(this.invisibleTagAttrs);
                stringBuilder2.append(">");
                String stringBuilder3 = stringBuilder2.toString();
                StringBuilder stringBuilder4 = new StringBuilder("<span class='tco-ellipsis'>");
                stringBuilder4.append(str2);
                stringBuilder4.append(stringBuilder3);
                stringBuilder4.append("&nbsp;</span></span>");
                stringBuilder4.append(stringBuilder3);
                stringBuilder4.append(escapeHTML(substring));
                stringBuilder4.append("</span>");
                stringBuilder4.append("<span class='js-display-url'>");
                stringBuilder4.append(escapeHTML(replace));
                stringBuilder4.append("</span>");
                stringBuilder4.append(stringBuilder3);
                stringBuilder4.append(escapeHTML(substring2));
                stringBuilder4.append("</span>");
                stringBuilder4.append("<span class='tco-ellipsis'>");
                stringBuilder4.append(stringBuilder3);
                stringBuilder4.append("&nbsp;</span>");
                stringBuilder4.append(str3);
                stringBuilder4.append("</span>");
                escapeHTML = stringBuilder4;
            } else {
                escapeHTML = entity.displayURL;
            }
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put(ShareConstants.WEB_DIALOG_PARAM_HREF, str.toString());
        if (this.urlClass != null) {
            linkedHashMap.put("class", this.urlClass);
        }
        if (!(this.urlClass == null || this.urlClass.length() == 0)) {
            linkedHashMap.put("class", this.urlClass);
        }
        if (!(this.urlTarget == null || this.urlTarget.length() == 0)) {
            linkedHashMap.put("target", this.urlTarget);
        }
        linkToText(entity, escapeHTML, linkedHashMap, stringBuilder);
    }

    public String autoLinkEntities(String str, List<Entity> list) {
        StringBuilder stringBuilder = new StringBuilder(str.length() * 2);
        int i = 0;
        for (Entity entity : list) {
            stringBuilder.append(str.subSequence(i, entity.start));
            switch (entity.type) {
                case URL:
                    linkToURL(entity, str, stringBuilder);
                    break;
                case HASHTAG:
                    linkToHashtag(entity, str, stringBuilder);
                    break;
                case MENTION:
                    linkToMentionAndList(entity, str, stringBuilder);
                    break;
                case CASHTAG:
                    linkToCashtag(entity, str, stringBuilder);
                    break;
                default:
                    break;
            }
            i = entity.end;
        }
        stringBuilder.append(str.subSequence(i, str.length()));
        return stringBuilder.toString();
    }

    public String autoLink(String str) {
        str = escapeBrackets(str);
        return autoLinkEntities(str, this.extractor.extractEntitiesWithIndices(str));
    }

    public String autoLinkUsernamesAndLists(String str) {
        return autoLinkEntities(str, this.extractor.extractMentionsOrListsWithIndices(str));
    }

    public String autoLinkHashtags(String str) {
        return autoLinkEntities(str, this.extractor.extractHashtagsWithIndices(str));
    }

    public String autoLinkURLs(String str) {
        return autoLinkEntities(str, this.extractor.extractURLsWithIndices(str));
    }

    public String autoLinkCashtags(String str) {
        return autoLinkEntities(str, this.extractor.extractCashtagsWithIndices(str));
    }

    public String getUrlClass() {
        return this.urlClass;
    }

    public void setUrlClass(String str) {
        this.urlClass = str;
    }

    public String getListClass() {
        return this.listClass;
    }

    public void setListClass(String str) {
        this.listClass = str;
    }

    public String getUsernameClass() {
        return this.usernameClass;
    }

    public void setUsernameClass(String str) {
        this.usernameClass = str;
    }

    public String getHashtagClass() {
        return this.hashtagClass;
    }

    public void setHashtagClass(String str) {
        this.hashtagClass = str;
    }

    public String getCashtagClass() {
        return this.cashtagClass;
    }

    public void setCashtagClass(String str) {
        this.cashtagClass = str;
    }

    public String getUsernameUrlBase() {
        return this.usernameUrlBase;
    }

    public void setUsernameUrlBase(String str) {
        this.usernameUrlBase = str;
    }

    public String getListUrlBase() {
        return this.listUrlBase;
    }

    public void setListUrlBase(String str) {
        this.listUrlBase = str;
    }

    public String getHashtagUrlBase() {
        return this.hashtagUrlBase;
    }

    public void setHashtagUrlBase(String str) {
        this.hashtagUrlBase = str;
    }

    public String getCashtagUrlBase() {
        return this.cashtagUrlBase;
    }

    public void setCashtagUrlBase(String str) {
        this.cashtagUrlBase = str;
    }

    public boolean isNoFollow() {
        return this.noFollow;
    }

    public void setNoFollow(boolean z) {
        this.noFollow = z;
    }

    public void setUsernameIncludeSymbol(boolean z) {
        this.usernameIncludeSymbol = z;
    }

    public void setSymbolTag(String str) {
        this.symbolTag = str;
    }

    public void setTextWithSymbolTag(String str) {
        this.textWithSymbolTag = str;
    }

    public void setUrlTarget(String str) {
        this.urlTarget = str;
    }

    public void setLinkAttributeModifier(LinkAttributeModifier linkAttributeModifier) {
        this.linkAttributeModifier = linkAttributeModifier;
    }

    public void setLinkTextModifier(LinkTextModifier linkTextModifier) {
        this.linkTextModifier = linkTextModifier;
    }
}
