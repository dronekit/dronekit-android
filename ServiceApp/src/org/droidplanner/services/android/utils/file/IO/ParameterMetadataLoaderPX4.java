package org.droidplanner.services.android.utils.file.IO;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import org.droidplanner.services.android.core.drone.profiles.ParameterMetadataPX4;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by KiBa on 2015/12/24.
 */
public class ParameterMetadataLoaderPX4 {

    private static final String PARAMETERMETADATA_PATH = "Parameters/ParameterMetaData.xml";

    //**********************PX4 metadata************************
    private static final String METADATA_GROUP_PX4 = "group";
    private static final String METADATA_PARAMETER_PX4 = "parameter";

    private static final String METADATA_DISPLAYNAME_PX4 = "short_desc";
    private static final String METADATA_DESCRIPTION_PX4 = "long_desc";
    private static final String METADATA_UNITS_PX4 = "unit";
    private static final String METADATA_RANGE_MAX_PX4 = "max";
    private static final String METADATA_RANGE_MIN_PX4 = "min";
    private static final String METADATA_DECIMAL_PX4 = "decimal";

    private static final String METADATA_TYPE_PX4_FLOAT = "FLOAT";
    private static final String METADATA_TYPE_PX4_INT32 = "INT32";
    private static List<Integer> minListInt32 = new ArrayList<>();
    private static List<Float> minListFloat = new ArrayList<>();

    public static void load(Context context, String metadataType, Map<String, ParameterMetadataPX4> metadata)
            throws IOException, XmlPullParserException {
        InputStream inputStream = context.getAssets().open(PARAMETERMETADATA_PATH);
        open(inputStream, metadataType, metadata);
    }

    private static void open(InputStream inputStream, String metadataType, Map<String, ParameterMetadataPX4> metadataMap)
            throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parseMetadataPX4(parser, metadataType, metadataMap);

        } finally {
            try {
                inputStream.close();
            } catch (IOException e) { /* nop */
            }
        }
    }

    //**************************** PX4 *********************************

    private static void parseMetadataPX4(XmlPullParser pullParser, String metadataType,
                                         Map<String, ParameterMetadataPX4> metadataMap) throws XmlPullParserException, IOException {
        int type = pullParser.getEventType();

        ParameterMetadataPX4 metadataPX4 = null;
        String name = null;         // label name
        String groupName = null;    // group--'name' attribute
        String defaultAttr = null;  // parameter--'default' attribute
        String nameAttr = null;     // parameter--'name' attribute
        String typeAttr = null;     // parameter--'type' attribute

        boolean parsing = false;

        while (type != XmlPullParser.END_DOCUMENT) {
            switch (type) {
                case XmlPullParser.START_TAG:
                    name = pullParser.getName(); // label name

                    if (TextUtils.equals(metadataType, name)) {
                        parsing = true;
                    } else if (parsing) {
                        // if the label name is <group>
                        if (TextUtils.equals(pullParser.getName(), METADATA_GROUP_PX4)) {
                            groupName = pullParser.getAttributeValue(0);
                        } else if (TextUtils.equals(pullParser.getName(), METADATA_PARAMETER_PX4)) { // if label name isn't 'group'
                            metadataPX4 = new ParameterMetadataPX4();
                            // get attributes of the <parameter> label
                            if (groupName != null) {
                                metadataPX4.setGroupName(groupName); // group value
                            }
                            defaultAttr = pullParser.getAttributeValue(0); // default value
                            nameAttr = pullParser.getAttributeValue(1);    // name value
                            typeAttr = pullParser.getAttributeValue(2);    // type value
                            // set attributes to ParameterMetadataPX4
                            metadataPX4.setDefaultAttr(defaultAttr);
                            metadataPX4.setName(nameAttr);
                            metadataPX4.setTypeAttr(typeAttr);

                        } else {
                            if (metadataPX4 != null) {
                                addMetaDataPropertyPX4(metadataPX4, name, pullParser.nextText());
                            }
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (TextUtils.equals(metadataType, name)) {
                        return;
                    } else if (metadataPX4 != null && metadataPX4.getName().equals(nameAttr)) {
                        metadataMap.put(metadataPX4.getName(), metadataPX4);
                        metadataPX4 = null;
                    }
                    break;
                default:
                    break;
            }

            type = pullParser.next();
        }
    }


    private static void addMetaDataPropertyPX4(ParameterMetadataPX4 metaData, String labelName, String text) {
        switch (labelName) {
            case METADATA_DISPLAYNAME_PX4:
                metaData.setDisplayName(text);
                break;
            case METADATA_DESCRIPTION_PX4:
                metaData.setDescription(text);
                break;
            case METADATA_UNITS_PX4:
                metaData.setUnits(text);
                break;
            case METADATA_DECIMAL_PX4:
                metaData.setDecimal(text);
                break;
            case METADATA_RANGE_MIN_PX4:
                handleMinRange(metaData, text);
                break;
            case METADATA_RANGE_MAX_PX4:
                handleMaxRange(metaData, text);
                break;

        }
    }

    private static void handleMinRange(ParameterMetadataPX4 metadataPX4, String text) {
        // if the type is int32, it indicates that it can be converted to 'values'
        if (TextUtils.equals(metadataPX4.getTypeAttr(), METADATA_TYPE_PX4_INT32)) {
            if (minListInt32.isEmpty()) {
                minListInt32.add(0, Integer.parseInt(text));
            }
        }
        // if the type is float, it indicates that it only can be converted to 'range'
        else if (TextUtils.equals(metadataPX4.getTypeAttr(), METADATA_TYPE_PX4_FLOAT)) {
            if (minListFloat.isEmpty()) {
                minListFloat.add(0, Float.parseFloat(text));
            }
        }
        metadataPX4.setMin(text);
    }

    private static void handleMaxRange(ParameterMetadataPX4 metadataPX4, String text) {
        // set max value
        metadataPX4.setMax(text);
        // if the type is int32, it indicates that it can be converted to 'values'
        if (TextUtils.equals(metadataPX4.getTypeAttr(), METADATA_TYPE_PX4_INT32)) {
            if (!minListInt32.isEmpty()) {
                int min = minListInt32.get(0);
                int max = Integer.parseInt(text);
                // if the diff is between 2, that can be chose, else fill in manually
                if (min == 0 || min == -1) {
                    if (max - min <= 2) {
                        String value = "";
                        for (int i = min; i <= max; i++) {
                            if (i != max) {
                                value = value + i + ": ,";
                            } else {
                                value = value + i + ": ";
                            }
                        }
                        metadataPX4.setValues(value);
                    }
                } else {
                    String range = min + " " + max;
                    metadataPX4.setRange(range);
                }
                minListInt32.clear();
            }
        }
        // if the type is float, it indicates that it only can be converted to 'range'
        else if (TextUtils.equals(metadataPX4.getTypeAttr(), METADATA_TYPE_PX4_FLOAT)) {
            if (!minListFloat.isEmpty()) {
                float min = minListFloat.get(0);
                String range = min + " " + text;
                metadataPX4.setRange(range);
            }
            minListFloat.clear();
        }
    }
}