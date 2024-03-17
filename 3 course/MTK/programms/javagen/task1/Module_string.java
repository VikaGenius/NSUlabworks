// HASH COLLISIONS: YES
// timestamp: 1694758754000

package task1;

import com.area9innovation.flow.*;

@SuppressWarnings("unchecked")
final public class Module_string {
	public static final String f_i2s(int ai) {
			return Integer.toString((int)(ai));
		}
	public static final String f_strGlue(Object[] aarr, String asep) {
			if (((Object[])aarr).length == 0) {
				return "";
			} else {
				if ((Native.length(aarr)==1)) {
					return ((String)(aarr[0]));
				} else {
					final Struct l2_$1 = Module_list.f_makeList();
					final String l4_sep = asep;
					final Func3<Struct_Cons,Integer, Struct, String> l3_$0 = (Func3<Struct_Cons, Integer, Struct, String>)(Integer aidx, Struct aacc, String ae) -> {
						if ((((int)aidx)==0)) {
							return (new Struct_Cons(ae, aacc));
						} else {
							return (new Struct_Cons(ae, (new Struct_Cons(l4_sep, aacc))));
						}
					};
					return Native.list2string(((Struct)Native.foldi(aarr, l2_$1, ((Func3<Object,Integer, Object, Object>)(Func3)l3_$0))));
				}
			}
		}
}
