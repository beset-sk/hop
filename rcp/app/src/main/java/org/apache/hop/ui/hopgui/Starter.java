package org.apache.hop.ui.hopgui;

public class Starter {

  public static void main(String[] args) {

    System.out.println("--------------------------------------------");
    System.out.println(System.getProperty("org.apache.poi.ss.ignoreMissingFontSystem"));
    System.out.println("===============================================");
    HopGui.main(args);
  }
}
