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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.pipeline.transforms.mock.TransformMockHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TextFileOutputCustomEnclosureTriggerTest {
  private TransformMockHelper<TextFileOutputCustomMeta, TextFileOutputCustomData> helper;

  @BeforeEach
  void setUp() {
    helper =
        new TransformMockHelper<>(
            "TEXT FILE OUTPUT CUSTOM ENCLOSURE TRIGGER TEST",
            TextFileOutputCustomMeta.class,
            TextFileOutputCustomData.class);
    Mockito.when(helper.logChannelFactory.create(Mockito.any(), Mockito.any(ILoggingObject.class)))
        .thenReturn(helper.iLogChannel);
  }

  @AfterEach
  void tearDown() throws Exception {
    helper.cleanUp();
  }

  @Test
  void parsesByteAndUnicodeFormatsUsedByThe216Transform() throws Exception {
    TextFileOutputCustomMeta meta = new TextFileOutputCustomMeta();
    meta.setEncoding(Const.UTF_8);
    meta.setEnclosingTriggerHexCodes("0A; 0x0D 09,U+060C");
    TextFileOutputCustomData data = new TextFileOutputCustomData();
    data.hasEncoding = true;
    TextFileOutputCustom transform = newTransform(meta, data);

    byte[][] triggers = transform.parseEnclosureTriggerCodes();

    assertArrayEquals(new byte[] {0x0A}, triggers[0]);
    assertArrayEquals(new byte[] {0x0D}, triggers[1]);
    assertArrayEquals(new byte[] {0x09}, triggers[2]);
    assertArrayEquals("،".getBytes(StandardCharsets.UTF_8), triggers[3]);
  }

  @Test
  void matchesAUnicodeTriggerAsAnExactByteSequence() throws Exception {
    TextFileOutputCustomMeta meta = new TextFileOutputCustomMeta();
    meta.setEncoding(Const.UTF_8);
    meta.setEnclosingTriggerHexCodes("U+060C");
    TextFileOutputCustomData data = new TextFileOutputCustomData();
    data.hasEncoding = true;
    TextFileOutputCustom transform = newTransform(meta, data);
    byte[][] triggers = transform.parseEnclosureTriggerCodes();

    assertTrue(
        transform.containsSeparatorOrEnclosureOrConfiguredChars(
            "ABC،DEF".getBytes(StandardCharsets.UTF_8), new byte[0], new byte[0], triggers));
    assertFalse(
        transform.containsSeparatorOrEnclosureOrConfiguredChars(
            "ABCبDEF".getBytes(StandardCharsets.UTF_8), new byte[0], new byte[0], triggers));
  }

  @Test
  void rejectsInvalidTriggerCodes() {
    TextFileOutputCustomMeta meta = new TextFileOutputCustomMeta();
    meta.setEncoding(Const.UTF_8);
    meta.setEnclosingTriggerHexCodes("100,U+XYZ");
    TextFileOutputCustom transform = newTransform(meta, new TextFileOutputCustomData());

    assertThrows(HopException.class, transform::parseEnclosureTriggerCodes);
  }

  private TextFileOutputCustom newTransform(
      TextFileOutputCustomMeta meta, TextFileOutputCustomData data) {
    return new TextFileOutputCustom(
        helper.transformMeta, meta, data, 0, helper.pipelineMeta, helper.pipeline);
  }
}
