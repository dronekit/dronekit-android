package org.droidplanner.services.android.impl.utils.file.IO;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Xml;

import org.droidplanner.services.android.impl.core.drone.profiles.ParameterMetadata;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by fhuya on 10/29/14.
 */
public class ParameterMetadataLoader {
    private static final String TAG = ParameterMetadataLoader.class.getSimpleName();

    private static final String PARAMS_ASSET_PATH = "Parameters";
    private static final String PARAMETERMETADATA_PATH = "Parameters/ParameterMetaData.xml";

    private static final String METADATA_DISPLAYNAME = "DisplayName";
    private static final String METADATA_DESCRIPTION = "Description";
    private static final String METADATA_UNITS = "Units";
    private static final String METADATA_VALUES = "Values";
    private static final String METADATA_RANGE = "Range";

    public static void load(Context context, String metadataType, Map<String, ParameterMetadata> metadata)
            throws IOException, XmlPullParserException {
        final AssetManager assMan = context.getAssets();
        final String[] files = assMan.list(PARAMS_ASSET_PATH);

        if(files != null) {
            for(String file: files) {
                final String path = String.format("%s/%s", PARAMS_ASSET_PATH, file);
                Timber.d("Load metadata from %s for type %s", path, metadataType);
                final InputStream input = assMan.open(path);

                final Map<String, ParameterMetadata> map = new HashMap<>();
                open(input, metadataType, map);
                Timber.d("Read %d params", map.size());

                int added = 0;
                for(String k: map.keySet()) {
                    if(!metadata.containsKey(k)) {
                        metadata.put(k, map.get(k));
                        ++added;
                    }
                }

                Timber.d("Added %d params to map", added);
            }
        }
    }

    private static void open(InputStream inputStream, String metadataType, Map<String, ParameterMetadata> metadataMap)
            throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parseMetadata(parser, metadataType, metadataMap);

        } finally {
            try {
                inputStream.close();
            } catch (IOException e) { /* nop */
            }
        }
    }

    private static void parseMetadata(XmlPullParser parser, String metadataType, Map<String, ParameterMetadata> metadataMap)
            throws XmlPullParserException, IOException {
        String name;
        boolean parsing = false;
        ParameterMetadata metadata = null;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    // name == metadataType: start collecting metadata(s)
                    // metadata == null: create new metadata w/ name
                    // metadata != null: add to metadata as property
                    if (metadataType.equals(name)) {
                        parsing = true;
                    } else if (parsing) {
                        if (metadata == null) {
                            metadata = new ParameterMetadata();
                            metadata.setName(name);
                        } else {
                            addMetaDataProperty(metadata, name, parser.nextText());
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    // name == metadataType: done
                    // name == metadata.name: add metadata to metadataMap
                    if (metadataType.equals(name)) {
                        return;
                    } else if (metadata != null && metadata.getName().equals(name)) {
                        metadataMap.put(metadata.getName(), metadata);
                        metadata = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        // no metadata
    }

    private static void addMetaDataProperty(ParameterMetadata metaData, String name, String text) {
        switch (name) {
            case METADATA_DISPLAYNAME:
                metaData.setDisplayName(text);
                break;
            case METADATA_DESCRIPTION:
                metaData.setDescription(text);
                break;
            case METADATA_UNITS:
                metaData.setUnits(text);
                break;
            case METADATA_RANGE:
                metaData.setRange(text);
                break;
            case METADATA_VALUES:
                metaData.setValues(text);
                break;
        }
    }
}
