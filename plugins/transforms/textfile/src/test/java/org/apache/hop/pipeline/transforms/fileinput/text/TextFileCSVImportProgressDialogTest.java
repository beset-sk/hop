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

package org.apache.hop.pipeline.transforms.fileinput.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.apache.hop.core.IProgressMonitor;
import org.apache.hop.core.ProgressNullMonitorListener;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaDate;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.core.row.value.ValueMetaPlugin;
import org.apache.hop.core.row.value.ValueMetaPluginType;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transforms.csvinput.CsvInputField;
import org.apache.hop.pipeline.transforms.csvinput.CsvInputMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextFileCSVImportProgressDialogTest {

  @BeforeEach
  void beforeEach() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    String[] classNames = {
      ValueMetaString.class.getName(), ValueMetaInteger.class.getName(),
      ValueMetaDate.class.getName(), ValueMetaNumber.class.getName()
    };
    for (String className : classNames) {
      registry.registerPluginClass(className, ValueMetaPluginType.class, ValueMetaPlugin.class);
    }
  }

  @Test
  void doScanSetsFieldsToStringWhenFileContainsOnlyHeader() throws Exception {
    CsvInputMeta meta = new CsvInputMeta();
    meta.setEncoding(StandardCharsets.UTF_8.name());
    meta.setHeaderPresent(true);
    meta.getInputFields().add(numberField("first"));
    meta.getInputFields().add(numberField("second"));

    InputStreamReader reader =
        new InputStreamReader(
            new ByteArrayInputStream("first,second\n".getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8);
    TextFileCSVImportProgressDialog<CsvInputField> dialog =
        new TextFileCSVImportProgressDialog<>(
            null, new Variables(), meta, new PipelineMeta(), reader, 10, true);

    Method doScan =
        TextFileCSVImportProgressDialog.class.getDeclaredMethod(
            "doScan", IProgressMonitor.class, boolean.class);
    doScan.setAccessible(true);
    doScan.invoke(dialog, new ProgressNullMonitorListener(), false);

    assertEquals(IValueMeta.TYPE_STRING, meta.getInputFields().get(0).getType());
    assertEquals(IValueMeta.TYPE_STRING, meta.getInputFields().get(1).getType());
  }

  private CsvInputField numberField(String name) {
    CsvInputField field = new CsvInputField(name);
    field.setType(IValueMeta.TYPE_NUMBER);
    return field;
  }
}
