// HASH COLLISIONS: YES
// timestamp: 1694758754000

package task1;

import com.area9innovation.flow.*;

@SuppressWarnings("unchecked")
final public class Module_runtime {
	public static Reference<Boolean> g_localStorageEnabled;
	public static void init() {
		g_localStorageEnabled=((Reference<Boolean>)(new Reference(true)));
	}
	public static final Object f_println(Object astr) {
			if (Module_securitymode.f_isLoggingEnabled()) {
				return Native.println(((Object)astr));
			} else {
				return null;
			}
		}
}
