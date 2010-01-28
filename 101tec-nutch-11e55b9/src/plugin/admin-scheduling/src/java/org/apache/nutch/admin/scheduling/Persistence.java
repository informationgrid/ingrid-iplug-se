/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
