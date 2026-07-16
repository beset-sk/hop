package org.apache.hop.ui.hopgui;

import java.net.URL;

public class Starter {

  public static void main(String[] args) {

    System.out.println("--------------------------------------------");
    System.out.println(System.getProperty("org.apache.poi.ss.ignoreMissingFontSystem"));
    System.out.println(
        "DHOP_SHARED_JDBC_FOLDERS: " + System.getProperty("HOP_SHARED_JDBC_FOLDERS"));
    System.out.println("===============================================");
    URL an = Starter.class.getClassLoader().getResource("oracle/jdbc/OracleDriver.class");
    System.out.println("AN " + an);

    HopGui.main(args);
  }
}
