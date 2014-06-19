import static org.junit.Assert.*;

import org.junit.Test;


public class MFTest {

	private char[][] input = new char[4][4];
	private char[][] test = new char[4][4];
	
	private void reset(){
		for(int i = 0 ; i < 4; i++){
			for(int j = 0; j < 4; j++){
				input[i][j] = '.';
				test[i][j] = '0';
			}
		}	
	}
	
	@Test
	public void testNone() {
		reset();
		assertArrayEquals(test, MF.generate(4, 4, input));
	}
	
	@Test
	public void testSingle() {
		reset();
		input[2][2] = '*';
		test[2][2] = '*';
		test[2][3] = '1';
		test[2][1] = '1';
		test[1][2] = '1';
		test[3][2] = '1';
		
		assertArrayEquals(test, MF.generate(4, 4, input));	
	}
	
	@Test
	public void testSingleEdge() {
		reset();
		input[0][0] = '*';
		test[0][0] = '*';
		test[0][1] = '1';
		test[1][0] = '1';
		
		assertArrayEquals(test, MF.generate(4, 4, input));	
	}
	
	@Test
	public void testTwoStars() {
		reset();
		input[0][0] = '*';
		input[1][1] = '*';
		test[0][0] = '*';
		test[1][1] = '*';
		test[0][1] = '2';
		test[1][0] = '2';
		test[1][2] = '1';
		test[2][1] = '1';
		
		assertArrayEquals(test, MF.generate(4, 4, input));	
		
	}
	
	@Test
	public void testThreeStars() {
		reset();
		input[0][0] = '*';
		input[1][1] = '*';
		input[0][2] = '*';
		test[0][0] = '*';
		test[1][1] = '*';
		test[0][2] = '*';
		test[0][1] = '3';
		test[1][0] = '2';
		test[1][2] = '2';
		test[2][1] = '1';
		test[0][3] = '1';
		
		assertArrayEquals(test, MF.generate(4, 4, input));	
		
	}
	
	@Test
	public void testStarOnTop() {
		reset();
		input[0][0] = '*';
		input[1][0] = '*';
		test[0][0] = '*';
		test[1][0] = '*';
		test[0][1] = '1';
		test[1][1] = '1';
		test[2][0] = '1';
		
		
		assertArrayEquals(test, MF.generate(4, 4, input));	
		
	}
	

	@Test
	public void testStarOnSide() {
		reset();
		input[0][0] = '*';
		input[0][1] = '*';
		test[0][0] = '*';
		test[0][1] = '*';
		test[1][0] = '1';
		test[1][1] = '1';
		test[0][2] = '1';
		
		
		assertArrayEquals(test, MF.generate(4, 4, input));	
		
	}
	
}
