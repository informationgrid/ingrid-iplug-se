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
package org.apache.nutch.admin.searcher;

import junit.framework.TestCase;

public class TestThreadPool extends TestCase {

  public static int _counter = 0;

  public static class CounterRunnable implements Runnable {
    @Override
    public void run() {
      _counter++;
    }
  }

  public void testThreadPool() throws Exception {
    ThreadPool threadPool = new ThreadPool();
    CounterRunnable counterRunnable1 = new CounterRunnable();
    CounterRunnable counterRunnable2 = new CounterRunnable();
    CounterRunnable counterRunnable3 = new CounterRunnable();
    threadPool.execute(counterRunnable1);
    threadPool.execute(counterRunnable2);
    threadPool.execute(counterRunnable3);
    Thread.sleep(500);
    assertEquals(3, _counter);
  }
}
