import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Conductor {
	
	public static void main(String[] args) {
		try {
			Conductor c = new Conductor(args[0]);
		} catch (FileNotFoundException e) {
			System.err.println("Invalid file name. Try again.");
		}
	}
	
	private boolean songIsOver;
	private int turn;
	private Map<Note,ChoirMember> enumMap;
	private List<ChoirMember> choir;
	private List<String> notes;
	private List<String> lengths;
	
	public Conductor(String fileName) throws FileNotFoundException {
		enumMap = new HashMap<Note,ChoirMember>();
		songIsOver = false;
		turn = 0;
		
		notes = new ArrayList<String>();
		lengths = new ArrayList<String>();
		
		List<String> input = new ArrayList<String>();
		
		File songFile = new File(fileName);
		if (!songFile.exists()) {
			throw new FileNotFoundException("File " + fileName + " cannot be located. Try again");
		}
		 try (FileReader fileReader = new FileReader(songFile);
                 BufferedReader in = new BufferedReader(fileReader)) {
			
			String nextLine = in.readLine();
			while (nextLine != null) {
				input.add(nextLine);
				nextLine = in.readLine();
			}
			
		} catch (Exception ignored) {ignored.printStackTrace();}
		
		
		if (validateInput(input)) {
			for (String s : input) {
				String[] parsedLine = s.split("\\s+");
				notes.add(parsedLine[0]);
				lengths.add(parsedLine[1]);
			}
			choir = hireChoir();
			startMusic();
			while (!songIsOver) {
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			fireChoir();
		}
		
	}
	
	public synchronized void acquire(int x) {
		while (x!=turn) {
			try {
				wait();
			} catch (InterruptedException ignore) {}
		}
	}
	
	public synchronized void release() {
		turn++;
		if (turn==notes.size()) {
			songIsOver = true;
		}
		notifyAll();	
	}

	private ArrayList<ChoirMember> hireChoir() {
		ArrayList<ChoirMember> c = new ArrayList<ChoirMember>();
		
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
	
	private void fireChoir() {
		for (ChoirMember c : choir) {
			c.concludeMusicSession();
		}
	}
	
	private void startMusic() {
		for (ChoirMember c : choir) {
			c.startPlaying();
		}
	}
	
	private boolean validateInput(List<String> input) {
		boolean valid = true;

		for (String s : input) {
			String[] parsedLine = s.split("\\s+");
			if (parsedLine.length!=2) {
				valid = false;
				System.err.println(s + " is not a valid line.");
			}
			else {
				//check the note
				String note = parsedLine[0];
				try {
					Note.valueOf(note); //if this fails, it was invalid
				} catch(Exception ignore) {
					valid = false;
					System.err.println(note + " is not a valid note.");
				}
				
				//check the length
				String len = parsedLine[1];
				if (numberToNoteLength(len)==null) {
					System.err.println(len + " is not a valid note length.");
					valid = false;
				}
			}
		}

		if (!valid) {
			System.err.println("Invalid input. Program will now terminate.");
		}
		
		return valid;
		
	}
	
	private NoteLength numberToNoteLength(String len) {
		switch (len) {
			case "1":
				return NoteLength.WHOLE;
			case "2":
				return NoteLength.HALF;
			case "4":
				return NoteLength.QUARTER;
			case "8":
				return NoteLength.EIGTH;
		}
		return null;
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