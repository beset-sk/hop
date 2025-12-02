package sk.b3t.apache.hop.pipeline.transforms.rapidstart;

public class Laboratory {

  public static void main(String[] args) {
    System.out.println(
        "ExecutionBuilder from: "
            + org.apache.hop.execution.ExecutionBuilder.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation());
    System.out.println(
        "LocalPipelineEngine from: "
            + org.apache.hop.pipeline.engines.local.LocalPipelineEngine.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation());
  }
}
