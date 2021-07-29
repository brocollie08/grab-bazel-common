/*
 * Copyright 2021 Grabtaxi Holdings PTE LTE (GRAB)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grab.databinding.stub.rclass.parser

import com.grab.databinding.stub.binding.parser.Binding
import com.grab.databinding.stub.binding.parser.BindingType
import com.grab.databinding.stub.binding.parser.LayoutBindingData
import com.grab.databinding.stub.common.BaseBindingStubTest
import com.grab.databinding.stub.rclass.parser.Type.*
import com.grab.databinding.stub.rclass.parser.RFieldEntry
import com.squareup.javapoet.ClassName
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import com.grab.databinding.stub.rclass.parser.ResToRParser
import com.grab.databinding.stub.rclass.parser.ResToRParserImpl
import com.grab.databinding.stub.rclass.parser.Type
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import com.grab.databinding.stub.rclass.parser.ParserType
import com.grab.databinding.stub.rclass.parser.xml.DeclareStyleableParser
import com.grab.databinding.stub.rclass.parser.xml.DefaultXmlParser

class ResToRStyleableValueParserTest : BaseBindingStubTest() {

    private lateinit var resToRParser: ResToRParser
    private val value = "0"

    @Before
    fun setUp() {
        val providedParsers = mapOf(ParserType.STYLEABLE_PARSER to DeclareStyleableParser(), 
            ParserType.DEFAULT_PARSER to DefaultXmlParser())
        resToRParser = ResToRParserImpl(providedParsers)
    }

    @Test
    fun `assert styleable values are parsed correctly`() {
        val listTemp = testResFiles(
                TestResFile("styleable.xml",
                        contents = """
                <resources>
                    <declare-styleable name="StyleableView">
                        <attr name="colorAttr" />
                        <attr name="sizeAttr" />
                    </declare-styleable>
                </resources>
                """.trimIndent(), path = "/src/res/values/")
        )

        val result = resToRParser.parse(listTemp, emptyList<String>()) as MutableMap<Type, MutableSet<RFieldEntry>>
        val parentValue = "{ 0,0 }"
        val exptectedStyleable = setOf(RFieldEntry(Type.STYLEABLE, "StyleableView", parentValue, isArray = true),
                RFieldEntry(Type.STYLEABLE, "StyleableView_colorAttr", value),
                RFieldEntry(Type.STYLEABLE, "StyleableView_sizeAttr", value))

        val exptectedAttrs = setOf(RFieldEntry(Type.ATTR, "colorAttr", value),
                RFieldEntry(Type.ATTR, "sizeAttr", value))        

        assertEquals(mapOf(Type.STYLEABLE to exptectedStyleable,
            Type.ATTR to exptectedAttrs), result)
    }

    @Test
    fun `assert styleable values are parsed correctly (dot will be replaced with underscore)`() {
        val listTemp = testResFiles(
                TestResFile("styleable.xml",
                        contents = """
                <resources>
                    <declare-styleable name="Base.StyleableView">
                        <attr name="colorAttr" />
                        <attr name="sizeAttr" />
                    </declare-styleable>
                </resources>
                """.trimIndent(), path = "/src/res/values/")
        )

        val result = resToRParser.parse(listTemp, emptyList<String>()) as MutableMap<Type, MutableSet<RFieldEntry>>
        val parentValue = "{ 0,0 }"
        val exptectedStyleable = setOf(RFieldEntry(Type.STYLEABLE, "Base_StyleableView", parentValue, isArray = true),
                RFieldEntry(Type.STYLEABLE, "Base_StyleableView_colorAttr", value),
                RFieldEntry(Type.STYLEABLE, "Base_StyleableView_sizeAttr", value))

        val exptectedAttrs = setOf(RFieldEntry(Type.ATTR, "colorAttr", value),
                RFieldEntry(Type.ATTR, "sizeAttr", value))

        assertEquals(mapOf(Type.STYLEABLE to exptectedStyleable,
                Type.ATTR to exptectedAttrs), result)
    }
}
