/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.corepolicy.common.shared;

import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.multimap.utils.CaseInsensitiveMultimap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal Object which represents metadata in whatever form it may take e.g. String or Stream.
 */
public class MetadataValue implements Comparable {

    private InputStream streamValue;
    private String stringValue;
    private boolean hasStream;
    private boolean haveCachedStream;
    private ApiProperties apiProperties;

    protected MetadataValue( ApiProperties apiProperties )
    {
        this.apiProperties = apiProperties;
        this.hasStream = false;
        this.haveCachedStream = false;
    }

    public MetadataValue( ApiProperties apiProperties, InputStream streamValue )
    {
        this(apiProperties);
        this.streamValue = streamValue;
        this.hasStream = true;
    }

    public MetadataValue( ApiProperties apiProperties, String stringValue)
    {
        this(apiProperties);
        this.stringValue = stringValue;
    }

    public InputStream getStreamValue() {
        if (streamValue != null)
        {
            try {
                streamValue.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return streamValue;
    }

    public void setStreamValue(InputStream streamValue) {
        this.streamValue = streamValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isHasStream() {
        return hasStream;
    }

    public void setHasStream(boolean hasStream) {
        this.hasStream = hasStream;
    }

    static private final int LEFT_IS_GREATER = 1;
    static private final int RIGHT_IS_GREATER = -1;

    public int compareTo(Object objB) {

        // we can just do our null first implementation, so we dont need to check for null later.
        if (this == null && objB == null) {
            return 0;
        }
        if (this == null) {
            return RIGHT_IS_GREATER;
        }
        if (objB == null) {
            return LEFT_IS_GREATER;
        }

        // if they are both metadata values, then try to see if we can use strings for comparison.
        if ( this instanceof MetadataValue && objB instanceof MetadataValue ) {
            // check if both of same type. if not put strings, first regardless!
            if (!this.hasStream && !((MetadataValue)objB).hasStream) {
                return this.getStringValue().compareTo(((MetadataValue)objB).getStringValue());
            }

            // compare using the actual stream comparator if they have any specified.
            // If its our own wrapped stream it can compare using doc references.
            if (this.getStreamValue() instanceof Comparator && ((MetadataValue) objB).getStreamValue() instanceof Comparator) {
                return ((Comparator) this).compare(this.getStreamValue(), ((MetadataValue) objB).getStreamValue());
            }
        }

        // order based on the class name / ident of the stream,
        // it should remain consistent at least.
        return this.toString().compareTo(objB.toString());
    }

    /**
     * Regardless of the format of the item, force the information is returned as string!
     *
     * @return
     */
    public String getAsString() {
        //
        // This should only be used on objects which are not yet stream aware
        if (!hasStream) {
            // otherwise its just a string - return it.
            return getStringValue();
        }

        // the stream needs to be turned into a string.  Only possibly used when converting
        // to interfaces which only contain strings, such as setTerms or the like.
        // N.B. now adding a configurable cache of the stream, which if converted to a string
        // once we hold onto this for the duration of the evaluation.
        if (!apiProperties.getStreamsCacheEnabled()) {
            return asString(getStreamValue());
        }

        if (haveCachedStream) {
            return getStringValue();
        }

        // otherwise we have cache enabled, but haven't yet converted it to a string.
        // get the string from the stream, and indicate we have done this.
        String strValue = asString(getStreamValue());
        setStringValue(strValue);
        haveCachedStream = true;
        return strValue;
    }

    /**
     * Utility Methods
     */


    /**
     * Utility method to create a list of metadatavalues from a list of strings.
     * @param values
     * @return
     */
    public static Collection<MetadataValue> getMetadataValues(Collection<String> values, ApiProperties apiProperties) {

        Validate.notNull(apiProperties);
        return values.stream().map(value->new MetadataValue(apiProperties, value)).collect(Collectors.toList());
    }

    /**
     * Utility method to create a list of metadatavalues from a list of streams.
     * @param values
     * @return
     */
    public static Collection<MetadataValue> getMetadataValuesFromStreams(Collection<InputStream> values, ApiProperties apiProperties) {

        Validate.notNull(apiProperties);
        return values.stream().map(value -> new MetadataValue(apiProperties, value)).collect(Collectors.toList());
    }

    /**
     * Utility method to convert a map of metadatavalue objects, into a map of strings.
     * @param existingMap
     * @return
     */
    public static Multimap<String, String> convertToStringMap( Multimap< String, MetadataValue > existingMap ){

        Multimap<String, String> newMap = new CaseInsensitiveMultimap<>();

        for( Map.Entry<String, MetadataValue> entry : existingMap.entries())
        {
            newMap.put( entry.getKey(), entry.getValue().getAsString() );
        }

        return newMap;
    }

    /**
     * Utility method to convert a map of string objects, into a map of metadata objects.
     * @param existingMap
     * @return
     */
    public static Multimap<String, MetadataValue> convertToMetadataMap( Multimap< String, String > existingMap, ApiProperties apiProperties ){

        Multimap<String, MetadataValue> newMap = new CaseInsensitiveMultimap<>();

        for( Map.Entry<String, String> entry : existingMap.entries())
        {
            newMap.put( entry.getKey(), new MetadataValue( apiProperties, entry.getValue() ));
        }

        return newMap;
    }

    /**
     * Utility method to create a list of strings from a list of metadatavalue objects.
     * @param values
     * @return
     */
    public static Collection<String> getStringValues(Collection<MetadataValue> values) {
        return values.stream().map(MetadataValue::getAsString).collect(Collectors.toList());
    }

    protected static String asString(InputStream stream)
    {
        try {
            // Anyone that may want the inputstream as a string, please reset the whole stream, and read from the start.
            stream.reset();
            return IOUtils.toString(stream, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert stream to a string.", e);
        }
    }

    public ApiProperties getApiProperties() {
        return apiProperties;
    }

    void setApiProperties(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }
}
