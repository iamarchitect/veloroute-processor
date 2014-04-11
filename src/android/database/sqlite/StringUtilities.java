package android.database.sqlite;

import java.util.HashSet;
import java.util.Set;

public class StringUtilities {
	/**
	 * Utility function to check a string contains a set of characters.
	 * 
	 * @param str
	 * @param token
	 * @return
	 */
	public static int indexOfFirstContainedCharacter(String str, String token) {
		Set<Character> set = new HashSet<Character>();
		for (int i = 0; i < token.length(); i++) {
			set.add(token.charAt(i)); // Build a constant-time lookup table.
		}
		for (int i = 0; i < str.length(); i++) {
			if (set.contains(str.charAt(i))) {
				return i; // Found a character in s1 also in s2.
			}
		}
		return -1; // No matches.
	}
}