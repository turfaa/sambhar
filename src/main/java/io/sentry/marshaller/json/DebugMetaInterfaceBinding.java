package io.sentry.marshaller.json;

import com.fasterxml.jackson.core.JsonGenerator;
import io.sentry.event.interfaces.DebugMetaInterface;
import io.sentry.event.interfaces.DebugMetaInterface.DebugImage;
import java.io.IOException;
import java.util.Iterator;

public class DebugMetaInterfaceBinding implements InterfaceBinding<DebugMetaInterface> {
    private static final String IMAGES = "images";
    private static final String TYPE = "type";
    private static final String UUID = "uuid";

    public void writeInterface(JsonGenerator jsonGenerator, DebugMetaInterface debugMetaInterface) throws IOException {
        jsonGenerator.writeStartObject();
        writeDebugImages(jsonGenerator, debugMetaInterface);
        jsonGenerator.writeEndObject();
    }

    private void writeDebugImages(JsonGenerator jsonGenerator, DebugMetaInterface debugMetaInterface) throws IOException {
        jsonGenerator.writeArrayFieldStart(IMAGES);
        Iterator it = debugMetaInterface.getDebugImages().iterator();
        while (it.hasNext()) {
            DebugImage debugImage = (DebugImage) it.next();
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(UUID, debugImage.getUuid());
            jsonGenerator.writeStringField("type", debugImage.getType());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }
}
