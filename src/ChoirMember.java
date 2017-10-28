import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class ChoirMember extends Thread{
	private List<Note> notes;
	private List<NoteLength> lengths;
	private List<Integer> turns;
	private final AudioFormat af;
	private Thread t;
	private Conductor mutex;
	
	/**
	 * Creates a new ChoirMember object which can play BellNotes when given a Note and Length via {@link #addNoteToPlay(Note, NoteLength, int)}
	 * and {@link #startPlaying()}
	 * @param c
	 */
	public ChoirMember(Conductor c) {
		af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
		t = new Thread(this);
		mutex = c;
		notes = new ArrayList<Note>();
		lengths = new ArrayList<NoteLength>();
		turns = new ArrayList<Integer>();
	}
	
	/**
	 * this method gives a ChoirMember a new note to play
	 * @param n the Note to play
	 * @param len the NoteLength which designates how long to play for
	 * @param t the turn at which the note should be played
	 */
	public void addNoteToPlay(Note n, NoteLength len, int t) {
		notes.add(n);
		lengths.add(len);
		turns.add(t);
	}
	
	/**
	 * starts playing notes
	 */
	public void startPlaying() {
		t.start();
	}
	
	
	/**
	 * loop through all of the notes given to the ChoirMember,
	 * aquire the mutex, play the note, release the mutex.
	 */
	public void run() {
		for (int i = 0; i<notes.size();i++) {
			//acquire the mutex 
			mutex.acquire(turns.get(i));
			
			BellNote noteToPlay = new BellNote(notes.get(i),lengths.get(i));
			try {
				play(noteToPlay);
			} catch (LineUnavailableException ignore) {ignore.printStackTrace();}
			
			//release the mutex
			mutex.release();
		}
	}

	
	/**
	 * opens the line, calls playNote, then drains and closes the line.
	 * @see #playNote(SourceDataLine, BellNote)
	 * @param note the BellNote to play
	 */
	void play(BellNote note) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();
            playNote(line, note);
            line.drain();
        }
    }
	
	/**
	 * called by the play method. playes the note when passed the line to play to and the BellNote
	 * @see #play(BellNote)
	 * @param line
	 * @param bn
	 */
    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
    
    /**
     * Unnecessary but used as a good sanity check. Will join the thread. This should happen automatically when
     * the run() method finishes but just in case we do it here too.
     */
    public void concludeMusicSession() {
    	try {
			t.join();
		} catch (InterruptedException e) {e.printStackTrace();}
    }


}



