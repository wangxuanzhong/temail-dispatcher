package com.syswin.temail.dispatcher.notify.suspicious;

public interface TaskExecutor<T> {

  boolean offer(T t);

  T take() throws InterruptedException;

  boolean handleTask(T t);
}
