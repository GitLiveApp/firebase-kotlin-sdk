package compat.fixture;

/** Fixture for {@link compat.AndroidSdkApiJarDumperTest}: a typical closed-source SDK class. */
public class Widget implements Listener {
    public static final String PROVIDER_ID = "widget";
    public static final int MAX = 3;
    public static final long TIMEOUT = 5L;
    protected String label;
    private int hidden;
    int packagePrivate;

    public Widget() {}
    public Widget(String label, int... flags) { this.label = label; }
    private Widget(int hidden) {}

    public static Widget getInstance() { return new Widget(); }
    public String getLabel() { return label; }
    public final void setLabel(String label) { this.label = label; }
    @Deprecated public void old(java.util.List<String> values) {}
    protected void onChange() {}
    @androidx.annotation.RestrictTo(androidx.annotation.RestrictTo.Scope.LIBRARY) public void restricted() {}
    private void secret() {}
    void packagePrivateMethod() {}
    public void withHidden(zzabc arg) {}
    public zzabc returnsHidden() { return null; }
    public void withInternal(compat.fixture.internal.Secret arg) {}
    public zzabc zzhelper() { return null; }
    @Override public void onEvent(Widget source) {}
    public Kind kind() { return Kind.SMALL; }

    public static final class Builder {
        public Builder setLabel(String label) { return this; }
        public Widget build() { return new Widget(); }
    }

    public enum Kind { SMALL, LARGE }

    private static class Private {}

    static class PackagePrivate {}
}
