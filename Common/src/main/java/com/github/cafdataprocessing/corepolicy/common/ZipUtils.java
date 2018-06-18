/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
package com.github.cafdataprocessing.corepolicy.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility functions relating to data compression
 */
public class ZipUtils {

    public static byte[] compress(String string) throws IOException {

        try(ByteArrayOutputStream os = new ByteArrayOutputStream(string.length()) ) {
            try(  GZIPOutputStream gos = new GZIPOutputStream(os)) {
                gos.write(string.getBytes());
                gos.finish();
            }

            byte[] compressed = os.toByteArray();
            return compressed;
        }

    }

    public static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;

        try( ByteArrayInputStream is = new ByteArrayInputStream(compressed))
        {
            try( GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE) ) {
                StringBuilder string = new StringBuilder();
                byte[] data = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = gis.read(data)) != -1) {
                    string.append(new String(data, 0, bytesRead));
                }

                return string.toString();
            }
        }
    }

    public static String compressStringAndEncode(String str) throws IOException{
        if (str == null || str.length() == 0) {
            return str;
        }

        byte [] bytes = compress( str );
        return new String(Base64.getEncoder().encode( bytes ));
    }

    public static String decompressEncodedString(String str) throws IOException{
        if (str == null || str.length() == 0) {
            return str;
        }

        // Base64 decode string into byte array again.
        byte[] bytes = Base64.getDecoder().decode( str );
        return decompress( bytes );
    }
}
