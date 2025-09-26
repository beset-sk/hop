package sk.b3t.apache.hop.pipeline.transforms.rapidstart;

import java.util.Iterator;
import java.util.List;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.stream.IStream;

public class Rapidstart extends BaseTransform<RapidstartMeta, RapidstartData> {

  public Rapidstart(
      TransformMeta transformMeta,
      RapidstartMeta meta,
      RapidstartData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  @Override
  public boolean init() {
    boolean ok = super.init();
    // sem prípadne inicializácia podľa meta.getPackageName(), meta.getCode()
    return ok;
  }

  @Override
  public boolean processRow() throws HopException {

    System.out.println("================================================");

    if (first) {
      first = false;

      //
      List<IStream> infoStreams = meta.getTransformIOMeta().getInfoStreams();

      for (Iterator iterator = infoStreams.iterator(); iterator.hasNext(); ) {
        IStream iStream = (IStream) iterator.next();
        data.streams.put(iStream.getTransformName(), findInputRowSet(iStream.getTransformName()));
      }
    }

    Object[] r = getRow(); // číta z 1..n vstupných rowsetov
    if (r == null) {
      setOutputDone();
      return false;
    }

    if (first) {
      first = false;
      // metadáta vstupného riadku – pri passthrough ich len prekopírujeme
      data.outputRowMeta = getInputRowMeta().clone();
      // ak chceš pridávať nové polia do výstupu, urob to tu cez
      // data.outputRowMeta.addValueMeta(...)

    }

    // --- tvoje spracovanie riadku ---
    // Príklad passthrough:
    Object[] out = r;

    // ak potrebuješ pracovať s Package/Code:
    String pkg = resolve(getMeta().getPackageName());
    String code = resolve(getMeta().getCode());
    // ... vlastná logika ...

    putRow(data.outputRowMeta, out);
    return true;
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
