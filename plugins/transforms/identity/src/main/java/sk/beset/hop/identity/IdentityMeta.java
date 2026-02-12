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

package sk.beset.hop.identity;

import org.apache.hop.core.annotations.Transform;
import org.apache.hop.pipeline.transform.BaseTransformMeta;

/** Meta data for the identity transform. */
@Transform(
    id = "IdentityId",
    name = "i18n::Identity.Name",
    description = "i18n::Identity.Description",
    image = "identity.svg",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Transform",
    keywords = "i18n::IdentityMeta.keyword",
    documentationUrl = "")
public class IdentityMeta extends BaseTransformMeta<Identity, IdentityData> {

  public IdentityMeta() {
    super();
  }
}
