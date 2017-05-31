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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.MatcherWithTimeout;
import org.junit.Test;

import java.util.regex.Pattern;

public class MatcherWithTimeoutTest {

    @Test(expected = Exception.class)
    public void testFindthrows() throws Exception {

        String note = "在十几年前的中国，“海归”们在工作、薪水、社会地位等方面都是非常不错的。但是，近年来，随着留学人数的增多，\n" +
                "海归人数也不断的增加，昔年的风光已不复存在。在中国当今社会，甚至于对“海归”的批评声音多过褒奖。\n" +
                "很多留学回国的海归在中国国内找不到“存在感”，他们的身份让他们在中国职场处于尴尬的位置，\n" +
                "对于中国的社会环境也诸多不适\n";
        Pattern pattern = Pattern.compile("(?i)海归\\s*(\\S*\\s*){0,12}工作|工作\\s*(\\S*\\s*){0,12}海归");
        MatcherWithTimeout matcher = new MatcherWithTimeout(pattern.matcher(note), 1) ;
        while(matcher.find()){
            System.out.println(matcher.group());
        }
    }
}