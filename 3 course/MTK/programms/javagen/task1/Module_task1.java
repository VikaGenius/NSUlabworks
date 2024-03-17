// HASH COLLISIONS: YES
// timestamp: 1694758950000

package task1;

import com.area9innovation.flow.*;

@SuppressWarnings("unchecked")
final public class Module_task1 {
	public static final String f_arr2s(Object[] aarr) {
			return Module_string.f_strGlue(Native.map(aarr, ((Func1<Object,Object>)(Func1)Wrappers.w_i2s)), ",");
		}
	public static final Object f_main() {
			final Object[] l0_arr = (new Object[] { 1, 2, 3 });
			return Module_runtime.f_println(("arr: "+Module_task1.f_arr2s(l0_arr)));
		}
}
