package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import nexusvault.vault.IdxPath;

class Idx {

	@Test
	public void testEmptyPath() {
		final var emptyPath = IdxPath.createPath();
		assertEquals(0, emptyPath.length());
		assertEquals("", emptyPath.getFullName());
		assertFalse(emptyPath.hasParent());
		assertTrue(emptyPath.isRoot());
		assertNull(emptyPath.getParent());
	}

	@Test
	public void testImmutable() {
		final var immutable = IdxPath.createPath("test");
		final var anotherPath = immutable.resolve("test");
		assertNotEquals(immutable, anotherPath);
		assertEquals("test", immutable.getFullName());
		assertEquals("test" + IdxPath.SEPARATOR + "test", anotherPath.getFullName());
	}

	@Test
	public void testEmptyResolvesToEmpty() {
		final var path = IdxPath.createPath().resolve("").resolve("").resolve("");
		assertEquals(0, path.length());
		assertTrue(path.isRoot());
		assertEquals("", path.getFullName());
	}

	@Test
	public void testConstructors() {
		final var expected = "step1" + IdxPath.SEPARATOR + "step2" + IdxPath.SEPARATOR + "step3";

		final var pathA = IdxPath.createPath().resolve("step1").resolve("step2").resolve("step3");
		final var pathB = IdxPath.createPath(Arrays.asList("step1", "step2", "step3"));
		final var pathC = IdxPath.createPath("step1", "step2", "step3");
		final var pathD = IdxPath.createPathFrom(expected);
		final var pathE = IdxPath.createPathFrom("step1;step2;step3", ";");

		assertEquals(3, pathA.length(), "pathA: length");
		assertEquals(3, pathB.length(), "pathB: length");
		assertEquals(3, pathC.length(), "pathC: length");
		assertEquals(3, pathD.length(), "pathD: length");
		assertEquals(3, pathE.length(), "pathE: length");

		assertEquals(expected, pathA.getFullName(), "pathA: path");
		assertEquals(expected, pathB.getFullName(), "pathB: path");
		assertEquals(expected, pathC.getFullName(), "pathC: path");
		assertEquals(expected, pathD.getFullName(), "pathD: path");
		assertEquals(expected, pathE.getFullName(), "pathE: path");
	}

	@Test
	public void testEquals() {
		final var pathA = IdxPath.createPath("A", "B", "C");
		final var pathB = IdxPath.createPath("A", "B", "C");
		assertEquals(pathA, pathB);
	}

}
