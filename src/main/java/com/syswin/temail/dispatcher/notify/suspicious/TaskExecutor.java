package com.syswin.temail.dispatcher.notify.suspicious;

public interface TaskExecutor<T> {
  public boolean offer(T t);
  public T take() throws InterruptedException;
  public boolean handleTask(T t);
}
