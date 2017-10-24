import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Conductor {
	
	public static void main(String[] args) {
		Conductor c = new Conductor("musicFile.txt");
	}
	
	private boolean someoneIsSinging;
	private int turn;
	private Map enumMap;
	private List<ChoirMember> choir;
	private List<String> notes;
	private List<String> lengths;
	
	public Conductor(String fileName) {
		enumMap = new HashMap<Note,ChoirMember>();
		someoneIsSinging = false;
		turn = 0;
		
		notes = new LinkedList<String>();
		lengths = new LinkedList<String>();
		
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
			lengths.add(parsedLine[1]);
		}
		
		validateInput();
		
		choir = hireChoir();
	}
	
	public synchronized void acquire(int x) {
		while (x!=turn) {
			try {
				wait();
			} catch (InterruptedException ignore) {}
		}
		someoneIsSinging = true;	
	}
	
	
	public synchronized void release() {
		someoneIsSinging = false;
		turn++;
		notifyAll();	
	}

	private ArrayList<ChoirMember> hireChoir() {
		ArrayList<ChoirMember> c = new ArrayList<ChoirMember>();
		LinkedList<String> allNotes = new LinkedList<String>();
		
		for (int i = 0;i<notes.size();i++) {
			Note noteToPass = Note.valueOf(notes.get(i));
			NoteLength lenToPass = numberToNoteLength(lengths.get(i));
			int turn = i;
			if (enumMap.get(noteToPass)==null) {
				ChoirMember newHire = new ChoirMember(this);
				newHire.addNoteToPlay(noteToPass,lenToPass,turn);
				c.add(newHire);
				enumMap.put(noteToPass, newHire);
			}
			else {
				ChoirMember member = (ChoirMember) enumMap.get(noteToPass);
				member.addNoteToPlay(noteToPass,lenToPass,turn);
			}
		}
		return c;
	}
	
	private void validateInput() {
		//check all notes
		for (String note : notes) {
			
		}
		
		//check all note lengths
		for (String len : lengths) {
			
		}
		//check format?
	}
	
	private NoteLength numberToNoteLength(String len) {
		if (len.equals("1")) 
			return NoteLength.WHOLE;
		else if (len.equals("2"))
			return NoteLength.HALF;
		else if (len.equals("4"))
			return NoteLength.QUARTER;
		else
			return NoteLength.EIGTH;
		
	}
	
}


//These classes from Nate Williams
/**
* This class defines one bell not with note and length
*
*/
class BellNote {
  final Note note;
  final NoteLength length;

  BellNote(Note note, NoteLength length) {
      this.note = note;
      this.length = length;
  }
}


/**
* This enum defines valid note length for a bellnote.
*
*/
enum NoteLength {
  WHOLE(1.0f),
  HALF(0.5f),
  QUARTER(0.25f),
  EIGTH(0.125f);

  private final int timeMs;

  private NoteLength(float length) {
      timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
  }

  public int timeMs() {
      return timeMs;
  }
}


/**
* This enum defines valid notes
*
*/
enum Note {
  // REST Must be the first 'Note'
  REST,
  A4,
  A4S,
  B4,
  C4,
  C4S,
  D4,
  D4S,
  E4,
  F4,
  F4S,
  G4,
  G4S,
  A5;

  public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
  public static final int MEASURE_LENGTH_SEC = 1;

  // Circumference of a circle divided by # of samples
  private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

  private final double FREQUENCY_A_HZ = 440.0d;
  private final double MAX_VOLUME = 127.0d;

  private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

  private Note() {
      int n = this.ordinal();
      if (n > 0) {
          // Calculate the frequency!
          final double halfStepUpFromA = n - 1;
          final double exp = halfStepUpFromA / 12.0d;
          final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

          // Create sinusoidal data sample for the desired frequency
          final double sinStep = freq * step_alpha;
          for (int i = 0; i < sinSample.length; i++) {
              sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
          }
      }
  }

  public byte[] sample() {
      return sinSample;
  }
}