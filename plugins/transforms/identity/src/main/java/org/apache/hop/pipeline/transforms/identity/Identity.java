package org.apache.hop.pipeline.transforms.identity;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

public class Identity extends BaseTransform<IdentityMeta, IdentityData> {

  private static final Class<?> PKG = IdentityMeta.class;

  public Identity(
      TransformMeta transformMeta,
      IdentityMeta meta,
      IdentityData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean processRow() throws HopException {

    Object[] rowData = getRow();
    if (rowData == null) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    Object[] rowCopy = null;
    if (getTransformMeta().isDoingErrorHandling()) {
      rowCopy = getInputRowMeta().cloneRow(rowCopy);
    }

    if (first) {
      first = false;
    }

    putRow(getInputRowMeta(), rowData);

    return true;
  }

  @Override
  public boolean init() {
    // TODO Auto-generated method stub
    return super.init();
  }
}
