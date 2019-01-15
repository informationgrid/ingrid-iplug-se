/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
package de.ingrid.iplug.se.webapp.controller.instance.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.ingrid.iplug.se.SEIPlug;

public class Persistence<T> {

    // protected File _workingDirectory;

    public void makePersistent(T t, String instanceName) throws IOException {
        String workingDirectory = checkWorkingDirectory(instanceName);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream( new FileOutputStream( new File(
                workingDirectory, t.getClass().getSimpleName() + ".ser" ) ) );
        objectOutputStream.writeObject( t );
        objectOutputStream.close();
    }

    public void makeTransient(Class<T> t, String instanceName) throws IOException {
        String workingDirectory = checkWorkingDirectory(instanceName);
        File file = new File( workingDirectory, t.getSimpleName() + ".ser" );
        file.delete();
    }

    @SuppressWarnings("unchecked")
    public T load(Class<T> t, String instanceName) throws IOException, ClassNotFoundException {
        String workingDirectory = checkWorkingDirectory(instanceName);
        ObjectInputStream objectInputStream = new ObjectInputStream( new FileInputStream( new File( workingDirectory,
                t.getSimpleName() + ".ser" ) ) );
        T t2 = (T) objectInputStream.readObject();
        objectInputStream.close();
        return t2;
    }

    public boolean exists(Class<T> t, String instanceName) {
        String workingDirectory = checkWorkingDirectory(instanceName);
        File file = new File( workingDirectory, t.getSimpleName() + ".ser" );
        return file.exists();
    }

//    protected void setWorkingDirectory(File workingDirectory) throws Exception {
//        _workingDirectory = workingDirectory;
//    }

    private String checkWorkingDirectory(String instanceName) {
        String dir = SEIPlug.conf.getInstancesDir();
        File instanceDir = new File(dir, instanceName);
        if (instanceDir == null || !instanceDir.exists()) {
            throw new RuntimeException( "working directory does not exists" );
        }
        return instanceDir.getAbsolutePath();
    }
}
