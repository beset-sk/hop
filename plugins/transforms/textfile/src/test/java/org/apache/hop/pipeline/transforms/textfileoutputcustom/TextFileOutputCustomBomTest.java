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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.hop.core.Const;
import org.apache.hop.core.compress.CompressionPluginType;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.pipeline.transforms.mock.TransformMockHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class TextFileOutputCustomBomTest {
  private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

  @TempDir Path tempDir;

  private TransformMockHelper<TextFileOutputCustomMeta, TextFileOutputCustomData> helper;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType(CompressionPluginType.getInstance());
    PluginRegistry.init();
  }

  @BeforeEach
  void setUp() {
    helper =
        new TransformMockHelper<>(
            "TEXT FILE OUTPUT CUSTOM BOM TEST",
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
  void writesBomToANewUtf8File() throws Exception {
    Path file = tempDir.resolve("new.txt");
    TextFileOutputCustomMeta meta = newBomMeta(false);
    TextFileOutputCustomData data = new TextFileOutputCustomData();
    TextFileOutputCustom transform = newTransform(meta, data);

    transform.initFileStreamWriter(file.toString());
    data.writer.flush();
    data.getFileStreamsCollection().closeFile(file.toString());

    assertArrayEquals(UTF8_BOM, Files.readAllBytes(file));
  }

  @Test
  void appendsToBomOnlyFileWithoutDuplicatingBomAndStillWritesHeader() throws Exception {
    Path file = tempDir.resolve("bom-only.txt");
    Files.write(file, UTF8_BOM);
    TextFileOutputCustomMeta meta = newBomMeta(true);
    TextFileOutputCustomData data = new TextFileOutputCustomData();
    TextFileOutputCustom transform = newTransform(meta, data);

    assertTrue(transform.isWriteHeader(file.toString()));
    transform.initFileStreamWriter(file.toString());
    data.writer.flush();
    data.getFileStreamsCollection().closeFile(file.toString());

    assertArrayEquals(UTF8_BOM, Files.readAllBytes(file));
  }

  private TextFileOutputCustomMeta newBomMeta(boolean append) {
    TextFileOutputCustomMeta meta = new TextFileOutputCustomMeta();
    meta.setEncoding(Const.UTF_8);
    meta.setAddUtf8Bom(true);
    meta.setHeaderEnabled(true);
    meta.setFileCompression("None");
    meta.getFileSettings().setFileAppended(append);
    return meta;
  }

  private TextFileOutputCustom newTransform(
      TextFileOutputCustomMeta meta, TextFileOutputCustomData data) {
    return new TextFileOutputCustom(
        helper.transformMeta, meta, data, 0, helper.pipelineMeta, helper.pipeline);
  }
}
