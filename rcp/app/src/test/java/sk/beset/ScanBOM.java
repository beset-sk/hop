package sk.beset;

import java.io.File;
import java.io.FileInputStream;

public class ScanBOM {

  public static void main(String[] args) throws Exception {

    File bom =
        new File(
            "/Users/peterpulmann/projects/HopProjects/config/projects/nestle_EGYPT/_data_out/Custom/bom.csv");

    FileInputStream bomStream = new FileInputStream(bom);

    File noBom =
        new File(
            "/Users/peterpulmann/projects/HopProjects/config/projects/nestle_EGYPT/_data_out/Custom/bom.append.csv");

    FileInputStream noBomStream = new FileInputStream(noBom);

    System.out.println(Integer.toHexString(bomStream.read()));
    System.out.println(Integer.toHexString(bomStream.read()));
    System.out.println(Integer.toHexString(bomStream.read()));

    while (true) {
      int nextNoBom = noBomStream.read();
      int nextBom = bomStream.read();

      if (nextNoBom < 0) {
        if (nextBom >= 0) System.out.println("---------------------------");
        break;
      }
      ;

      if (nextBom < 0) {
        if (nextNoBom >= 0) System.out.println("============================");
        break;
      }
      ;

      if ((nextNoBom - nextBom) != 0) System.out.println(nextNoBom - nextBom);
    }
  }
}
