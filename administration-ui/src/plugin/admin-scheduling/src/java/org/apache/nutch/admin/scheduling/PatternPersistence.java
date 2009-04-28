package org.apache.nutch.admin.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.springframework.stereotype.Service;

@Service
public class PatternPersistence {

  public void savePattern(File folder, String pattern) throws IOException {
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(
        new FileOutputStream(new File(folder.getAbsoluteFile(), "pattern.ser")));
    Pattern patternObject = new Pattern();
    patternObject.setPattern(pattern);
    objectOutputStream.writeObject(patternObject);
    objectOutputStream.close();
  }

  public Pattern loadPattern(File folder) throws Exception {
    ObjectInputStream objectInputStream = new ObjectInputStream(
        new FileInputStream(new File(folder, "pattern.ser")));
    Pattern pattern = (Pattern) objectInputStream.readObject();
    return pattern;
  }

  public boolean existsPattern(File instanceFolder) {
    return new File(instanceFolder, "pattern.ser").exists();
  }

  public void deletePattern(File instanceFolder) {
    new File(instanceFolder, "pattern.ser").delete();
  }
}
