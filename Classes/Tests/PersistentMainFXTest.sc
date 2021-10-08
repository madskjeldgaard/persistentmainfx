PersistentMainFXTest1 : UnitTest {
	test_check_classname {
		var result = PersistentMainFX.new;
		this.assert(result.class == PersistentMainFX);
	}
}


PersistentMainFXTester {
	*new {
		^super.new.init();
	}

	init {
		PersistentMainFXTest1.run;
	}
}
