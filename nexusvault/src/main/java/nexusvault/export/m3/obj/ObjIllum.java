package nexusvault.export.m3.obj;

enum ObjIllum {
	/** Color on and Ambient off */
	ZERO,
	/** Color on and Ambient on */
	ONE,
	/** Highlight on */
	TWO,
	/** Reflection on and Ray trace on */
	THREE,
	/** Transparency: Glass on, Reflection: Ray trace on */
	FOUR,
	/** Reflection: Fresnel on and Ray trace on */
	FIVE,
	/** Transparency: Refraction on, Reflection: Fresnel off and Ray trace on */
	SIX,
	/** Transparency: Refraction on, Reflection: Fresnel on and Ray trace on */
	SEVEN,
	/** Reflection on and Ray trace off */
	EIGHT,
	/** Transparency: Glass on, Reflection: Ray trace off */
	NINE,
	/** Casts shadows onto invisible surfaces */
	TEN;

	public int getValue() {
		return ordinal();
	}
}