package compat.fixture;

public abstract class Base extends Widget implements Listener, Runnable {
    public abstract String name();
    @Override public void run() {}
}
