package sk.b3t.apache.hop.pipeline.transforms.rapidstart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.encryption.Encr;
import org.apache.hop.core.encryption.HopTwoWayPasswordEncoder;
import org.apache.hop.core.encryption.ITwoWayPasswordEncoder;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.serializer.json.JsonMetadataProvider;
import org.apache.hop.pipeline.PipelineHopMeta;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.engine.PipelineEngineFactory;
import org.apache.hop.pipeline.engines.local.LocalPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.RowAdapter;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transforms.dummy.DummyMeta;
import org.apache.hop.pipeline.transforms.rowgenerator.GeneratorField;
import org.apache.hop.pipeline.transforms.rowgenerator.RowGeneratorMeta;
import org.junit.jupiter.api.Test;

class UT_Rapidstart {

  @Test
  void test() throws Exception {
    // 1) Init HOP + plugin scanning (HOP_PLUGIN_BASE_FOLDERS môžeš nastaviť cez -D...):
    HopEnvironment.init(); // načíta config a pluginy  // ← :contentReference[oaicite:5]{index=5}
    IVariables vars =
        Variables.getADefaultVariableSpace(); // zoberie aj premenné z hop-config.json  // ←
    // :contentReference[oaicite:6]{index=6}

    // 2) Postavíme pipeline v pamäti:
    PipelineMeta pipelineMeta = new PipelineMeta();
    pipelineMeta.setName("ForUT");

    // -- Injector (vstupné pole 'in' typu String)
    // InjectorMeta injectorMeta = new InjectorMeta();
    // TransformMeta inj = new TransformMeta("Injector", injectorMeta);
    // pipelineMeta.addTransform(inj);

    RowGeneratorMeta rgMeta = new RowGeneratorMeta();
    rgMeta.setRowLimit("10");
    rgMeta.setIntervalInMs("500");
    IValueMeta valueMeta = ValueMetaFactory.createValueMeta("valueA", IValueMeta.TYPE_STRING);
    IValueMeta valueMetaInt = ValueMetaFactory.createValueMeta("rowNo", IValueMeta.TYPE_INTEGER);
    GeneratorField rowNo = new GeneratorField();
    rowNo.setName(valueMeta.getName());
    rowNo.setType(valueMeta.getTypeDesc());
    rowNo.setLength(valueMeta.getLength());
    rowNo.setPrecision(valueMeta.getPrecision());
    rowNo.setCurrency(valueMeta.getCurrencySymbol());
    rowNo.setDecimal(valueMeta.getDecimalSymbol());
    rowNo.setGroup(valueMeta.getGroupingSymbol());

    GeneratorField field = new GeneratorField();
    field.setName(valueMetaInt.getName());
    field.setType(valueMetaInt.getTypeDesc());
    field.setLength(valueMetaInt.getLength());
    field.setPrecision(valueMetaInt.getPrecision());
    field.setCurrency(valueMetaInt.getCurrencySymbol());
    field.setDecimal(valueMetaInt.getDecimalSymbol());
    field.setGroup(valueMetaInt.getGroupingSymbol());

    rgMeta.getFields().add(rowNo);
    rgMeta.getFields().add(field);

    TransformMeta rowgwen = new TransformMeta("Generate A", rgMeta);
    pipelineMeta.addTransform(rowgwen);

    TransformMeta rowgwenB = new TransformMeta("Generate B", rgMeta);
    pipelineMeta.addTransform(rowgwenB);

    // -- Tvoja meta s 2 parametrami
    RapidstartMeta pkgMeta = new RapidstartMeta();
    pkgMeta.setPackageName("demo.pkg");
    pkgMeta.setCode("X1");
    TransformMeta pkg = new TransformMeta("PackageCode", pkgMeta);
    pipelineMeta.addTransform(pkg);
    pipelineMeta.addPipelineHop(new PipelineHopMeta(rowgwen, pkg));
    pipelineMeta.addPipelineHop(new PipelineHopMeta(rowgwenB, pkg));

    // -- Dummy (zber výstupu)
    DummyMeta dummyMeta = new DummyMeta();
    TransformMeta dum = new TransformMeta("Out", dummyMeta);
    pipelineMeta.addTransform(dum);
    pipelineMeta.addPipelineHop(new PipelineHopMeta(pkg, dum));

    ITwoWayPasswordEncoder enc = Encr.getEncoder();
    if (enc == null) enc = new HopTwoWayPasswordEncoder();
    String projectHome = "/Users/peterpulmann/projects-hop/dev/config/projects/default";
    String metadataFolder = Paths.get(projectHome, "metadata").toString();

    IHopMetadataProvider provider = new JsonMetadataProvider(enc, metadataFolder, vars);

    System.out.println("Provider: " + provider);

    // 3) Engine (Local) + príprava
    IPipelineEngine<?> engine =
        PipelineEngineFactory.createPipelineEngine(
            vars, "local", provider, pipelineMeta); // ← :contentReference[oaicite:7]{index=7}

    LocalPipelineEngine local = (LocalPipelineEngine) engine;
    local.prepareExecution(); // pripojí rowsety

    // 4) Producer pre Injector + schéma vstupu
    //  RowProducer producer =  local.addRowProducer("Injector", 0); // ←
    // :contentReference[oaicite:8]{index=8}
    // IRowMeta inMeta = new RowMeta();
    // inMeta.addValueMeta(new ValueMetaString("in"));

    // 5) Poslucháč výstupu
    List<Object[]> outRows = Collections.synchronizedList(new ArrayList<>());
    ITransform outTransform =
        local.getTransform("Out", 0); // ← :contentReference[oaicite:9]{index=9}
    outTransform.addRowListener(
        new RowAdapter() {
          @Override
          public void rowWrittenEvent(IRowMeta rowMeta, Object[] row) {
            outRows.add(Arrays.copyOf(row, row.length));
          }
        });

    // 6) Štart + vstrekni dáta
    local.startThreads();
    // producer.putRow(inMeta, new Object[] {"a"});
    // producer.putRow(inMeta, new Object[] {"b"});
    // producer.finished();

    local.waitUntilFinished(); // ← :contentReference[oaicite:10]{index=10}
    assertTrue(local.getErrors() == 0, "Pipeline errors: " + local.getErrors());
    assertEquals(20, outRows.size());
    // tu vieš doplniť ďalšie asserty podľa toho, čo má tvoja transformácia robiť
  }
}
