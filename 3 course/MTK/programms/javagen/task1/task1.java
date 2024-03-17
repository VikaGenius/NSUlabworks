// HASH COLLISIONS: YES
// timestamp: 1694759408204

package task1;

import com.area9innovation.flow.*;

@SuppressWarnings("unchecked")
public final class task1 extends FlowRuntime {

	// Init hosts: 1
	private static final void init_hosts() {
		FlowRuntime.registerNativeHost(Native.class);
	}

	// Init modules: 3
	private static final void init_modules() {
		Module_url.init();
		Module_securitymode.init();
		Module_runtime.init();
	}
	private static final void init() {
		Structs.init();
		init_hosts();
		init_modules();
	}
	public static void main(String[] args) {
		FlowRuntime.program_args = args;
		task1.init();
		Module_task1.f_main();
	}
}
