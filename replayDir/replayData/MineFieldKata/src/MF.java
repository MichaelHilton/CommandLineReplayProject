
public class MF {

	private static char[][] mineField;
	
	private static void initialize(int height, int width){
		mineField = new char[height][width];
		for(int i = 0; i < height; i++){
			for(int j = 0 ; j < width; j++){
				mineField[i][j] = '0';
			}
		}
	}
	
	private static void checkStar(char[][] input, int x, int y, int height, int width){
		if(input[x][y] == '*'){
			if(x > 0){
				if(mineField[x - 1][y] != '*'){
					if(mineField[x - 1][y] != '0'){
						mineField[x - 1][y]++;	
					}else{
						mineField[x - 1][y] = '1';
					}
				}
			}
			if(x < height - 1){
				if(mineField[x + 1][y] != '*'){
					if(mineField[x + 1][y] != '0'){
						mineField[x + 1][y]++;	
					}else{
						mineField[x + 1][y] = '1';
					}
				}
			}
			if(y > 0){
				if(mineField[x][y - 1] != '*'){		
					if(mineField[x][y - 1] != '0'){
						mineField[x][y - 1]++;	
					}else{
						mineField[x][y - 1] = '1';
					}		
				}
			}
			if(y < width - 1){
				if(mineField[x][y + 1] != '*'){		
					if(mineField[x][y + 1] != '0'){
						mineField[x][y + 1]++;	
					}else{
						mineField[x][y + 1] = '1';
					}
				}
			}
			mineField[x][y] = '*';
		}
	
	}
	
	public static char[][] generate(int height, int width, char[][] input){
		initialize(height, width);
		
		for(int i = 0; i < height; i++){
			for(int j = 0 ; j < width; j++){
				checkStar(input, i, j, height, width);
			}
		}
		
		
		return mineField;
	}
	
}
