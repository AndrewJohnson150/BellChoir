import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Conductor {
	
	public static void main(String[] args) {
		Conductor c = new Conductor("musicFile.txt");
	}
	
	private List<ChoirMember> choir;
	private List<String> notes;
	private List<Integer> lengths;
	
	public Conductor(String fileName) {
		notes = new LinkedList<String>();
		lengths = new LinkedList<Integer>();
		
		List<String> input = new LinkedList<String>();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String nextLine = in.readLine();
			while (nextLine != null) {
				input.add(nextLine);
				nextLine = in.readLine();
			}
		} catch (Exception ignored) {ignored.printStackTrace();}
		
		for (String s : input) {
			String[] parsedLine = s.split("\\s+");
			notes.add(parsedLine[0]);
			lengths.add(Integer.parseInt(parsedLine[1]));
		}
	}
	
	private ArrayList<ChoirMember> hireChoir() {
		ArrayList<ChoirMember> c = new ArrayList<ChoirMember>();
		LinkedList<String> allNotes = new LinkedList<String>();
		
		for (int i = 0;i<notes.size();i++) {
			if (allNotes.contains(notes.get(i))) {
				int indexOfChoirMember = c.indexOf(notes.get(i));
				c.get(i).addNoteToPlay(notes.get(i),lengths.get(i));
			}
			else {
				
			}
		}
		
		return c;
	}
	
}
