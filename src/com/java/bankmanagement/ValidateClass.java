package com.java.bankmanagement;

import java.util.regex.Pattern;

public class ValidateClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//System.out.println(Pattern.matches("[^0][0-9]+", "13112233"));
		//System.out.println(Pattern.matches("[^0][0-9]{7}", "13112233"));
		System.out.println(Pattern.matches("[F][D][-][0-9]{6}","FD-675134"));
		//System.out.println(Pattern.matches("[FD]+","FD-675134"));

	}

}
