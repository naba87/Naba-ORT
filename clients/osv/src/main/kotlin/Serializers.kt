/*
 * Copyright (C) 2022 EPAM Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.clients.osv

import java.time.Instant
import java.time.format.DateTimeFormatter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Use a custom serializer in order to map the original data structure to a more strict and simple, enum based, model.
 */
internal object EventSerializer : KSerializer<Event> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(javaClass.canonicalName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Event {
        require(decoder is JsonDecoder)

        val element = decoder.decodeJsonElement()
        require(element is JsonObject)

        require(element.entries.size == 1)
        val (key, value) = element.entries.first()
        val type = enumValueOf<Event.Type>(key.uppercase())

        return Event(type, value.jsonPrimitive.content)
    }

    override fun serialize(encoder: Encoder, value: Event) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("This class can be encoded only by Json format")

        val tree = JsonObject(mapOf(value.type.name.lowercase() to JsonPrimitive(value.value)))

        output.encodeJsonElement(tree)
    }
}

/**
 * Handle RFC3339 formatted strings with a 'Z' as suffix.
 */
internal object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(javaClass.canonicalName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(decoder.decodeString(), Instant::from)
    }
}
