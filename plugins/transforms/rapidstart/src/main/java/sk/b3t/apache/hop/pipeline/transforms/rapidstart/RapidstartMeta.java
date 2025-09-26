package sk.b3t.apache.hop.pipeline.transforms.rapidstart;

import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

@Transform(
    id = "Rapidstart",
    name = "i18n::Rapidstart.Name",
    description = "i18n::Rapidstart.Description",
    image = "rapidstart.svg",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Output",
    documentationUrl = "/pipeline/transforms/rapidstart.html",
    keywords = "package,code,rapid,start")
public class RapidstartMeta extends BaseTransformMeta<Rapidstart, RapidstartData> {

  private static final Class<?> PKG = RapidstartMeta.class;

  // POZOR: "package" je Java kľúčové slovo, preto field nazvime packageName,
  // ale sérializačný kľúč v XML nech je "package".
  @HopMetadataProperty(
      key = "package",
      injectionKeyDescription = "PackageCodeMeta.Injection.Package")
  private String packageName;

  @HopMetadataProperty(key = "code", injectionKeyDescription = "PackageCodeMeta.Injection.Code")
  private String code;

  public RapidstartMeta() {
    setDefault();
  }

  public RapidstartMeta(RapidstartMeta m) {
    this.packageName = m.packageName;
    this.code = m.code;
  }

  @Override
  public RapidstartMeta clone() {
    return new RapidstartMeta(this);
  }

  @Override
  public void setDefault() {
    this.packageName = "";
    this.code = "";
  }

  @Override
  public void getFields(
      IRowMeta rowMeta,
      String name,
      IRowMeta[] info,
      TransformMeta nextTransform,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    // passthrough – nemeníme schému. Ak by si chcel pridať nové polia, urob to tu cez
    // rowMeta.addValueMeta(...)
  }

  @Override
  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transformMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {

    // 1..n vstupov je OK (žiadna chyba, keď je input.length >= 1; ak nie je pripojený žiadny vstup,
    // upozorníme)
    if (input == null || input.length == 0) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING,
              BaseMessages.getString(PKG, "PackageCodeMeta.CheckResult.NoInput"),
              transformMeta));
    } else {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(PKG, "PackageCodeMeta.CheckResult.AcceptsInput"),
              transformMeta));
    }

    // Validácia povinných parametrov podľa potreby
    // napr. ak Code musí byť vyplnený:
    // if (code == null || code.isEmpty()) { ... }
  }

  @Override
  public String getDialogClassName() {
    return RapidstartDialog.class.getName();
  }

  // --- getters/setters ---

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
