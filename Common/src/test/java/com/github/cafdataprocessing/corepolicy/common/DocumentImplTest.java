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
package com.github.cafdataprocessing.corepolicy.common;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getInputStream;
import static org.mockito.Mockito.*;

/**
 * Tests for the DocumentImpl class
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentImplTest {

    // Check that if we add a stream object
    @Test
    public void testAccessToStreamsCaseInsensitive(){

        String testString = "Place some random information into my stream.";

        InputStream is = getInputStream(testString);

        // Ensure the same property can be added and accessed case insensitive.
        Document doc = new DocumentImpl();
        doc.getStreams().put( "MyKey", is);

        // check we can get it back correctly.
        {
            InputStream anotherStream = doc.getStreams().get("MyKey").stream().findFirst().get();
            Assert.assertNotNull(anotherStream);
            Assert.assertEquals("Must return the same stream object, and not a copy", anotherStream.toString(), is.toString());
        }

        // Try again using a variation of - UPPERCASE.
        {
            InputStream anotherStream = doc.getStreams().get("MYKEY").stream().findFirst().get();
            Assert.assertNotNull(anotherStream);
            Assert.assertEquals("Must return the same stream object, and not a copy", anotherStream.toString(), is.toString());
        }

        // Try again using a variation of - lowercase.
        {
            InputStream anotherStream = doc.getStreams().get("mykey").stream().findFirst().get();
            Assert.assertNotNull(anotherStream);
            Assert.assertEquals("Must return the same stream object, and not a copy", anotherStream.toString(), is.toString());
        }

    }

    @Test
    public void testSupportsNullStream(){
        Document doc = new DocumentImpl();
        doc.getStreams().put( "MyKey", null);

        // check we can get it back correctly.
        Collection<InputStream> streams = doc.getStreams().get("MyKey");
        Assert.assertNotNull(streams);
        Assert.assertNull(streams.toArray()[0]);
    }

    @Test
    public void testStreamsSupportMultiValuePerKey(){

        String testString = "Place some random information into my stream.";
        String testString2 = "Place some random information into my stream.";
        InputStream is = getInputStream(testString);
        InputStream is2 = getInputStream(testString2);

        Document doc = new DocumentImpl();
        doc.getStreams().put( "MyKey", is);
        doc.getStreams().put( "MyKey", is2);

        // check we can get it back correctly.
        Collection<InputStream> streams = doc.getStreams().get("MyKey");

        Assert.assertNotNull(streams);
        Assert.assertEquals("Must be same number of streams as that input for the key", 2, streams.size());
    }


    @Test
    public void testStreamsAreNotAccessed() throws IOException {

        InputStream is = mock(InputStream.class);

        Document doc = new DocumentImpl();
        doc.getStreams().put( "MyKey", is);

        // check we can get it back correctly.
        Collection<InputStream> streams = doc.getStreams().get("MyKey");

        // verify that none of the read / write methods have been called on the mock!
        verify(is, times(0)).read();
        verify(is, times(0)).read(any());
        verify(is, times(0)).read(any(), anyInt(), anyInt());

        Assert.assertNotNull(streams);
        Assert.assertEquals("Must be same number of streams as that input for the key", 1, streams.size());
    }
}
