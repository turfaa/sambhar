package com.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class Extractor {
    protected boolean extractURLWithoutProtocol = true;

    public static class Entity {
        protected String displayURL;
        protected int end;
        protected String expandedURL;
        protected final String listSlug;
        protected int start;
        protected final Type type;
        protected final String value;

        public enum Type {
            URL,
            HASHTAG,
            MENTION,
            CASHTAG
        }

        public Entity(int i, int i2, String str, String str2, Type type) {
            this.displayURL = null;
            this.expandedURL = null;
            this.start = i;
            this.end = i2;
            this.value = str;
            this.listSlug = str2;
            this.type = type;
        }

        public Entity(int i, int i2, String str, Type type) {
            this(i, i2, str, null, type);
        }

        public Entity(Matcher matcher, Type type, int i) {
            this(matcher, type, i, -1);
        }

        public Entity(Matcher matcher, Type type, int i, int i2) {
            this(matcher.start(i) + i2, matcher.end(i), matcher.group(i), type);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Entity)) {
                return false;
            }
            Entity entity = (Entity) obj;
            return this.type.equals(entity.type) && this.start == entity.start && this.end == entity.end && this.value.equals(entity.value);
        }

        public int hashCode() {
            return ((this.type.hashCode() + this.value.hashCode()) + this.start) + this.end;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.value);
            stringBuilder.append("(");
            stringBuilder.append(this.type);
            stringBuilder.append(") [");
            stringBuilder.append(this.start);
            stringBuilder.append(",");
            stringBuilder.append(this.end);
            stringBuilder.append("]");
            return stringBuilder.toString();
        }

        public Integer getStart() {
            return Integer.valueOf(this.start);
        }

        public Integer getEnd() {
            return Integer.valueOf(this.end);
        }

        public String getValue() {
            return this.value;
        }

        public String getListSlug() {
            return this.listSlug;
        }

        public Type getType() {
            return this.type;
        }

        public String getDisplayURL() {
            return this.displayURL;
        }

        public void setDisplayURL(String str) {
            this.displayURL = str;
        }

        public String getExpandedURL() {
            return this.expandedURL;
        }

        public void setExpandedURL(String str) {
            this.expandedURL = str;
        }
    }

    private static final class IndexConverter {
        protected int charIndex = 0;
        protected int codePointIndex = 0;
        protected final String text;

        IndexConverter(String str) {
            this.text = str;
        }

        /* Access modifiers changed, original: 0000 */
        public int codeUnitsToCodePoints(int i) {
            if (i < this.charIndex) {
                this.codePointIndex -= this.text.codePointCount(i, this.charIndex);
            } else {
                this.codePointIndex += this.text.codePointCount(this.charIndex, i);
            }
            this.charIndex = i;
            if (i > 0 && Character.isSupplementaryCodePoint(this.text.codePointAt(i - 1))) {
                this.charIndex--;
            }
            return this.codePointIndex;
        }

        /* Access modifiers changed, original: 0000 */
        public int codePointsToCodeUnits(int i) {
            this.charIndex = this.text.offsetByCodePoints(this.charIndex, i - this.codePointIndex);
            this.codePointIndex = i;
            return this.charIndex;
        }
    }

    private void removeOverlappingEntities(List<Entity> list) {
        Collections.sort(list, new Comparator<Entity>() {
            public int compare(Entity entity, Entity entity2) {
                return entity.start - entity2.start;
            }
        });
        if (!list.isEmpty()) {
            Iterator it = list.iterator();
            Entity entity = (Entity) it.next();
            while (it.hasNext()) {
                Entity entity2 = (Entity) it.next();
                if (entity.getEnd().intValue() > entity2.getStart().intValue()) {
                    it.remove();
                } else {
                    entity = entity2;
                }
            }
        }
    }

    public List<Entity> extractEntitiesWithIndices(String str) {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(extractURLsWithIndices(str));
        arrayList.addAll(extractHashtagsWithIndices(str, false));
        arrayList.addAll(extractMentionsOrListsWithIndices(str));
        arrayList.addAll(extractCashtagsWithIndices(str));
        removeOverlappingEntities(arrayList);
        return arrayList;
    }

    public List<String> extractMentionedScreennames(String str) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (Entity entity : extractMentionedScreennamesWithIndices(str)) {
            arrayList.add(entity.value);
        }
        return arrayList;
    }

    public List<Entity> extractMentionedScreennamesWithIndices(String str) {
        ArrayList arrayList = new ArrayList();
        for (Entity entity : extractMentionsOrListsWithIndices(str)) {
            if (entity.listSlug == null) {
                arrayList.add(entity);
            }
        }
        return arrayList;
    }

    public List<Entity> extractMentionsOrListsWithIndices(String str) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        Object obj = null;
        for (char c : str.toCharArray()) {
            if (c == '@' || c == 65312) {
                obj = 1;
                break;
            }
        }
        if (obj == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(str);
        while (matcher.find()) {
            if (!Regex.INVALID_MENTION_MATCH_END.matcher(str.substring(matcher.end())).find()) {
                if (matcher.group(4) == null) {
                    arrayList.add(new Entity(matcher, Type.MENTION, 3));
                } else {
                    arrayList.add(new Entity(matcher.start(3) - 1, matcher.end(4), matcher.group(3), matcher.group(4), Type.MENTION));
                }
            }
        }
        return arrayList;
    }

    public String extractReplyScreenname(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = Regex.VALID_REPLY.matcher(str);
        if (!matcher.find()) {
            return null;
        }
        if (Regex.INVALID_MENTION_MATCH_END.matcher(str.substring(matcher.end())).find()) {
            return null;
        }
        return matcher.group(1);
    }

    public List<String> extractURLs(String str) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (Entity entity : extractURLsWithIndices(str)) {
            arrayList.add(entity.value);
        }
        return arrayList;
    }

    public List<Entity> extractURLsWithIndices(String str) {
        if (!(str == null || str.length() == 0)) {
            int i;
            if (this.extractURLWithoutProtocol) {
                i = 46;
            } else {
                i = 58;
            }
            if (str.indexOf(i) != -1) {
                ArrayList arrayList = new ArrayList();
                Matcher matcher = Regex.VALID_URL.matcher(str);
                while (matcher.find()) {
                    if (matcher.group(4) == null) {
                        if (this.extractURLWithoutProtocol) {
                            if (Regex.INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN.matcher(matcher.group(2)).matches()) {
                            }
                        }
                    }
                    String group = matcher.group(3);
                    int start = matcher.start(3);
                    int end = matcher.end(3);
                    Matcher matcher2 = Regex.VALID_TCO_URL.matcher(group);
                    if (matcher2.find()) {
                        group = matcher2.group();
                        end = group.length() + start;
                    }
                    arrayList.add(new Entity(start, end, group, Type.URL));
                }
                return arrayList;
            }
        }
        return Collections.emptyList();
    }

    public List<String> extractHashtags(String str) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (Entity entity : extractHashtagsWithIndices(str)) {
            arrayList.add(entity.value);
        }
        return arrayList;
    }

    public List<Entity> extractHashtagsWithIndices(String str) {
        return extractHashtagsWithIndices(str, true);
    }

    private List<Entity> extractHashtagsWithIndices(String str, boolean z) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        Object obj = null;
        for (char c : str.toCharArray()) {
            if (c == '#' || c == 65283) {
                obj = 1;
                break;
            }
        }
        if (obj == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Regex.VALID_HASHTAG.matcher(str);
        while (matcher.find()) {
            if (!Regex.INVALID_HASHTAG_MATCH_END.matcher(str.substring(matcher.end())).find()) {
                arrayList.add(new Entity(matcher, Type.HASHTAG, 3));
            }
        }
        if (z) {
            List extractURLsWithIndices = extractURLsWithIndices(str);
            if (!extractURLsWithIndices.isEmpty()) {
                arrayList.addAll(extractURLsWithIndices);
                removeOverlappingEntities(arrayList);
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    if (((Entity) it.next()).getType() != Type.HASHTAG) {
                        it.remove();
                    }
                }
            }
        }
        return arrayList;
    }

    public List<String> extractCashtags(String str) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (Entity entity : extractCashtagsWithIndices(str)) {
            arrayList.add(entity.value);
        }
        return arrayList;
    }

    public List<Entity> extractCashtagsWithIndices(String str) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        if (str.indexOf(36) == -1) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Regex.VALID_CASHTAG.matcher(str);
        while (matcher.find()) {
            arrayList.add(new Entity(matcher, Type.CASHTAG, 3));
        }
        return arrayList;
    }

    public void setExtractURLWithoutProtocol(boolean z) {
        this.extractURLWithoutProtocol = z;
    }

    public boolean isExtractURLWithoutProtocol() {
        return this.extractURLWithoutProtocol;
    }

    public void modifyIndicesFromUnicodeToUTF16(String str, List<Entity> list) {
        IndexConverter indexConverter = new IndexConverter(str);
        for (Entity entity : list) {
            entity.start = indexConverter.codePointsToCodeUnits(entity.start);
            entity.end = indexConverter.codePointsToCodeUnits(entity.end);
        }
    }

    public void modifyIndicesFromUTF16ToToUnicode(String str, List<Entity> list) {
        IndexConverter indexConverter = new IndexConverter(str);
        for (Entity entity : list) {
            entity.start = indexConverter.codeUnitsToCodePoints(entity.start);
            entity.end = indexConverter.codeUnitsToCodePoints(entity.end);
        }
    }
}
