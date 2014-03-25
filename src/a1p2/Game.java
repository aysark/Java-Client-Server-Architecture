package a1p2;

import java.util.ArrayList;

public class Game {

	private String[] wordList = {"caterpillar","stone","awesome","class","paralysis","prophet","nuance","teller","filter","programming"};
	public String word;
	public String word_sofar = "";
	private ArrayList<Character> lettersGuessed = new ArrayList<Character>();
	
	public Game() {
		word = wordList[(int) (Math.random() * wordList.length)];
		
		for (char letter: word.toCharArray()) {
			word_sofar = word_sofar.concat("_");
        }
	}
	
	public Game(String word) {
		this.word = word;
		for (char letter: word.toCharArray()) {
			word_sofar = word_sofar.concat("_");
        }
	}
	
	public String guessLetter(Player player, char letter) {
		if (!isGuessed(letter)) {
			addGuess(letter);
            if (getIndexesOf(letter).size() > 0) {
            	player.score++;
            	for (int i=0;i<word.length();i++) {
            		if (word.charAt(i) == letter) {
            			char[] chars = word_sofar.toCharArray();
                    	chars[i] = letter;
                    	word_sofar = String.valueOf(chars);
                    	break;
            		}
            	}
                return "Good guess! +1";
            }
            else {
            	player.score--;
                return "Uh oh, that letter is not in the word. -1";
            }
        }
        else {
            return "Meh.. looks like that letter has already been guessed!";
        }
	}
		
	public void addGuess(char letter) {
        lettersGuessed.add(letter);
    }
	
	public ArrayList<Character> getLettersGuessed() {
        return lettersGuessed;
    }
	
	private ArrayList<Integer> getIndexesOf(char letter) {
        ArrayList<Integer> instances = new ArrayList<Integer>();
        for (int i = word.indexOf(letter); i >= 0; i = word.indexOf(letter, i + 1)) {
            instances.add(i);
        }
        return instances;
    }
	
	private boolean isGuessed(char letter) {
        return lettersGuessed.indexOf(letter) != -1;
    }

	
	class Player {
		int score;
		String name;
		int port;
		
		public Player(String name, int port) {
			this.score = 0;
			this.name = name;
			this.port = port;
		}
	}
}
