package de.ingrid.iplug.se.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used help to exchange simple objects between different plugins.
 * A {@linkplain Queue} will be used here to connect the producer and consumer.
 * 
 * @author ralfwehner
 * @see InterplugInCommunication
 */
public class InterplugInQueueCommunication<T> {

  private static InterplugInQueueCommunication<String> _stringQueueCommunication = null;

  private final Map<String, BlockingQueue<T>> _objectContent;

  private Lock _contentLock = new ReentrantLock(true);

  public InterplugInQueueCommunication() {
    super();
    _objectContent = new HashMap<String, BlockingQueue<T>>();
  }

  public static InterplugInQueueCommunication<String> getInstanceForStringQueues() {
    if (_stringQueueCommunication == null) {
      _stringQueueCommunication = new InterplugInQueueCommunication<String>();
    }
    return _stringQueueCommunication;
  }

  public void setObjectContent(String key, BlockingQueue<T> content) {
    _contentLock.lock();
    try {
      _objectContent.put(key, content);
    } finally {
      _contentLock.unlock();
    }
  }

  public BlockingQueue<T> getObjectContent(String key) {
    return _objectContent.get(key);
  }

  public boolean offer(String key, T element) {
    _contentLock.lock();
    try {
      if (!_objectContent.containsKey(key)) {
        _objectContent.put(key, new LinkedBlockingQueue<T>(10000));
      }
      return _objectContent.get(key).offer(element);
    } finally {
      _contentLock.unlock();
    }
  }

  public T poll(String key) {
    _contentLock.lock();
    try {
      if (!_objectContent.containsKey(key)) {
        return null;
      }
      return _objectContent.get(key).poll();
    } finally {
      _contentLock.unlock();
    }
  }
}
