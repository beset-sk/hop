package sk.b3t.apache.hop.pipeline.transforms.rapidstart;

import java.util.Map;
import org.apache.hop.core.IRowSet;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.pipeline.transform.BaseTransformData;

public class RapidstartData extends BaseTransformData {

  public RapidstartData() {
    // TODO Auto-generated constructor stub
    outputRowMeta = new RowMeta();
    outputRowMeta.addValueMeta(new ValueMetaString("line"));
  }

  public IRowMeta outputRowMeta;

  public Map<String, IRowSet> streams;
}
