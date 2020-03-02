package nexusvault.format.m3;

/**
 * This class is work in progress and it's name might change in the future, if its usage is more clear. <br>
 * The values listed in this enum are not complete.
 * <p>
 * Missing values:
 * <ul>
 * <li><b>28</b>
 * <li><b>31, 32</b>
 * <li><b>40 -&gt; 80</b>
 * <li><b>86, 87, 89</b>
 * <li><b>90, 93, 94, 95</b>
 * <li><b>97 -&gt; 255 </b>
 * </ul>
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
	/** Used for eyes, faces, piercings and sometimes for other facial elements */
	FACE_ELEMENT(15),
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
	/** This seems exclusive to chua */
	LEFT_THIGH_OR_KNEE_FUR(82),
	/** This seems exclusive to chua */
	RIGHT_THIGH_OR_KNEE_FUR(83),
	/** Can be seen on male granok and human, kind of placeholder for the face, but inside of the head */
	INNER_HEAD(84),
	CHEST(85),
	// MISSING(86)
	// MISSING(87)
	TAIL(88),
	// MISSING(89),
	// MISSING(90),
	/** May be female specific */
	UPPERBUST(91),
	NECK_START(92),
	// MISSING(93 - 95),
	HORNS(96);

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