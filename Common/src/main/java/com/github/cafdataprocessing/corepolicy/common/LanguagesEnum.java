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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum of languages.
 */
public enum LanguagesEnum {
    Afrikaans,
    Albanian,
    Amharic,
    Arabic,
    Armenian,
    Azeri,
    Basque,
    Belarussian,
    Bengali,
    Berber,
    Breton,
    Bulgarian,
    Burmese,
    Catalan,
    Cherokee,
    Chinese,
    Croatian,
    Czech,
    Danish,
    Dutch,
    English,
    Esperanto,
    Estonian,
    Faroese,
    Finnish,
    French,
    Gaelic,
    Georgian,
    German,
    Greek,
    Greenlandic,
    Gujarati,
    Hebrew,
    Hindi,
    Hungarian,
    Icelandic,
    Indonesian,
    Italian,
    Japanese,
    Kannada,
    Kazakh,
    Khmer,
    Korean,
    Kurdish,
    Lao,
    Latin,
    Latvian,
    Lithuanian,
    Luxembourgish,
    Macedonian,
    Malayalam,
    Maltese,
    Maori,
    Mongolian,
    Nepali,
    Norwegian,
    Oriya,
    Persian,
    Polish,
    Portuguese,
    Pushto,
    Romanian,
    Russian,
    Serbian,
    Sindhi,
    Singhalese,
    Slovak,
    Slovenian,
    Somali,
    Spanish,
    Swahili,
    Swedish,
    Syriac,
    Tagalog,
    Tajik,
    Tamil,
    Telugu,
    Thai,
    Tibetan,
    Turkish,
    Ukrainian,
    Urdu,
    Uyghur,
    Uzbek,
    Vietnamese,
    Welsh;

    @JsonCreator
    public static LanguagesEnum forValue(String value) {
        return LanguagesEnum.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
