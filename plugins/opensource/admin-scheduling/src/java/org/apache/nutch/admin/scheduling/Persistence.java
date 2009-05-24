package org.apache.nutch.admin.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Persistence<T> {

  protected File _workingDirectory;

  public void makePersistent(T t) throws IOException {
    checkWorkingDirectory();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(
        new FileOutputStream(new File(_workingDirectory, t.getClass()
            .getSimpleName()
            + ".ser")));
    objectOutputStream.writeObject(t);
    objectOutputStream.close();
  }

  public void makeTransient(Class<T> t) throws IOException {
    checkWorkingDirectory();
    File file = new File(_workingDirectory, t.getSimpleName() + ".ser");
    file.delete();
  }

  @SuppressWarnings("unchecked")
  public T load(Class<T> t) throws IOException, ClassNotFoundException {
    checkWorkingDirectory();
    ObjectInputStream objectInputStream = new ObjectInputStream(
        new FileInputStream(new File(_workingDirectory, t.getSimpleName()
            + ".ser")));
    T t2 = (T) objectInputStream.readObject();
    objectInputStream.close();
    return t2;
  }

  public boolean exists(Class<T> t) {
    checkWorkingDirectory();
    File file = new File(_workingDirectory, t.getSimpleName() + ".ser");
    return file.exists();
  }

  protected void setWorkingDirectory(File workingDirectory) throws Exception {
    _workingDirectory = workingDirectory;
  }

  private void checkWorkingDirectory() {
    if (_workingDirectory == null || !_workingDirectory.exists()) {
      throw new RuntimeException("working directory does ot exists");
    }
  }
}
