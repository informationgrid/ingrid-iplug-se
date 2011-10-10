package org.apache.nutch.admin.scheduling;

import java.io.IOException;

public class ExecuteProcessTool {

  public static boolean execute(String command, String argument)
      throws IOException {
    int exitValue = 0;
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec(command + " " + argument);
    try {
      exitValue = process.waitFor();
    } catch (InterruptedException e) {
      throw new IOException(e.getMessage());
    }
    return exitValue != 0 ? false : true;
  }

}
