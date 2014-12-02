package org.droidplanner.services.android.utils.file.IO;

import android.content.Context;
import android.util.Xml;

import com.o3dr.services.android.lib.drone.property.Parameter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by fhuya on 10/29/14.
 */
public class ParameterMetadataLoader {

	private static final String PARAMETERMETADATA_PATH = "Parameters/ParameterMetaData.xml";

	private static final String METADATA_DISPLAYNAME = "DisplayName";
	private static final String METADATA_DESCRIPTION = "Description";
	private static final String METADATA_UNITS = "Units";
	private static final String METADATA_VALUES = "Values";
	private static final String METADATA_RANGE = "Range";

	public static void load(Context context, String metadataType, Map<String, Parameter> parameters)
			throws IOException, XmlPullParserException {
		final InputStream inputStream = context.getAssets().open(PARAMETERMETADATA_PATH);
		open(inputStream, metadataType, parameters);
	}

	private static void open(InputStream inputStream, String metadataType, Map<String, Parameter>
                             parameters) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(inputStream, null);
			parseMetadata(parser, metadataType, parameters);

		} finally {
			try {
				inputStream.close();
			} catch (IOException e) { /* nop */
			}
		}
	}

	private static void parseMetadata(XmlPullParser parser, String metadataType,
			Map<String, Parameter> parameters) throws XmlPullParserException, IOException {

		boolean parsing = false;
        Parameter parameter = null;

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG: {
				String name = parser.getName();
				// name == metadataType: start updating parameters' metadata(s)
				if (metadataType.equals(name)) {
					parsing = true;
				} else if (parsing) {
                    if(parameter == null) {
                        parameter = parameters.get(name);
                    }
					else {
                        addMetaDataProperty(parameter, name, parser.nextText());
                    }
				}
				break;
			}

			case XmlPullParser.END_TAG: {
				String name = parser.getName();
				// name == metadataType: done
				if (metadataType.equals(name)) {
					return;
				} else if(parameter != null){
                    parameter = null;
                }
				break;
			}
			}
			eventType = parser.next();
		}

		// no metadata
	}

	private static void addMetaDataProperty(Parameter parameter, String name, String text) {
		if (name.equals(METADATA_DISPLAYNAME))
			parameter.setDisplayName(text);
		else if (name.equals(METADATA_DESCRIPTION))
			parameter.setDescription(text);

		else if (name.equals(METADATA_UNITS))
			parameter.setUnits(text);
		else if (name.equals(METADATA_RANGE))
			parameter.setRange(text);
		else if (name.equals(METADATA_VALUES))
			parameter.setValues(text);
	}
}
