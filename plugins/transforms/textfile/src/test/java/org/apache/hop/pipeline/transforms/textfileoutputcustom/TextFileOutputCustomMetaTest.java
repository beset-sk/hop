/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.textfileoutputcustom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.serializer.memory.MemoryMetadataProvider;
import org.apache.hop.metadata.serializer.xml.XmlMetadataUtil;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.junit.jupiter.api.Test;

class TextFileOutputCustomMetaTest {

  @Test
  void preservesAddUtf8BomWhenCloned() {
    TextFileOutputCustomMeta meta = new TextFileOutputCustomMeta();
    assertFalse(meta.isAddUtf8Bom());

    meta.setAddUtf8Bom(true);
    meta.setEnclosingTriggerHexCodes("0A,U+060C");

    TextFileOutputCustomMeta clone = (TextFileOutputCustomMeta) meta.clone();
    assertTrue(clone.isAddUtf8Bom());
    assertEquals("0A,U+060C", clone.getEnclosingTriggerHexCodes());
  }

  @Test
  void roundTripsAddUtf8BomThroughXml() throws Exception {
    TextFileOutputCustomMeta meta = new TextFileOutputCustomMeta();
    meta.setAddUtf8Bom(true);
    meta.setEnclosingTriggerHexCodes("0A,U+060C");

    String xml =
        XmlHandler.openTag(TransformMeta.XML_TAG)
            + XmlMetadataUtil.serializeObjectToXml(meta)
            + XmlHandler.closeTag(TransformMeta.XML_TAG);
    assertTrue(xml.contains("<add_utf8_bom>Y</add_utf8_bom>"));
    assertTrue(
        xml.contains("<enclosing_trigger_hex_codes>0A,U+060C</enclosing_trigger_hex_codes>"));

    TextFileOutputCustomMeta copy = new TextFileOutputCustomMeta();
    XmlMetadataUtil.deSerializeFromXml(
        XmlHandler.loadXmlString(xml, TransformMeta.XML_TAG),
        TextFileOutputCustomMeta.class,
        copy,
        new MemoryMetadataProvider());

    assertTrue(copy.isAddUtf8Bom());
    assertEquals("0A,U+060C", copy.getEnclosingTriggerHexCodes());
  }
}
