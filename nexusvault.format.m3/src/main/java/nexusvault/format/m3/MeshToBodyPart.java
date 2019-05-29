package nexusvault.format.m3;

/**
 * This class is work in progress and it's name might change in the future, if its usage is more clear. <br>
 * The values listed in this enum are not complete.
 * <p>
 * Missing values: <b>
 * <ul>
 * <li>15
 * <li>28
 * <li>31
 * <li>32
 * <li>40 -&gt; 80
 * <li>89
 * <li>90
 * <li>93 -&gt; 255
 * </ul>
 *
 */
public enum MeshToBodyPart {
	MISSING(-1),
	NONE(0),
	LEFT_ANKLE(1),
	RIGHT_ANKLE(2),
	LEFT_UPPER_ARM(3),
	RIGHT_UPPER_ARM4(4),
	LEFT_LOWER_LEG(5),
	RIGHT_LOWER_LEG(6),
	LEFT_KNEE_BELOW(7),
	RIGHT_KNEE_BELOW(8),
	LOWER_CHEST_AND_BACK(9),
	SHOULDERS_AND_BACK(10),
	LEFT_EAR(11),
	RIGHT_EAR(12),
	LEFT_ELBOW(13),
	RIGHT_ELBOW(14),
	// MISSING(15),
	LEFT_FINGERS(16),
	RIGHT_FINGERS(17),
	LEFT_HEEL(18),
	RIGHT_HEEL(19),
	LEFT_LOWER_ARM(20),
	RIGHT_LOWER_ARM(21),
	LEFT_PALM(22),
	RIGHT_PALM(23),
	LEFT_KNEE(24),
	RIGHT_KNEE(25),
	NECK(26),
	PELVIC(27),
	// MISSING(28),
	LEFT_ARM_TRICEPS(29),
	RIGHT_ARM_TRICEPS(30),
	// MISSING(31),
	// MISSING(32),
	LEFT_THIGH(33),
	RIGHT_THIGH(34),
	LEFT_TOES(35),
	RIGHT_TOES(36),
	MIDRIFF(37),
	LEFT_WRIST(38),
	RIGHT_WRIST(39),
	// MISSING(40 - 80),
	HEAD(81),
	CHEST(85),
	TAIL(88),
	// MISSING(89),
	// MISSING(90),
	/** May be female specific */
	UPPERBUST(91),
	NECK_START(92);

	private final int id;

	private MeshToBodyPart(int id) {
		this.id = id;
	}

	public static MeshToBodyPart resolve(int value) {
		for (final MeshToBodyPart e : MeshToBodyPart.values()) {
			if (e.id == value) {
				return e;
			}
		}
		return MeshToBodyPart.MISSING;
	}
}