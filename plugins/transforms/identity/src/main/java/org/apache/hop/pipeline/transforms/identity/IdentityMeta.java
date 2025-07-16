package org.apache.hop.pipeline.transforms.identity;

import org.apache.hop.core.annotations.Transform;
import org.apache.hop.pipeline.transform.BaseTransformMeta;

@Transform(
    id = "Identity",
    image = "identity.svg",
    name = "i18n:org.apache.hop.pipeline.transforms.identity:Identity.Name",
    description = "i18n:org.apache.hop.pipeline.transforms.identity:Identity.Description",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Transform",
    keywords = "i18n::IdentityMeta.keyword",
    documentationUrl = "/pipeline/transforms/identity.html")
public class IdentityMeta extends BaseTransformMeta<Identity, IdentityData> {

  private static final Class<?> PKG = IdentityMeta.class;

  public IdentityMeta() {
    super();
  }
}
