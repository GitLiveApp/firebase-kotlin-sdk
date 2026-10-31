package compat

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidSdkApiJarDumperTest {

    private fun classBytes(vararg names: String): List<ByteArray> = names.map { name ->
        checkNotNull(javaClass.classLoader.getResourceAsStream("$name.class")) { "missing fixture $name" }.use { it.readBytes() }
    }

    private val fixtures = classBytes(
        "compat/fixture/Widget", "compat/fixture/Widget\$Builder", "compat/fixture/Widget\$Kind", "compat/fixture/Widget\$Private", "compat/fixture/Widget\$PackagePrivate",
        "compat/fixture/Listener", "compat/fixture/Base", "compat/fixture/zzabc", "compat/fixture/R", "compat/fixture/R\$string",
        "compat/fixture/internal/Secret", "compat/fixture/Marker", "compat/fixture/Hidden",
    )

    @Test
    fun `dumps public api in metalava format`() {
        val expected = """
            // Signature format: 3.0
            package compat.fixture {

              public abstract class Base extends compat.fixture.Widget implements compat.fixture.Listener java.lang.Runnable {
                ctor public Base();
                method public abstract String name();
                method public void run();
              }

              public interface Listener {
                method public void onEvent(compat.fixture.Widget);
              }

              public @interface Marker {
                method public abstract String value();
              }

              public class Widget implements compat.fixture.Listener {
                ctor public Widget();
                ctor public Widget(String, int...);
                method public static compat.fixture.Widget getInstance();
                method public String getLabel();
                method public compat.fixture.Widget.Kind kind();
                method @Deprecated public void old(java.util.List);
                method protected void onChange();
                method public void onEvent(compat.fixture.Widget);
                method public final void setLabel(String);
                field public static final int MAX = 3;
                field public static final String PROVIDER_ID = "widget";
                field public static final long TIMEOUT = 5L;
                field protected String label;
              }

              public static final class Widget.Builder {
                ctor public Widget.Builder();
                method public compat.fixture.Widget build();
                method public compat.fixture.Widget.Builder setLabel(String);
              }

              public enum Widget.Kind {
                enum_constant public static final compat.fixture.Widget.Kind LARGE;
                enum_constant public static final compat.fixture.Widget.Kind SMALL;
              }

            }

        """.trimIndent() + "\n"
        assertEquals(expected, AndroidSdkApiJarDumper.dumpClasses(fixtures))
    }
}
